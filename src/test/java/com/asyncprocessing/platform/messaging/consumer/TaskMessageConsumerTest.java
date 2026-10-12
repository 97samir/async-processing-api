package com.asyncprocessing.platform.messaging.consumer;

import com.asyncprocessing.platform.task.repository.TaskRepository;
import com.asyncprocessing.platform.task.service.ReportGenerationService;
import com.asyncprocessing.platform.task.service.TaskProcessingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskMessageConsumerTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskProcessingService taskProcessingService;

    @Mock
    private ReportGenerationService reportGenerationService;

    @InjectMocks
    private TaskMessageConsumer taskMessageConsumer;

    // cuando startTask() rechaza un nuevo procesamiento
    // La decisión seguir estando centralizada en TaskProcessingService
    @Test
    void shouldNotProcessTaskAgainWhenTaskCannotBeStarted() {
        // Arrange
        Long taskId = 7L;

        when(taskProcessingService.startTask(taskId))
                .thenReturn(false);

        // Act
        taskMessageConsumer.consumeTask(taskId.toString());

        // Assert
        verify(taskProcessingService).startTask(taskId);

        verifyNoInteractions(
                taskRepository,
                reportGenerationService
        );

        verify(taskProcessingService, never())
                .completeTask(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyString()
                );
    }
}
