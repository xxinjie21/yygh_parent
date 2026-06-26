package com.yygh.order.service;

import com.yygh.model.order.OrderInfo;
import com.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

/**
 * 支付服务接口
 * @author XXJ
 */
public interface PaymentService extends IService<PaymentInfo> {
    /** 向支付记录表添加信息 */
    void savePaymentInfo(OrderInfo order, Integer status);
    /** 支付成功（兼容旧版Map回调） */
    void paySuccess(String out_trade_no, Map<String, String> resultMap);
    /** 支付成功（APIv3回调，直接传transactionId） */
    void paySuccessV3(String outTradeNo, String transactionId);
    /** 获取支付记录 */
    PaymentInfo getPaymentInfo(Long orderId, Integer paymentType);
}
