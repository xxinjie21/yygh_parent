package com.yygh.order.service.impl;

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
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 * @author XXJ
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, OrderInfo> implements OrderService {

    private final PatientFeignClient patientFeignClient;

    private final HospitalFeignClient hospitalFeignClient;

    private final WeixinService weixinService;

    private final RedissonClient redissonClient;

    // 保存订单（预约挂号）
    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        // 获取就诊人信息
        Patient patient = patientFeignClient.getPatient(patientId);
        if (patient == null) {
            throw new YyghException(ResultCodeEnum.PARAM_ERROR);
        }

        // 获取排班相关信息
        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);

        // 判断当前时间是否还可以预约
        if (new DateTime(scheduleOrderVo.getStartTime()).isAfterNow()
                || new DateTime(scheduleOrderVo.getEndTime()).isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.TIME_NO);
        }

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
        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
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
        String sign = signInfoVo.getSignKey();
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

        } else {
            // 医院接口调用失败，回退号源
            redissonClient.getAtomicLong(redisKey).addAndGet(1);
            log.error("医院接口调用失败，号源已回退，排班编号：{}", scheduleOrderVo.getHosScheduleId());
            throw new YyghException(hospitalResult.getString("message"), ResultCodeEnum.FAIL.getCode());
        }
        return orderInfo.getId();
    }

    // 根据订单id查询订单详情
    @Override
    public OrderInfoVo getOrder(String orderId) {
        OrderInfo orderInfo = baseMapper.selectById(orderId);
        this.packOrderInfo(orderInfo);
        return BeanCopyUtils.copy(orderInfo, OrderInfoVo.class);
    }

    // 订单列表（条件查询带分页）
    @Override
    public IPage<OrderInfo> selectPage(Page<OrderInfo> pageParam, OrderQueryDTO orderQueryDTO) {
        // 获取条件值
        String name = orderQueryDTO.getKeyword(); // 医院名称
        Long patientId = orderQueryDTO.getPatientId(); // 就诊人ID
        String orderStatus = orderQueryDTO.getOrderStatus(); // 订单状态
        String reserveDate = orderQueryDTO.getReserveDate(); // 预约日期
        String createTimeBegin = orderQueryDTO.getCreateTimeBegin();
        String createTimeEnd = orderQueryDTO.getCreateTimeEnd();

        // 构建查询条件
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(name != null, OrderInfo::getHosname, name);
        wrapper.eq(patientId != null, OrderInfo::getPatientId, patientId);
        wrapper.eq(orderStatus != null, OrderInfo::getOrderStatus, orderStatus);
        wrapper.ge(reserveDate != null, OrderInfo::getReserveDate, reserveDate);
        wrapper.ge(createTimeBegin != null, OrderInfo::getCreateTime, createTimeBegin);
        wrapper.le(createTimeEnd != null, OrderInfo::getCreateTime, createTimeEnd);
        // 调用mapper
        IPage<OrderInfo> pages = baseMapper.selectPage(pageParam, wrapper);
        // 封装订单状态描述
        pages.getRecords().forEach(this::packOrderInfo);
        return pages;
    }

    // 取消预约
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

        // 判断是否可以取消
        DateTime quitTime = new DateTime(orderInfo.getQuitTime());
        if (quitTime.isBeforeNow()) {
            throw new YyghException(ResultCodeEnum.CANCEL_ORDER_NO);
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
        String sign = signInfoVo.getSignKey();
        reqMap.put("sign", sign);

        JSONObject result = HttpRequestHelper.sendRequest(reqMap,
                signInfoVo.getApiUrl() + "/order/updateCancelStatus");

        // 根据医院接口返回数据
        if (result.getInteger("code") != 200) {
            throw new YyghException(result.getString("message"), ResultCodeEnum.FAIL.getCode());
        }

        String redisKey = "schedule:" + scheduleOrderVo.getHosScheduleId() + ":availableNumber";

        // 未支付状态：直接取消
        if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.UNPAID.getStatus().intValue()) {
            // 更新订单状态为已取消
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            baseMapper.updateById(orderInfo);
            // Redisson原子回退号源
            redissonClient.getAtomicLong(redisKey).addAndGet(1);
            // 同步更新MySQL
            hospitalFeignClient.updateAvailableNumber(scheduleOrderVo.getHosScheduleId(), 1);
            log.info("未支付订单取消成功，订单id：{}，号源已回退", orderId);
        }

        // 已支付状态：退款后取消
        if (orderInfo.getOrderStatus().intValue() == OrderStatusEnum.PAID.getStatus().intValue()) {
            // 已支付，先退款
            Boolean isRefund = weixinService.refund(orderId);
            if (!isRefund) {
                throw new YyghException(ResultCodeEnum.CANCEL_ORDER_FAIL);
            }
            // 更新订单状态为已取消
            orderInfo.setOrderStatus(OrderStatusEnum.CANCLE.getStatus());
            baseMapper.updateById(orderInfo);
            // Redisson原子回退号源
            redissonClient.getAtomicLong(redisKey).addAndGet(1);
            // 同步更新MySQL
            hospitalFeignClient.updateAvailableNumber(scheduleOrderVo.getHosScheduleId(), 1);
            log.info("已支付订单取消退款成功，订单id：{}，号源已回退", orderId);
        }

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
