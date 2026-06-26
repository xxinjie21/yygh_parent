package com.yygh.order.service;

import java.util.Map;

/**
 * 微信支付服务接口（基于官方 wechatpay-java SDK）
 * @author XXJ
 */
public interface WeixinService {
    /** 生成微信支付Native二维码 */
    Map createNative(Long orderId);
    /** 调用微信接口查询支付状态 */
    Map<String, String> queryPayStatus(Long orderId);
    /** 微信退款 */
    Boolean refund(Long orderId);
}
