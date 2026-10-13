package com.asyncprocessing.platform.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Información de un error ocurrido al procesar una solicitud")
public record ApiErrorResponse(

        @Schema(description = "Código de estado HTTP",
                example = "404")
        int status,

        @Schema(description = "Tipo de error HTTP",
                example = "Not Found")
        String error,

        @Schema(description = "Descripción del problema",
                example = "No se encontró la tarea con ID: 99")
        String message,

        @Schema(description = "Ruta donde ocurrió el error",
                example = "/api/v1/tasks/99")
        String path,

        @Schema(description = "Fecha y hora en que se produjo el error",
                example = "2026-10-12T21:30:00")
        LocalDateTime timestamp

) {
}