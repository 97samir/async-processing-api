package com.asyncprocessing.platform.messaging.producer;

import com.asyncprocessing.platform.task.event.TaskCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class TaskCreatedEventListener {

    private final TaskMessageProducer taskMessageProducer;

    // ejecuta después de confirmar la transacción al evento
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTaskCreated(TaskCreatedEvent event) {
        taskMessageProducer.sendTask(event.taskId());
    }
}
