package com.carpooling.trip.config;

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

    public static final String ROUTING_KEY_TRIP = "trip.#";
    public static final String ROUTING_KEY_RESERVATION = "reservation.#";

    @Bean
    public TopicExchange carpoolingExchange() {
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
    public Binding tripBinding(Queue tripQueue, TopicExchange carpoolingExchange) {
        return BindingBuilder.bind(tripQueue).to(carpoolingExchange).with(ROUTING_KEY_TRIP);
    }

    @Bean
    public Binding reservationBinding(Queue reservationQueue, TopicExchange carpoolingExchange) {
        return BindingBuilder.bind(reservationQueue).to(carpoolingExchange).with(ROUTING_KEY_RESERVATION);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
