package com.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;
import com.yygh.enums.PaymentTypeEnum;
import com.yygh.enums.RefundStatusEnum;
import com.yygh.model.order.OrderInfo;
import com.yygh.model.order.PaymentInfo;
import com.yygh.model.order.RefundInfo;
import com.yygh.order.config.WechatPayProperties;
import com.yygh.order.service.OrderService;
import com.yygh.order.service.PaymentService;
import com.yygh.order.service.RefundInfoService;
import com.yygh.order.service.WeixinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 微信支付服务实现类（基于官方 wechatpay-java SDK）
 *
 * @author XXJ
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class WeixinServiceImpl implements WeixinService {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final RedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final RefundInfoService refundInfoService;
    private final WechatPayProperties wechatPayProperties;
    private final NativePayService nativePayService;
    private final RefundService refundService;

    /**
     * 生成微信支付Native二维码
     * SDK 内置签名，无需手动拼接 XML
     */
    @Override
    public Map createNative(Long orderId) {
        try {
            // 从Redis获取缓存的支付二维码
            Map payMap = (Map) redisTemplate.opsForValue().get(orderId.toString());
            if (payMap != null) {
                return payMap;
            }
            // 1 根据orderId获取订单信息
            OrderInfo order = orderService.getById(orderId);
            // 2 向支付记录表添加信息
            paymentService.savePaymentInfo(order, PaymentTypeEnum.WEIXIN.getStatus());
            // 3 构建SDK统一下单请求
            PrepayRequest request = new PrepayRequest();
            request.setAppid(wechatPayProperties.getAppid());
            request.setMchid(wechatPayProperties.getPartner());
            request.setDescription("预约挂号-" + order.getHosname());
            request.setOutTradeNo(order.getOutTradeNo());
            // 金额（元→分），微信要求整数「分」
            Amount amount = new Amount();
            amount.setTotal(toFen(order.getAmount()));
            request.setAmount(amount);
            request.setNotifyUrl(wechatPayProperties.getNotifyUrl());
            // 4 调用SDK统一下单（SDK自动签名、构建HTTP请求）
            PrepayResponse response = nativePayService.prepay(request);
            // 5 封装返回结果
            Map map = new HashMap<>();
            map.put("orderId", orderId);
            map.put("totalFee", order.getAmount());
            map.put("resultCode", "SUCCESS");
            map.put("codeUrl", response.getCodeUrl());
            // 缓存到Redis（2小时）
            redisTemplate.opsForValue().set(orderId.toString(), map, 120, TimeUnit.MINUTES);
            return map;
        } catch (Exception e) {
            log.error("微信支付统一下单失败", e);
            return null;
        }
    }

    /**
     * 查询微信支付订单状态
     * SDK 内置签名，返回结构化结果
     */
    @Override
    public Map<String, String> queryPayStatus(Long orderId) {
        try {
            OrderInfo orderInfo = orderService.getById(orderId);
            // 通过out_trade_no查询
            QueryOrderByOutTradeNoRequest queryRequest = new QueryOrderByOutTradeNoRequest();
            queryRequest.setOutTradeNo(orderInfo.getOutTradeNo());
            queryRequest.setMchid(wechatPayProperties.getPartner());
            Transaction transaction = nativePayService.queryOrderByOutTradeNo(queryRequest);
            // 返回简化结果，兼容旧接口格式
            Map<String, String> resultMap = new HashMap<>();
            resultMap.put("trade_state", transaction.getTradeState().name());
            resultMap.put("out_trade_no", transaction.getOutTradeNo());
            resultMap.put("transaction_id", transaction.getTransactionId());
            return resultMap;
        } catch (Exception e) {
            log.error("查询微信支付状态失败", e);
            return null;
        }
    }

    /**
     * 金额换算：元 → 分
     *
     * <p>微信支付以「分」为单位且必须为整数。此处先 setScale 指定舍入模式再取值，
     * 避免 {@code intValue()} 静默丢弃小数导致少收款；
     * 使用 {@code intValueExact()} 而非 {@code intValue()}，
     * 金额超出 int 范围时立即抛异常，好过悄悄溢出成错误金额。
     */
    private int toFen(BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("订单金额不能为空");
        }
        return amount.multiply(CENT).setScale(0, RoundingMode.HALF_UP).intValueExact();
    }

    /** 元与分的换算进制 */
    private static final BigDecimal CENT = new BigDecimal("100");

    /**
     * 微信退款
     * SDK 内置签名和证书管理
     */
    @Override
    public Boolean refund(Long orderId) {
        // 分布式锁防并发重复退款
        String lockKey = "lock:refund:" + orderId;
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock();
        try {
            // 查出支付信息
            PaymentInfo paymentInfo = paymentService.getPaymentInfo(orderId, PaymentTypeEnum.WEIXIN.getStatus());
            if (paymentInfo == null) {
                log.warn("退款失败：未找到支付记录，orderId={}", orderId);
                return false;
            }
            // 添加信息到退款记录表（幂等）
            RefundInfo refundInfo = refundInfoService.saveRefundInfo(paymentInfo);
            // 已退款则直接返回
            if (refundInfo.getRefundStatus().intValue() == RefundStatusEnum.REFUND.getStatus().intValue()) {
                return true;
            }
            // 获取订单金额
            OrderInfo orderInfo = orderService.getById(orderId);
            if (orderInfo == null) {
                log.warn("退款失败：未找到订单，orderId={}", orderId);
                return false;
            }
            // 构建SDK退款请求
            CreateRequest request = new CreateRequest();
            request.setOutTradeNo(paymentInfo.getOutTradeNo());
            request.setOutRefundNo("tk" + paymentInfo.getOutTradeNo());
            request.setTransactionId(paymentInfo.getTradeNo());
            AmountReq amountReq = new AmountReq();
            int refundFee = toFen(orderInfo.getAmount());
            // 退款金额与原订单金额：本项目仅支持全额退款，两者相等；
            // 若后续支持部分退款，refund 应传入实际退款金额，total 始终为原订单金额
            amountReq.setRefund((long) refundFee);
            amountReq.setTotal((long) refundFee);
            amountReq.setCurrency("CNY");
            request.setAmount(amountReq);
            // 调用SDK退款（SDK自动签名、加载证书）
            Refund response = refundService.create(request);
            log.info("微信退款响应：status={}, refundId={}", response.getStatus(), response.getRefundId());
            if (Status.SUCCESS.equals(response.getStatus())) {
                refundInfo.setCallbackTime(new Date());
                refundInfo.setTradeNo(response.getRefundId());
                refundInfo.setRefundStatus(RefundStatusEnum.REFUND.getStatus());
                refundInfo.setCallbackContent(JSONObject.toJSONString(response));
                refundInfoService.updateById(refundInfo);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("微信退款失败", e);
        } finally {
            lock.unlock();
        }
        return false;
    }
}
