package com.carpooling.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "carpooling.topic";
    public static final String TRIP_QUEUE = "notification.trip.queue";
    public static final String RESERVATION_QUEUE = "notification.reservation.queue";

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue tripQueue() {
        return QueueBuilder.durable(TRIP_QUEUE).build();
    }

    @Bean
    public Queue reservationQueue() {
        return QueueBuilder.durable(RESERVATION_QUEUE).build();
    }

    @Bean
    public Binding tripBinding(Queue tripQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(tripQueue).to(topicExchange).with("trip.#");
    }

    @Bean
    public Binding reservationBinding(Queue reservationQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(reservationQueue).to(topicExchange).with("reservation.#");
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
