package com.asyncprocessing.platform.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

@Schema(description = "Respuesta paginada con las tareas registradas")
public record TaskPageResponse(

        @Schema(description = "Tareas incluidas en la página actual",
                implementation = TaskResponse.class)
        List<TaskResponse> content,

        @Schema(description = "Número de página actual, comenzando desde cero",
                example = "0")
        int page,

        @Schema(description = "Cantidad de elementos solicitados por página",
                example = "10")
        int size,

        @Schema(description = "Cantidad total de tareas que coinciden con la consulta",
                example = "25")
        long totalElements,

        @Schema(description = "Cantidad total de páginas disponibles",
                example = "3")
        int totalPages,

        @Schema(description = "Indica si esta es la primera página",
                example = "true")
        boolean first,

        @Schema(description = "Indica si esta es la última página",
                example = "false")
        boolean last

) {

    public static TaskPageResponse from(Page<TaskResponse> tasks) {
        return new TaskPageResponse(
                tasks.getContent(),
                tasks.getNumber(),
                tasks.getSize(),
                tasks.getTotalElements(),
                tasks.getTotalPages(),
                tasks.isFirst(),
                tasks.isLast()
        );
    }
}