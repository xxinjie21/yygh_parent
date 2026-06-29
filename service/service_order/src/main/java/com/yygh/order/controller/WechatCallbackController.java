package com.yygh.order.controller;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.yygh.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信支付回调控制器
 * 接收微信支付结果异步通知，SDK自动验签+AES解密
 *
 * @author XXJ
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/order/weixin")
public class WechatCallbackController {

    private final NotificationParser notificationParser;
    private final PaymentService paymentService;

    /**
     * 微信支付结果回调通知
     * 微信侧支付成功后异步通知此接口，SDK自动完成签名验证和报文解密
     */
    @PostMapping("/callback/notify")
    public String payNotify(@RequestBody String body,
                            @RequestHeader("Wechatpay-Serial") String serial,
                            @RequestHeader("Wechatpay-Nonce") String nonce,
                            @RequestHeader("Wechatpay-Signature") String signature,
                            @RequestHeader("Wechatpay-Timestamp") String timestamp,
                            @RequestHeader("Wechatpay-Signature-Type") String signType) {
        try {
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(serial)
                    .nonce(nonce)
                    .signature(signature)
                    .timestamp(timestamp)
                    .signType(signType)
                    .body(body)
                    .build();

            Notification notification = notificationParser.parse(requestParam, Notification.class);
            String plaintext = notification.getPlaintext();
            log.info("微信支付回调验签通过：{}", plaintext);

            JSONObject result = JSONObject.parseObject(plaintext);
            String outTradeNo = result.getString("out_trade_no");
            String transactionId = result.getString("transaction_id");
            String tradeState = result.getString("trade_state");

            if ("SUCCESS".equals(tradeState)) {
                paymentService.paySuccessV3(outTradeNo, transactionId);
                return "SUCCESS";
            }

            return "FAIL";
        } catch (Exception e) {
            log.error("微信支付回调处理失败", e);
            return "FAIL";
        }
    }
}
