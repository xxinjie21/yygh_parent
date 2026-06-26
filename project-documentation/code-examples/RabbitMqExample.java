package com.yygh.example;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * RabbitMQ 消息消费者示例
 * 演示如何使用 RabbitMQ 异步处理消息
 */
@Slf4j
@Component
public class RabbitMqExample {

    /**
     * 消息消费者
     * @param messageBody 消息体
     * @param message RabbitMQ 消息对象
     * @param channel 通道对象
     */
    @RabbitListener(queues = "SMS_QUEUE")
    public void handleMessage(String messageBody, Message message, Channel channel) throws IOException {
        try {
            // 解析消息
            log.info("收到消息: {}", messageBody);
            
            // 执行业务逻辑（如发送短信）
            processMessage(messageBody);
            
            // 手动确认消息
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("消息处理失败", e);
            // 拒绝消息，不重新入队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    /**
     * 处理消息的业务逻辑
     */
    private void processMessage(String messageBody) {
        // 实际业务处理逻辑
        // 例如：调用短信发送接口
        // SMSUtils.sendMessage(phone, code);
    }
}
