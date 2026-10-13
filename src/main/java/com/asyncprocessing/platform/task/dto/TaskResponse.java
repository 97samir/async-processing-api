package com.asyncprocessing.platform.task.dto;

import com.asyncprocessing.platform.task.entity.ProcessingTask;
import com.asyncprocessing.platform.task.enums.TaskStatus;
import com.asyncprocessing.platform.task.enums.TaskType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información de una tarea de procesamiento")
public record TaskResponse(

        @Schema(description = "Identificador único de la tarea",
                example = "7")
        Long id,

        @Schema(description = "Tipo de tarea solicitada",
                example = "REPORT_SUMMARY")
        TaskType type,

        @Schema(description = "Estado actual del procesamiento",
                example = "COMPLETED")
        TaskStatus status,

        @Schema(description = "Descripción proporcionada al crear la tarea",
                example = "Reporte mensual de ventas de octubre")
        String description,

        @Schema(description = "Resultado generado cuando el procesamiento termina correctamente",
                example = "Resumen generado correctamente para: Reporte mensual de ventas de octubre",
                nullable = true)
        String result,

        @Schema(description = "Detalle del error cuando el procesamiento falla",
                example = "El procesamiento falló después de agotar los reintentos",
                nullable = true)
        String errorMessage,

        @Schema(description = "Fecha y hora de creación de la tarea",
                example = "2026-10-12T14:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Fecha y hora de la última actualización de la tarea",
                example = "2026-10-12T14:30:05")
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