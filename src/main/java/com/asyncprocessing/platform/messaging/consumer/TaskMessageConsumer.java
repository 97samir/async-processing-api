package com.asyncprocessing.platform.messaging.consumer;

import com.asyncprocessing.platform.messaging.config.RabbitMQConfig;
import com.asyncprocessing.platform.task.entity.ProcessingTask;
import com.asyncprocessing.platform.task.repository.TaskRepository;
import com.asyncprocessing.platform.task.service.ReportGenerationService;
import com.asyncprocessing.platform.task.service.TaskProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TaskMessageConsumer {

    private final TaskRepository taskRepository;
    private final TaskProcessingService taskProcessingService;
    private final ReportGenerationService reportGenerationService;

    @RabbitListener(queues = RabbitMQConfig.TASK_QUEUE)
    public void consumeTask(String message) {
        Long taskId = Long.valueOf(message);

        log.info("Mensaje recibido. taskId={}", taskId);

        boolean started = taskProcessingService.startTask(taskId);

        if (!started) {
            log.info(
                    "La tarea ya terminó. No se reprocesará. taskId={}",
                    taskId
            );
            return;
        }

        ProcessingTask task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró la tarea: " + taskId
                ));

        String result =
                reportGenerationService.generateSummary(task);

        taskProcessingService.completeTask(taskId, result);

        log.info("Tarea completada. taskId={}", taskId);
    }
}
