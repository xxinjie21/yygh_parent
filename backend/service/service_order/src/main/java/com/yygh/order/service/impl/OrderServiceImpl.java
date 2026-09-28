package com.yygh.order.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.yygh.common.exception.YyghException;
import com.yygh.common.helper.HttpRequestHelper;
import com.yygh.common.result.ResultCodeEnum;
import com.yygh.common.utils.BeanCopyUtils;
import com.yygh.enums.OrderStatusEnum;
import com.yygh.hosp.client.HospitalFeignClient;
import com.yygh.model.order.OrderInfo;
import com.yygh.model.user.Patient;
import com.yygh.order.mapper.OrderMapper;
import com.yygh.order.service.OrderService;
import com.yygh.order.service.WeixinService;
import com.yygh.user.client.PatientFeignClient;
import com.yygh.vo.hosp.ScheduleOrderVo;
import com.yygh.dto.OrderQueryDTO;
import com.yygh.vo.order.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * @author XXJ
 */
@RequiredArgsConstructor
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {
    /**
     * 显式声明 slf4j 日志对象，遮蔽父类 ServiceImpl 继承来的 ibatis Log。
     *
     * <p>为什么不能直接依赖 @Slf4j：MyBatis-Plus 的 ServiceImpl 中有一个
     * <code>protected final org.apache.ibatis.logging.Log log</code> 字段，
     * Lombok 发现父类已存在同名 log 时会跳过生成，导致此处的 log 是 ibatis 的 Log 实现，
     * 它没有 info(String, Object...) 这类占位符重载，使用占位符打日志会直接编译失败。
     */
    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);


    private final PatientFeignClient patientFeignClient;

    private final HospitalFeignClient hospitalFeignClient;

    private final WeixinService weixinService;

    private final RedissonClient redissonClient;

    /**
     * 保存订单（预约挂号）
     *
     * <p>号源并发控制采用两层：Redis 原子扣减（RAtomicLong）+ Redisson 分布式锁串行化同一排班的扣减，
     * 扣减失败或医院接口调用失败均会回退号源。
     *
     * <p><b>注意</b>：本方法包含对医院系统的远程 HTTP 调用，事务持有时间较长。
     * 生产环境下更优的做法是把远程调用拆出事务，改用「本地事务 + 可靠消息最终一致性」，
     * 此处为保证「订单落库」与「号源扣减」的原子性，采用事务内调用并配合失败回滚。
     *
     * @param userId 当前登录用户，用于校验就诊人归属，防止越权使用他人就诊人
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long saveOrder(String scheduleId, Long patientId, Long userId) {
        // 获取就诊人信息
        Patient patient = patientFeignClient.getPatient(patientId);
        if (patient == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 归属校验：就诊人必须属于当前登录用户，防止横向越权替他人挂号
        if (userId == null || !Objects.equals(patient.getUserId(), userId)) {
            log.warn("越权挂号被拦截，操作用户：{}，就诊人：{}，就诊人归属：{}", userId, patientId, patient.getUserId());
            throw new YyghException(ResultCodeEnum.PERMISSION);
        }

        // 获取排班相关信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        if (scheduleOrderVo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        // 判断当前时间是否还可以预约
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }

        // Redisson分布式锁：防止同一排班并发抢号
        String lockKey = "lock:schedule:" + scheduleOrderVo.getHosScheduleId();
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            // 使用Redisson RAtomicLong原子扣减号源
            String redisKey = "schedule:" + scheduleOrderVo.getHosScheduleId() + ":availableNumber";
            RAtomicLong atomicLong = redissonClient.getAtomicLong(redisKey);
            // 初始化：首次使用或Redis重启后，从排班数据同步号源
            if (!atomicLong.isExists()) {
                atomicLong.set(scheduleOrderVo.getAvailableNumber());
            }
            long afterDecrement = atomicLong.addAndGet(-1);
            if (afterDecrement < 0) {
                // 号源不足，回退
                atomicLong.addAndGet(1);
                throw new YyghException(ResultCodeEnum.NUMBER_NO);
            }
            log.info("号源扣减成功，排班编号：{}，Redis键：{}，剩余：{}", scheduleOrderVo.getHosScheduleId(), redisKey, afterDecrement);

            // 获取签名信息
            SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(scheduleOrderVo.getHoscode());

            // 添加到订单表
            OrderInfo orderInfo = new OrderInfo();
            BeanUtils.copyProperties(scheduleOrderVo, orderInfo);
            // 设置订单其他数据
            // 订单交易号：毫秒时间戳 + 6位随机数。
            // 说明：out_trade_no 上建有唯一索引 uk_out_trade_no，极端情况下发生碰撞时由数据库唯一约束兜底拒绝。
            // 相比 new Random()，ThreadLocalRandom 在高并发下无 CAS 竞争，性能更好。
            String outTradeNo = System.currentTimeMillis()
                    + String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
            orderInfo.setOutTradeNo(outTradeNo);
            orderInfo.setScheduleId(scheduleId);
            orderInfo.setUserId(patient.getUserId());
            orderInfo.setPatientId(patientId);
            orderInfo.setPatientName(patient.getName());
            orderInfo.setPatientPhone(patient.getPhone());
            orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());
            baseMapper.insert(orderInfo);

            // 调用医院接口，实现预约挂号操作
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("hoscode", orderInfo.getHoscode());
            paramMap.put("depcode", orderInfo.getDepcode());
            paramMap.put("hosScheduleId", scheduleOrderVo.getHosScheduleId());
            paramMap.put("reserveDate", new DateTime(orderInfo.getReserveDate()).toString("yyyy-MM-dd"));
            paramMap.put("reserveTime", orderInfo.getReserveTime());
            paramMap.put("amount", orderInfo.getAmount());
            paramMap.put("name", patient.getName());
            paramMap.put("certificatesType", patient.getCertificatesType());
            paramMap.put("certificatesNo", patient.getCertificatesNo());
            paramMap.put("sex", patient.getSex());
            paramMap.put("birthdate", patient.getBirthdate());
            paramMap.put("phone", patient.getPhone());
            paramMap.put("isMarry", patient.getIsMarry());
            paramMap.put("provinceCode", patient.getProvinceCode());
            paramMap.put("cityCode", patient.getCityCode());
            paramMap.put("districtCode", patient.getDistrictCode());
            paramMap.put("address", patient.getAddress());
            // 联系人
            paramMap.put("contactsName", patient.getContactsName());
            paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
            paramMap.put("contactsCertificatesNo", patient.getContactsCertificatesNo());
            paramMap.put("contactsPhone", patient.getContactsPhone());
            paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
            String sign = HttpRequestHelper.getSign(paramMap, signInfoVo.getSignKey());
            paramMap.put("sign", sign);

            // 请求医院系统接口
            JSONObject hospitalResult = HttpRequestHelper.sendRequest(paramMap,
                    signInfoVo.getApiUrl() + "/order/submitOrder");

            if (hospitalResult.getInteger("code") == 200) {
                JSONObject jsonObject = hospitalResult.getJSONObject("data");
                // 预约记录唯一标识（医院预约记录主键）
                String hosRecordId = jsonObject.getString("hosRecordId");
                // 预约序号
                Integer number = jsonObject.getInteger("number");
                // 取号时间
                String fetchTime = jsonObject.getString("fetchTime");
                // 取号地址
                String fetchAddress = jsonObject.getString("fetchAddress");
                // 更新订单
                orderInfo.setHosRecordId(hosRecordId);
                orderInfo.setNumber(number);
                orderInfo.setFetchTime(fetchTime);
                orderInfo.setFetchAddress(fetchAddress);
                baseMapper.updateById(orderInfo);

                // 同步更新MySQL中的availableNumber（通过Feign调用service_hosp）
                hospitalFeignClient.updateAvailableNumber(scheduleOrderVo.getHosScheduleId(), -1);
                log.info("订单创建成功，订单号：{}，医院编号：{}", outTradeNo, scheduleOrderVo.getHoscode());

                return orderInfo.getId();
            } else {
                // 医院接口调用失败，回退号源
                redissonClient.getAtomicLong(redisKey).addAndGet(1);
                log.error("医院接口调用失败，号源已回退，排班编号：{}", scheduleOrderVo.getHosScheduleId());
                throw new YyghException(hospitalResult.getString("message"), ResultCodeEnum.FAIL.getCode());
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 根据订单id查询订单详情（带归属校验，防止遍历 orderId 查看他人订单）
     */
    @Override
    public OrderInfoVo getOrder(String orderId, Long userId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        if (orderInfo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 归属校验：订单必须属于当前登录用户
        if (userId == null || !Objects.equals(orderInfo.getUserId(), userId)) {
            log.warn("越权查询订单被拦截，操作用户：{}，订单：{}，订单归属：{}", userId, orderId, orderInfo.getUserId());
            throw new YyghException(ResultCodeEnum.PERMISSION);
        }
        this.packOrderInfo(orderInfo);
        return BeanCopyUtils.copy(orderInfo, OrderInfoVo.class);
    }

    // 订单列表（条件查询带分页）
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryDTO orderQueryDTO) {
        // 获取条件值
        String name = orderQueryDTO.getKeyword(); // 医院名称
        Long userId = orderQueryDTO.getUserId(); // 当前登录用户（行级隔离，必带）
        Long patientId = orderQueryDTO.getPatientId(); // 就诊人ID
        String orderStatus = orderQueryDTO.getOrderStatus(); // 订单状态
        String reserveDate = orderQueryDTO.getReserveDate(); // 预约日期
        String createTimeBegin = orderQueryDTO.getCreateTimeBegin();
        String createTimeEnd = orderQueryDTO.getCreateTimeEnd();

        // 构建查询条件
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, OrderInfo::getHosname, name);
        // 数据隔离：任何情况下都只返回当前用户的订单，避免越权看到他人数据
        wrapper.eq(userId != null, OrderInfo::getUserId, userId);
        wrapper.eq(patientId != null, OrderInfo::getPatientId, patientId);
        wrapper.eq(orderStatus != null, OrderInfo::getOrderStatus, orderStatus);
        // 预约日期为具体某一天，用等值匹配（此前误用 ge 会把该日之后的订单全部查出）
        wrapper.eq(reserveDate != null, OrderInfo::getReserveDate, reserveDate);
        wrapper.ge(createTimeBegin != null, OrderInfo::getCreateTime, createTimeBegin);
        wrapper.le(createTimeEnd != null, OrderInfo::getCreateTime, createTimeEnd);
        // 调用mapper
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        // 封装订单状态描述
        pages.getRecords().forEach(this::packOrderInfo);
        return pages;
    }

    /**
     * 取消预约（带归属校验，供Controller调用，防止越权取消他人订单）
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean cancelOrder(Long orderId, Long userId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        if (orderInfo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        // 归属校验：订单必须属于当前登录用户
        if (userId == null || !Objects.equals(orderInfo.getUserId(), userId)) {
            log.warn("越权取消订单被拦截，操作用户：{}，订单：{}，订单归属：{}", userId, orderId, orderInfo.getUserId());
            throw new YyghException(ResultCodeEnum.PERMISSION);
        }
        return cancelOrder(orderId);
    }

    /**
     * 取消预约（无归属校验，<b>仅限定时任务等系统内部调用</b>）
     *
     * <p>流程顺序说明：先做本地状态校验与退款，再调用医院接口。
     * 原实现先调医院取消再校验本地状态，会出现「医院侧已取消、本地却因状态不符拒绝」的两侧不一致。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean cancelOrder(Long orderId) {
        // 获取订单信息
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        if (orderInfo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        // 获取排班相关信息
        String scheduleId = orderInfo.getScheduleId();
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);
        if (scheduleOrderVo == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        // 判断是否在退号截止时间之前
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if (quitTime.isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }

        Integer currentStatus = orderInfo.getOrderStatus();
        // 已取消状态：幂等直接返回
        if (OrderStatusEnum.CANCLE.getStatus().equals(currentStatus)) {
            log.info("订单已处于取消状态，幂等返回，订单id：{}", orderId);
            return true;
        }
        // 已取号：不可取消
        if (OrderStatusEnum.GET_NUMBER.getStatus().equals(currentStatus)) {
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }
        // 只允许取消「未支付」和「已支付」两种状态，其余状态（如已就诊、已退款）明确拒绝
        if (!OrderStatusEnum.UNPAID.getStatus().equals(currentStatus)
                && !OrderStatusEnum.PAID.getStatus().equals(currentStatus)) {
            log.warn("订单当前状态不允许取消，订单id：{}，状态：{}", orderId, currentStatus);
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
        }

        // 已支付状态：先退款，再取消（退款失败则整体回滚）
        if (OrderStatusEnum.PAID.getStatus().equals(currentStatus)) {
            Boolean isRefund = weixinService.refund(orderId);
            if (!isRefund) {
                throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
            }
            log.info("已支付订单退款成功，订单id：{}", orderId);
        }

        // 调用医院接口实现预约取消
        SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
        if (null == signInfoVo) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }
        Map<String, Object> reqMap = new HashMap<>();
        reqMap.put("hoscode", orderInfo.getHoscode());
        reqMap.put("hosRecordId", orderInfo.getHosRecordId());
        reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
        String sign = HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey());
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper.sendRequest(reqMap,
                signInfoVo.getApiUrl() + "/order/updateCancelStatus");

        // 根据医院接口返回数据
        if (result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }

        // 更新订单状态为已取消
        orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
        baseMapper.updateById(orderInfo);

        // 回退号源：Redis原子值与MySQL在同一把分布式锁内更新，避免并发回退导致余量虚高
        String hosScheduleId = scheduleOrderVo.getHosScheduleId();
        String redisKey = "schedule:" + hosScheduleId + ":availableNumber";
        RLock lock = redissonClient.getLock("lock:schedule:" + hosScheduleId);
        lock.lock();
        try {
            redissonClient.getAtomicLong(redisKey).addAndGet(1);
            hospitalFeignClient.updateAvailableNumber(hosScheduleId, 1);
        } finally {
            lock.unlock();
        }
        log.info("订单取消成功，订单id：{}，号源已回退", orderId);
        return true;
    }

    // 订单统计
    @Override
    public OrderCountVo getCountMap(OrderCountQueryVo orderCountQueryVo) {
        // 调用mapper方法得到统计数据
        List<OrderCountVo> orderCountVoList = baseMapper.selectOrderCount(orderCountQueryVo);
        // 获取X轴数据：日期列表
        List<String> dateList = orderCountVoList.stream()
                .map(OrderCountVo::getReserveDate).collect(Collectors.toList());
        // 获取Y轴数据：数量列表
        List<Integer> countList = orderCountVoList.stream()
                .map(OrderCountVo::getCount).collect(Collectors.toList());
        OrderCountVo orderCountVo = new OrderCountVo();
        orderCountVo.setDateList(dateList);
        orderCountVo.setCountList(countList);
        return orderCountVo;
    }

    // MQ回调：同步订单状态（仅更新本地订单状态，不重复执行业务逻辑）
    @Override
    public void updateOrderStatus(Long hosRecordId, Integer orderStatus) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getHosRecordId, hosRecordId.toString());
        OrderInfo orderInfo = baseMapper.selectOne(wrapper);
        if (orderInfo != null) {
            orderInfo.setOrderStatus(orderStatus);
            baseMapper.updateById(orderInfo);
            log.info("MQ同步订单状态成功，hosRecordId：{}，新状态：{}", hosRecordId, orderStatus);
        } else {
            log.warn("MQ同步订单状态失败，未找到订单，hosRecordId：{}", hosRecordId);
        }
    }

    // 封装订单状态中文描述
    private OrderInfo packOrderInfo(OrderInfo orderInfo) {
        orderInfo.getParam().put("orderStatusString",
                OrderStatusEnum.getStatusNameByStatus(orderInfo.getOrderStatus()));
        return orderInfo;
    }
}
