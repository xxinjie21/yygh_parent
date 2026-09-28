package com.yygh.order.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.yygh.common.helper.HttpRequestHelper;
import com.yygh.enums.OrderStatusEnum;
import com.yygh.enums.PaymentStatusEnum;
import com.yygh.enums.PaymentTypeEnum;
import com.yygh.hosp.client.HospitalFeignClient;
import com.yygh.model.order.OrderInfo;
import com.yygh.model.order.PaymentInfo;
import com.yygh.order.mapper.PaymentInfoMapper;
import com.yygh.order.service.OrderService;
import com.yygh.order.service.PaymentService;
import com.yygh.vo.order.SignInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付服务实现类
 * @author XXJ
 */
@RequiredArgsConstructor
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentService {
    /**
     * 显式声明 slf4j 日志对象，遮蔽父类 ServiceImpl 继承来的 ibatis Log。
     *
     * <p>为什么不能直接依赖 @Slf4j：MyBatis-Plus 的 ServiceImpl 中有一个
     * <code>protected final org.apache.ibatis.logging.Log log</code> 字段，
     * Lombok 发现父类已存在同名 log 时会跳过生成，导致此处的 log 是 ibatis 的 Log 实现，
     * 它没有 info(String, Object...) 这类占位符重载，使用占位符打日志会直接编译失败。
     */
    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);


    private final OrderService orderService;

    private final HospitalFeignClient hospitalFeignClient;

    //向支付记录表添加信息
    @Override
    public void savePaymentInfo(OrderInfo order, Integer status) {
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderId, order.getId());
        queryWrapper.eq(PaymentInfo::getPaymentType, status);
        Long count = baseMapper.selectCount(queryWrapper);
        if(count >0) return;
        // 保存交易记录
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(order.getId());
        paymentInfo.setPaymentType(status);
        paymentInfo.setOutTradeNo(order.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());
        String subject = new DateTime(order.getReserveDate()).toString("yyyy-MM-dd")+"|"
                +order.getHosname()+"|"+order.getDepname()+"|"+order.getTitle();
        paymentInfo.setSubject(subject);
        paymentInfo.setTotalAmount(order.getAmount());
        baseMapper.insert(paymentInfo);
    }

    /**
     * 支付成功回调（兼容旧版XML回调结果）
     *
     * <p>一致性说明：支付记录与订单状态在同一个本地事务内更新，
     * 避免「钱已收到但订单仍为未支付」的资金损失；
     * 幂等说明：用带状态条件的 UPDATE 代替「先查后写」，以影响行数判断是否为重复回调。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void paySuccess(String out_trade_no, Map<String, String> resultMap) {
        //1 根据订单编号得到支付记录
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOutTradeNo, out_trade_no);
        queryWrapper.eq(PaymentInfo::getPaymentType, PaymentTypeEnum.WEIXIN.getStatus());
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);
        if (paymentInfo == null) {
            log.error("支付记录不存在，outTradeNo：{}", out_trade_no);
            return;
        }
        //2 更新支付记录：状态机条件更新，影响行数为0表示已被处理过（幂等）
        LambdaUpdateWrapper<PaymentInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PaymentInfo::getId, paymentInfo.getId())
                .ne(PaymentInfo::getPaymentStatus, PaymentStatusEnum.PAID.getStatus())
                .set(PaymentInfo::getPaymentStatus, PaymentStatusEnum.PAID.getStatus())
                .set(PaymentInfo::getCallbackTime, new Date())
                .set(PaymentInfo::getTradeNo, resultMap.get("transaction_id"))
                .set(PaymentInfo::getCallbackContent, resultMap.toString());
        if (baseMapper.update(null, updateWrapper) == 0) {
            log.info("支付回调重复通知，已幂等忽略，outTradeNo：{}", out_trade_no);
            return;
        }

        //3 更新订单状态为已支付
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        if (orderInfo == null) {
            log.error("订单不存在，orderId：{}", paymentInfo.getOrderId());
            return;
        }
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);

        //4 调用医院接口同步支付状态（外部系统调用，失败由对账任务补偿）
        notifyHospitalPayStatus(orderInfo);
    }

    /**
     * 支付成功（APIv3回调，直接传transactionId）
     *
     * <p>与 paySuccess(String, Map) 保持一致：同一事务更新支付记录与订单状态，
     * 并以带状态条件的 UPDATE 保证重复回调幂等。
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void paySuccessV3(String outTradeNo, String transactionId) {
        // 1 根据订单编号得到支付记录
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOutTradeNo, outTradeNo);
        queryWrapper.eq(PaymentInfo::getPaymentType, PaymentTypeEnum.WEIXIN.getStatus());
        PaymentInfo paymentInfo = baseMapper.selectOne(queryWrapper);
        if (paymentInfo == null) {
            log.error("支付记录不存在，outTradeNo：{}", outTradeNo);
            return;
        }
        // 2 更新支付记录：状态机条件更新，影响行数为0表示重复回调（幂等）
        LambdaUpdateWrapper<PaymentInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(PaymentInfo::getId, paymentInfo.getId())
                .ne(PaymentInfo::getPaymentStatus, PaymentStatusEnum.PAID.getStatus())
                .set(PaymentInfo::getPaymentStatus, PaymentStatusEnum.PAID.getStatus())
                .set(PaymentInfo::getCallbackTime, new Date())
                .set(PaymentInfo::getTradeNo, transactionId)
                .set(PaymentInfo::getCallbackContent, "APIv3回调: transactionId=" + transactionId);
        if (baseMapper.update(null, updateWrapper) == 0) {
            log.info("支付回调重复通知，已幂等忽略，outTradeNo：{}", outTradeNo);
            return;
        }
        // 3 更新订单状态为已支付
        OrderInfo orderInfo = orderService.getById(paymentInfo.getOrderId());
        if (orderInfo == null) {
            log.error("订单不存在，orderId：{}", paymentInfo.getOrderId());
            return;
        }
        orderInfo.setOrderStatus(OrderStatusEnum.PAID.getStatus());
        orderService.updateById(orderInfo);
        // 4 调用医院接口同步支付状态（外部系统调用，失败由对账任务补偿）
        notifyHospitalPayStatus(orderInfo);
    }

    /**
     * 通知医院系统更新订单支付状态
     *
     * <p>医院属于外部系统，此处失败<b>不回滚本地事务</b>：用户已完成付款，
     * 回滚会导致「已扣款却查不到支付状态」的严重资损。
     * 正确做法是本地状态先落库，外部同步失败记录日志，由对账定时任务补偿重试。
     */
    private void notifyHospitalPayStatus(OrderInfo orderInfo) {
        try {
            SignInfoVo signInfoVo = hospitalFeignClient.getSignInfoVo(orderInfo.getHoscode());
            if (signInfoVo == null) {
                log.error("医院签名信息不存在，hoscode：{}", orderInfo.getHoscode());
                return;
            }
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("hoscode", orderInfo.getHoscode());
            reqMap.put("hosRecordId", orderInfo.getHosRecordId());
            reqMap.put("timestamp", HttpRequestHelper.getTimestamp());
            reqMap.put("sign", HttpRequestHelper.getSign(reqMap, signInfoVo.getSignKey()));
            HttpRequestHelper.sendRequest(reqMap, signInfoVo.getApiUrl() + "/order/updatePayStatus");
        } catch (Exception e) {
            log.error("同步医院支付状态失败，交由对账任务补偿，订单号：{}", orderInfo.getOutTradeNo(), e);
        }
    }

    //获取支付记录
    @Override
    public PaymentInfo getPaymentInfo(Long orderId, Integer paymentType) {
        LambdaQueryWrapper<PaymentInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PaymentInfo::getOrderId, orderId);
        queryWrapper.eq(PaymentInfo::getPaymentType, paymentType);
        return baseMapper.selectOne(queryWrapper);
    }
}
