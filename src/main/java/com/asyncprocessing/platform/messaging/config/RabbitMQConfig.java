package com.asyncprocessing.platform.messaging.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String TASK_QUEUE = "task.processing";
    public static final String TASK_DLX = "task.processing.dlx";
    public static final String TASK_DLQ = "task.processing.dlq";
    public static final String TASK_DLQ_ROUTING_KEY = "task.processing.failed";

    @Bean
    public Queue taskQueue() {
        return QueueBuilder
                .durable(TASK_QUEUE)
                .deadLetterExchange(TASK_DLX)
                .deadLetterRoutingKey(TASK_DLQ_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange taskDeadLetterExchange() {
        return new DirectExchange(TASK_DLX);
    }

    @Bean
    public Queue taskDeadLetterQueue() {
        return QueueBuilder
                .durable(TASK_DLQ)
                .build();
    }

    @Bean
    public Binding taskDeadLetterBinding() {
        return BindingBuilder
                .bind(taskDeadLetterQueue())
                .to(taskDeadLetterExchange())
                .with(TASK_DLQ_ROUTING_KEY);
    }
}
