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
            if (message == null || message.get("hosRecordId") == null) {
                log.error("订单状态消息格式非法，缺少hosRecordId，message={}", message);
                ackAndStop(channel, msg);
                return;
            }
            Long hosRecordId = Long.valueOf(message.get("hosRecordId").toString());
            Integer orderStatus = Integer.valueOf(message.get("orderStatus").toString());
            log.info("MQ收到订单状态变更消息，hosRecordId：{}，新状态：{}", hosRecordId, orderStatus);
            orderService.updateOrderStatus(hosRecordId, orderStatus);
            // 手动确认
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("处理订单状态变更消息失败，消息将转入死信队列等待排查", e);
            // requeue=false：不重新入队以免无限重试；消息经死信交换机进入 yygh.order.dlq
            channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
        }
    }

    /**
     * 死信队列监听：处理反复失败的消息。
     *
     * <p>此处只做告警，生产环境应接入监控告警或提供人工/定时重投入口，
     * 避免订单状态长期不一致却无人知晓。
     */
    @RabbitListener(queues = MqConfig.ORDER_DLQ)
    public void handleDeadLetter(Message msg, Channel channel) {
        try {
            log.error("订单状态消息已进入死信队列，需人工排查并重投，body={}",
                    new String(msg.getBody(), java.nio.charset.StandardCharsets.UTF_8));
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("处理死信队列消息异常", e);
        }
    }

    /** 消息格式非法时确认并丢弃，避免无效消息反复投递 */
    private void ackAndStop(Channel channel, Message msg) throws IOException {
        channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
    }
}
