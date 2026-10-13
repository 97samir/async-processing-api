package com.asyncprocessing.platform.exception;

public class TaskNotFoundException extends RuntimeException {

    public TaskNotFoundException(Long taskId) {
        super("No se encontró la tarea con ID: " + taskId);
    }
}