package com.asyncprocessing.platform.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTaskRequest(

        @Schema(description = "Descripción que se utilizará para generar el resumen del reporte",
                example = "Reporte mensual de ventas de octubre",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 200, message = "La descripción no puede superar los 200 caracteres")
        String description

) {
}