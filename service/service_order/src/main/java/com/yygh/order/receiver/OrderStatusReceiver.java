package com.yygh.order.receiver;

import com.yygh.common.config.MqConfig;
import com.yygh.order.service.OrderService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * 订单状态消息消费者
 * 接收hospital-manager发出的订单状态变更通知，同步更新本地订单状态
 * @author XXJ
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OrderStatusReceiver {

    private final OrderService orderService;

    @RabbitListener(queues = MqConfig.ORDER_QUEUE)
    public void handleOrderStatus(Map<String, Object> message, Message msg, Channel channel) throws IOException {
        try {
            Long hosRecordId = Long.valueOf(message.get("hosRecordId").toString());
            Integer orderStatus = Integer.valueOf(message.get("orderStatus").toString());
            log.info("MQ收到订单状态变更消息，hosRecordId：{}，新状态：{}", hosRecordId, orderStatus);
            orderService.updateOrderStatus(hosRecordId, orderStatus);
            // 手动确认
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("处理订单状态变更消息失败", e);
            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
        }
    }
}
