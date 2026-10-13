package com.asyncprocessing.platform.task.controller;

import com.asyncprocessing.platform.exception.ApiErrorResponse;
import com.asyncprocessing.platform.task.dto.CreateTaskRequest;
import com.asyncprocessing.platform.task.dto.TaskPageResponse;
import com.asyncprocessing.platform.task.dto.TaskResponse;
import com.asyncprocessing.platform.task.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Tareas",
        description = "Operaciones para crear, consultar y seguir tareas asíncronas")
@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Crear una tarea",
            description = """
                    Registra una nueva tarea y solicita su procesamiento
                    asíncrono. La tarea puede permanecer en PENDING
                    mientras espera a ser procesada.
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Tarea creada correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = TaskResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos enviados no son válidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = taskService.createTask(request);

        URI location = URI.create("/api/v1/tasks/" + response.id());

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @Operation(summary = "Consultar una tarea",
            description = "Obtiene una tarea mediante su identificador.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tarea encontrada",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = TaskResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe una tarea con el identificador indicado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @Operation(summary = "Listar tareas",
            description = """
                    Obtiene las tareas registradas mediante paginación.
                    Los resultados se ordenan por fecha de creación descendente.
                    El número de página comienza en cero y el tamaño
                    permitido está entre 1 y 100.
                    """)
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Tareas obtenidas correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TaskPageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El número de página o el tamaño no son válidos",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class))
            )
    })
    @GetMapping
    public ResponseEntity<TaskPageResponse> getTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(taskService.getTasks(page, size));
    }
}
