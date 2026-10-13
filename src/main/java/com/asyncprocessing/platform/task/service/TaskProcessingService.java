package com.asyncprocessing.platform.task.service;

import com.asyncprocessing.platform.exception.TaskNotFoundException;
import com.asyncprocessing.platform.task.entity.ProcessingTask;
import com.asyncprocessing.platform.task.enums.TaskStatus;
import com.asyncprocessing.platform.task.event.TaskUpdatedEvent;
import com.asyncprocessing.platform.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskProcessingService {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public boolean startTask(Long taskId) {

        ProcessingTask task = findTask(taskId);

        if (task.getStatus() == TaskStatus.PENDING) {
            task.markAsProcessing();
            eventPublisher.publishEvent(new TaskUpdatedEvent(taskId));
            return true;
        }

        // Permite continuar un intento que falló mientras
        // la tarea estaba en PROCESSING.
        if (task.getStatus() == TaskStatus.PROCESSING) {
            return true;
        }

        // Las tareas COMPLETED o FAILED no se reprocesan.
        return false;
    }

    @Transactional
    public void completeTask(Long taskId, String result) {
        ProcessingTask task = findTask(taskId);

        if (task.getStatus() != TaskStatus.PROCESSING) {
            return;
        }

        task.complete(result);
        eventPublisher.publishEvent(new TaskUpdatedEvent(taskId));
    }

    @Transactional
    public void failTask(Long taskId, String errorMessage) {
        ProcessingTask task = findTask(taskId);

        if (task.getStatus() != TaskStatus.PROCESSING) {
            return;
        }

        task.fail(errorMessage);
        eventPublisher.publishEvent(new TaskUpdatedEvent(taskId));
    }

    private ProcessingTask findTask(Long taskId) {
        return taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId ));
    }
}
