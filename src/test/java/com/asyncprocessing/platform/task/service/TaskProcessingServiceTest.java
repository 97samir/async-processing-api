package com.asyncprocessing.platform.task.service;

import com.asyncprocessing.platform.task.entity.ProcessingTask;
import com.asyncprocessing.platform.task.enums.TaskStatus;
import com.asyncprocessing.platform.task.enums.TaskType;
import com.asyncprocessing.platform.task.event.TaskUpdatedEvent;
import com.asyncprocessing.platform.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskProcessingServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TaskProcessingService taskProcessingService;

    @Test
    void shouldStartPendingTaskAndPublishEvent() {
        Long taskId = 1L;
        ProcessingTask task = createTask();

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        boolean started = taskProcessingService.startTask(taskId);

        assertTrue(started);
        assertEquals(TaskStatus.PROCESSING, task.getStatus());

        verify(eventPublisher).publishEvent(
                new TaskUpdatedEvent(taskId)
        );
    }

    @Test
    void shouldAllowRetryWhenTaskIsProcessing() {
        Long taskId = 2L;
        ProcessingTask task = createTask();
        task.markAsProcessing();

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        boolean started = taskProcessingService.startTask(taskId);

        assertTrue(started);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldNotRestartCompletedTask() {
        Long taskId = 3L;
        ProcessingTask task = createTask();
        task.markAsProcessing();
        task.complete("Reporte generado");

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        boolean started = taskProcessingService.startTask(taskId);

        assertFalse(started);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldCompleteProcessingTaskAndPublishEvent() {
        Long taskId = 4L;
        ProcessingTask task = createTask();
        task.markAsProcessing();

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        taskProcessingService.completeTask(
                taskId,
                "Reporte generado correctamente"
        );

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
        assertEquals(
                "Reporte generado correctamente",
                task.getResult()
        );

        verify(eventPublisher).publishEvent(
                new TaskUpdatedEvent(taskId)
        );
    }

    @Test
    void shouldNotCompleteTaskThatIsNotProcessing() {
        Long taskId = 5L;
        ProcessingTask task = createTask();

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        taskProcessingService.completeTask(
                taskId,
                "Reporte generado"
        );

        assertEquals(TaskStatus.PENDING, task.getStatus());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldFailProcessingTaskAndPublishEvent() {
        Long taskId = 6L;
        ProcessingTask task = createTask();
        task.markAsProcessing();

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        taskProcessingService.failTask(
                taskId,
                "Error de procesamiento"
        );

        assertEquals(TaskStatus.FAILED, task.getStatus());
        assertEquals(
                "Error de procesamiento",
                task.getErrorMessage()
        );

        verify(eventPublisher).publishEvent(
                new TaskUpdatedEvent(taskId)
        );
    }

    @Test
    void shouldNotFailTaskThatIsNotProcessing() {
        Long taskId = 7L;
        ProcessingTask task = createTask();

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        taskProcessingService.failTask(
                taskId,
                "Error de procesamiento"
        );

        assertEquals(TaskStatus.PENDING, task.getStatus());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() {
        Long taskId = 999L;

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskProcessingService.startTask(taskId)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(eventPublisher, never()).publishEvent(any());
    }

    private ProcessingTask createTask() {
        return new ProcessingTask(
                TaskType.REPORT_SUMMARY,
                "Generar resumen mensual"
        );
    }
}
