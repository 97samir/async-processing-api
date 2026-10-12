package com.asyncprocessing.platform.messaging.producer;

import com.asyncprocessing.platform.messaging.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendTask(Long taskId) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.TASK_QUEUE,
                taskId.toString()
        );

        log.info("Tarea enviada a RabbitMQ. taskId={}", taskId);
    }
}
