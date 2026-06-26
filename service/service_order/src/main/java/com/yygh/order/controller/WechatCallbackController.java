package com.yygh.order.controller;

import com.alibaba.fastjson.JSONObject;
import com.wechat.pay.java.core.notification.Notification;
import com.wechat.pay.java.core.notification.NotificationParser;
import com.wechat.pay.java.core.notification.RequestParam;
import com.yygh.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.util.stream.Collectors;

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
    public String payNotify(HttpServletRequest request) {
        try {
            // 读取请求体
            String body = new BufferedReader(request.getReader())
                    .lines()
                    .collect(Collectors.joining("\n"));

            // 构建验签参数
            RequestParam requestParam = new RequestParam.Builder()
                    .serialNumber(request.getHeader("Wechatpay-Serial"))
                    .nonce(request.getHeader("Wechatpay-Nonce"))
                    .signature(request.getHeader("Wechatpay-Signature"))
                    .timestamp(request.getHeader("Wechatpay-Timestamp"))
                    .signType(request.getHeader("Wechatpay-Signature-Type"))
                    .body(body)
                    .build();

            // SDK自动验签并解密回调数据
            Notification notification = notificationParser.parse(requestParam, Notification.class);
            String plaintext = notification.getPlaintext();
            log.info("微信支付回调验签通过：{}", plaintext);

            // 解析支付结果并更新订单
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
