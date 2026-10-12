package com.asyncprocessing.platform.task.controller;

import com.asyncprocessing.platform.task.dto.CreateTaskRequest;
import com.asyncprocessing.platform.task.dto.TaskPageResponse;
import com.asyncprocessing.platform.task.dto.TaskResponse;
import com.asyncprocessing.platform.task.enums.TaskStatus;
import com.asyncprocessing.platform.task.enums.TaskType;
import com.asyncprocessing.platform.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void shouldCreateTaskAndReturnCreatedStatus() throws Exception {
        when(taskService.createTask(any(CreateTaskRequest.class)))
                .thenReturn(createTaskResponse(7L));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "description": "Generar resumen mensual"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/tasks/7"))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.type").value("REPORT_SUMMARY"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.description")
                        .value("Generar resumen mensual"));

        verify(taskService).createTask(any(CreateTaskRequest.class));
    }

    @Test
    void shouldRejectEmptyDescription() throws Exception {
        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "description": "   "
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldGetTaskById() throws Exception {
        when(taskService.getTaskById(7L))
                .thenReturn(createTaskResponse(7L));

        mockMvc.perform(get("/api/v1/tasks/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.type").value("REPORT_SUMMARY"));
    }

    @Test
    void shouldReturnNotFoundWhenTaskDoesNotExist() throws Exception {
        when(taskService.getTaskById(99L))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró la tarea con ID: 99"
                ));

        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldGetPaginatedTasks() throws Exception {
        TaskResponse task = createTaskResponse(7L);

        TaskPageResponse pageResponse = new TaskPageResponse(
                List.of(task),
                0,
                10,
                1L,
                1,
                true,
                true
        );

        when(taskService.getTasks(0, 10)).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/tasks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(7))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldReturnBadRequestWhenPageIsNegative() throws Exception {
        when(taskService.getTasks(-1, 10))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El número de página no puede ser negativo"
                ));

        mockMvc.perform(get("/api/v1/tasks")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenPageSizeExceedsLimit()
            throws Exception {
        when(taskService.getTasks(0, 101))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El tamaño de página debe estar entre 1 y 100"
                ));

        mockMvc.perform(get("/api/v1/tasks")
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    private TaskResponse createTaskResponse(Long id) {
        return new TaskResponse(
                id,
                TaskType.REPORT_SUMMARY,
                TaskStatus.PENDING,
                "Generar resumen mensual",
                null,
                null,
                null,
                null
        );
    }
}
