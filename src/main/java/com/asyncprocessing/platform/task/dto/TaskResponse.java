
package com.asyncprocessing.platform.task.dto;

import com.asyncprocessing.platform.task.entity.ProcessingTask;
import com.asyncprocessing.platform.task.enums.TaskStatus;
import com.asyncprocessing.platform.task.enums.TaskType;

import java.time.LocalDateTime;

public record TaskResponse(
        Long id,
        TaskType type,
        TaskStatus status,
        String description,
        String result,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TaskResponse from(ProcessingTask task) {
        return new TaskResponse(
                task.getId(),
                task.getType(),
                task.getStatus(),
                task.getDescription(),
                task.getResult(),
                task.getErrorMessage(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
