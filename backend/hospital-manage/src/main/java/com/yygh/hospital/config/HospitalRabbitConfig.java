package com.yygh.hospital.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * hospital-manage独立应用的RabbitMQ配置
 * 只定义订单状态通知相关的交换机/队列/绑定
 *
 * @author XXJ
 */
@Configuration
public class HospitalRabbitConfig {

    // 与主模块MqConfig保持一致的常量
    public static final String ORDER_EXCHANGE = "yygh.order.exchange";
    public static final String ORDER_QUEUE = "yygh.order.queue";
    public static final String ORDER_ROUTING_KEY = "yygh.order.status";

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue orderQueue() {
        return new Queue(ORDER_QUEUE, true);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(ORDER_ROUTING_KEY);
    }

    /**
     * RabbitTemplate使用JSON序列化消息体
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
