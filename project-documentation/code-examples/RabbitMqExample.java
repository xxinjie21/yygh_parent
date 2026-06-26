package com.yygh.example;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * RabbitMQ 消息消费者示例
 * 演示如何使用 RabbitMQ 异步处理订单状态变更消息
 *
 * @author XXJ
 */
@Slf4j
@Component
public class RabbitMqExample {

    /**
     * 订单状态变更消息消费者
     * 对应实际项目中的 OrderStatusReceiver
     *
     * @param messageBody 消息体（JSON格式）
     * @param message     RabbitMQ 消息对象
     * @param channel     通道对象
     */
    @RabbitListener(queues = "yygh.order.queue")
    public void handleOrderStatusMessage(String messageBody, Message message, Channel channel) throws IOException {
        try {
            // 解析消息
            log.info("收到订单状态变更消息: {}", messageBody);

            // 执行业务逻辑（如更新订单状态、同步数据）
            processMessage(messageBody);

            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("订单状态消息处理失败", e);
            // 拒绝消息，不重新入队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    /**
     * 处理订单状态变更的业务逻辑
     */
    private void processMessage(String messageBody) {
        // 实际业务处理逻辑
        // 例如：更新医院端订单状态、同步排班数据
    }
}
