package com.asyncprocessing.platform.task.service;
// prueba cambio de estado
import com.asyncprocessing.platform.messaging.producer.TaskCacheInvalidationListener;
import com.asyncprocessing.platform.task.event.TaskUpdatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.event.TransactionPhase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskCacheInvalidationListenerTest {

    @Mock
    private TaskCacheService taskCacheService;

    private TaskCacheInvalidationListener listener;

    @BeforeEach
    void setUp() {
        listener = new TaskCacheInvalidationListener(taskCacheService);
    }

    @Test
    void shouldEvictCacheForUpdatedTask() {
        TaskUpdatedEvent event = new TaskUpdatedEvent(7L);

        listener.onTaskUpdated(event);

        verify(taskCacheService).evictTask(7L);
    }

    @Test
    void shouldUseAfterCommitTransactionPhase() throws NoSuchMethodException {
        var method = TaskCacheInvalidationListener.class
                .getMethod("onTaskUpdated", TaskUpdatedEvent.class);

        var annotation = method.getAnnotation(
                org.springframework.transaction.event.TransactionalEventListener.class
        );

        assertEquals(TransactionPhase.AFTER_COMMIT, annotation.phase());
    }
}
