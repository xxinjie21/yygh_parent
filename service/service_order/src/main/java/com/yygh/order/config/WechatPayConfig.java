package com.yygh.order.config;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.notification.NotificationConfig;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RSANotificationConfig;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.refund.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * 微信支付SDK配置类
 * 构造官方SDK所需的Config、Service、NotificationParser Bean
 *
 * @author XXJ
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WechatPayConfig {

    private final WechatPayProperties properties;

    /**
     * 微信支付SDK核心配置
     * 使用 RSA 自动证书管理，SDK 自动处理签名和验签
     */
    @Bean
    public Config wechatPayConfig() {
        try {
            // 从classpath加载商户私钥
            ClassPathResource resource = new ClassPathResource(
                    properties.getPrivateKeyPath().replace("classpath:", ""));
            String privateKey;
            try (InputStream is = resource.getInputStream()) {
                privateKey = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            }

            return new RSAAutoCertificateConfig.Builder()
                    .merchantId(properties.getPartner())
                    .privateKey(privateKey)
                    .merchantSerialNumber(properties.getMerchantSerialNumber())
                    .apiV3Key(properties.getApiV3Key())
                    .build();
        } catch (Exception e) {
            log.error("微信支付SDK配置初始化失败", e);
            throw new RuntimeException("微信支付SDK配置初始化失败", e);
        }
    }

    /**
     * Native支付服务（统一下单、生成二维码）
     */
    @Bean
    public NativePayService nativePayService(Config config) {
        return new NativePayService.Builder()
                .config(config)
                .build();
    }

    /**
     * 退款服务
     */
    @Bean
    public RefundService refundService(Config config) {
        return new RefundService.Builder()
                .config(config)
                .build();
    }

    /**
     * 回调通知解析器（SDK自动验签、解密）
     */
    @Bean
    public NotificationParser notificationParser(Config config) {
        NotificationConfig notificationConfig = new RSANotificationConfig.Builder()
                .apiV3Key(properties.getApiV3Key())
                .build();
        return new NotificationParser((com.wechat.pay.java.core.notification.RSANotificationConfig) notificationConfig);
    }
}
