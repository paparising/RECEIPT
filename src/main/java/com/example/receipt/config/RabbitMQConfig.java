package com.example.receipt.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Main Queue Configuration
    public static final String REPORT_QUEUE = "report.queue";
    public static final String REPORT_EXCHANGE = "report.exchange";
    public static final String REPORT_ROUTING_KEY = "report.generate";

    // Dead Letter Queue Configuration
    public static final String REPORT_DLQ_QUEUE = "report.dlq.queue";
    public static final String REPORT_DLQ_EXCHANGE = "report.dlq.exchange";
    public static final String REPORT_DLQ_ROUTING_KEY = "report.dlq";

    // Dead Letter Queue
    @Bean
    public Queue reportDLQueue() {
        return new Queue(REPORT_DLQ_QUEUE, true);
    }

    @Bean
    public TopicExchange reportDLExchange() {
        return new TopicExchange(REPORT_DLQ_EXCHANGE, true, false);
    }

    @Bean
    public Binding reportDLBinding(Queue reportDLQueue, TopicExchange reportDLExchange) {
        return BindingBuilder.bind(reportDLQueue)
                .to(reportDLExchange)
                .with(REPORT_DLQ_ROUTING_KEY);
    }

    // Main Report Queue with Dead Letter Configuration
    @Bean
    public Queue reportQueue() {
        return QueueBuilder.durable(REPORT_QUEUE)
                .deadLetterExchange(REPORT_DLQ_EXCHANGE)
                .deadLetterRoutingKey(REPORT_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange reportExchange() {
        return new TopicExchange(REPORT_EXCHANGE, true, false);
    }

    @Bean
    public Binding reportBinding(Queue reportQueue, TopicExchange reportExchange) {
        return BindingBuilder.bind(reportQueue)
                .to(reportExchange)
                .with(REPORT_ROUTING_KEY);
    }
}
