package com.yygh.common.service;

import com.yygh.common.config.MqConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * RabbitMQ消息发送服务
 * 封装RabbitTemplate，提供统一的消息发送入口
 *
 * @author XXJ
 */
@Service
@RequiredArgsConstructor
public class RabbitService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 发送订单状态变更消息
     * @param message 订单状态消息（hosRecordId、orderStatus等）
     */
    public void sendOrderStatusMessage(Map<String, Object> message) {
        rabbitTemplate.convertAndSend(MqConfig.ORDER_EXCHANGE, MqConfig.ORDER_ROUTING_KEY, message);
    }
}
