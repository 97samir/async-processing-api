package com.asyncprocessing.platform.messaging.consumer;
// mensajes fallidos
import com.asyncprocessing.platform.messaging.config.RabbitMQConfig;
import com.asyncprocessing.platform.task.service.TaskProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskDeadLetterConsumer {

    private final TaskProcessingService taskProcessingService;

    @RabbitListener(queues = RabbitMQConfig.TASK_DLQ)
    public void handleDeadLetter(String message) {
        Long taskId = Long.valueOf(message);

        log.error(
                "La tarea agotó los reintentos de procesamiento. taskId={}",
                taskId
        );

        taskProcessingService.failTask(
                taskId,
                "El procesamiento falló después de agotar los reintentos"
        );
    }
}
