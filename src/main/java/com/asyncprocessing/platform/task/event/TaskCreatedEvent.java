package com.asyncprocessing.platform.task.event;

// para transportar el identificador de la tarea
public record TaskCreatedEvent(
        Long taskId) {
}
