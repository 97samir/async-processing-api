package com.asyncprocessing.platform.task.service;

import com.asyncprocessing.platform.task.dto.TaskResponse;
import com.asyncprocessing.platform.task.entity.ProcessingTask;
import com.asyncprocessing.platform.task.enums.TaskType;
import com.asyncprocessing.platform.task.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TaskCacheService taskCacheService;

    @InjectMocks
    private TaskService taskService;

    // Caché encontrada
    @Test
    void shouldReturnTaskFromCacheWhenItExists() {
        Long taskId = 7L;

        TaskResponse cachedTask = new TaskResponse(
                taskId,
                TaskType.REPORT_SUMMARY,
                com.asyncprocessing.platform.task.enums.TaskStatus.COMPLETED,
                "Generar resumen mensual",
                "Resumen generado correctamente",
                null,
                null,
                null
        );

        when(taskCacheService.getTask(taskId))
                .thenReturn(Optional.of(cachedTask));

        TaskResponse result = taskService.getTaskById(taskId);

        assertEquals(taskId, result.id());

        verify(taskRepository, never()).findById(taskId);
        verify(taskCacheService, never()).cacheTask(any());
    }

    // cache vacia
    @Test
    void shouldGetTaskFromDatabaseWhenCacheMisses() {
        Long taskId = 8L;

        ProcessingTask task = new ProcessingTask(
                TaskType.REPORT_SUMMARY,
                "Generar resumen semanal"
        );

        when(taskCacheService.getTask(taskId))
                .thenReturn(Optional.empty());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.of(task));

        TaskResponse result = taskService.getTaskById(taskId);

        assertEquals("Generar resumen semanal", result.description());

        verify(taskRepository).findById(taskId);
        verify(taskCacheService).cacheTask(any(TaskResponse.class));
    }

    // tarea inexistente
    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() {
        Long taskId = 999L;

        when(taskCacheService.getTask(taskId))
                .thenReturn(Optional.empty());

        when(taskRepository.findById(taskId))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getTaskById(taskId)
        );

        assertEquals(404, exception.getStatusCode().value());

        verify(taskCacheService, never()).cacheTask(any());
    }
}
