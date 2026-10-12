package com.asyncprocessing.platform.messaging.producer;

import com.asyncprocessing.platform.task.event.TaskUpdatedEvent;
import com.asyncprocessing.platform.task.service.TaskCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TaskCacheInvalidationListener {

    private final TaskCacheService taskCacheService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskUpdated(TaskUpdatedEvent event) {

        taskCacheService.evictTask(event.taskId());
    }
}