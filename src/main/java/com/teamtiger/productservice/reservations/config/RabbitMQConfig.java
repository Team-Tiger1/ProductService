package com.teamtiger.productservice.reservations.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for publishing reservation related events.
 */
@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "reservation.events";

    public static final String DELAY_EXCHANGE = "reservation.delay";
    public static final String DELAY_QUEUE = "reservation.delay.queue";
    public static final String DELAY_ROUTING_KEY = "reservation.delay.key";

    @Bean
    public DirectExchange productExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-delayed-type", "direct");
        return new CustomExchange(DELAY_EXCHANGE, "x-delayed-message", true, false, arguments);
    }

    @Bean
    public Queue delayQueue() {
        return new Queue(DELAY_QUEUE, true);
    }

    @Bean
    public Binding delayQueueBinding(Queue delayQueue, CustomExchange delayedExchange) {
        return BindingBuilder.bind(delayQueue)
                .to(delayedExchange)
                .with(DELAY_ROUTING_KEY)
                .noargs();
    }

    //Converts Java objects to JSON
    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter messageConverter) {

        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

}
