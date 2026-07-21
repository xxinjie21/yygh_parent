package com.yygh.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信支付配置属性类
 * 映射 application.properties 中 weixin.* 配置项
 *
 * @author XXJ
 */
@Data
@Component
@ConfigurationProperties(prefix = "weixin")
public class WechatPayProperties {

    /** 微信公众号appid */
    private String appid;

    /** 微信商户号（mchId） */
    private String partner;

    /** APIv3密钥（32位，商户平台设置） */
    private String apiV3Key;

    /** 商户证书序列号 */
    private String merchantSerialNumber;

    /** 商户API私钥路径（classpath相对路径） */
    private String privateKeyPath;

    /** 支付结果回调通知地址 */
    private String notifyUrl;
}
