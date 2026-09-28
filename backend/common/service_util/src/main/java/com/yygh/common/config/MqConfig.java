package com.yygh.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 * 定义交换机、队列、绑定关系
 *
 * @author XXJ
 */
@Configuration
public class MqConfig {

    // ==================== 订单状态通知 ====================
    public static final String ORDER_EXCHANGE = "yygh.order.exchange";
    public static final String ORDER_QUEUE = "yygh.order.queue";
    public static final String ORDER_ROUTING_KEY = "yygh.order.status";

    // ==================== 死信：承载多次处理失败的订单状态消息 ====================
    public static final String ORDER_DLX_EXCHANGE = "yygh.order.dlx.exchange";
    public static final String ORDER_DLQ = "yygh.order.dlq";
    public static final String ORDER_DLQ_ROUTING_KEY = "yygh.order.dlq";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    /**
     * 订单队列绑定死信交换机。
     *
     * <p>消费者在业务异常时执行 {@code basicNack(requeue=false)}，
     * 如果不配死信交换机，这些消息会被<b>永久丢弃</b>，订单状态将再也无法同步。
     * 配置后消息会转入死信队列，可排查并重投。
     */
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(ORDER_QUEUE)
                .deadLetterExchange(ORDER_DLX_EXCHANGE)
                .deadLetterRoutingKey(ORDER_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }

    @Bean
    public DirectExchange orderDlxExchange() {
        return new DirectExchange(ORDER_DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderDeadLetterQueue() {
        return QueueBuilder.durable(ORDER_DLQ).build();
    }

    @Bean
    public Binding orderDlqBinding() {
        return BindingBuilder.bind(orderDeadLetterQueue())
                .to(orderDlxExchange())
                .with(ORDER_DLQ_ROUTING_KEY);
    }

    /**
     * RabbitTemplate使用JSON序列化消息体
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
