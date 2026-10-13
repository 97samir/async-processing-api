
package com.asyncprocessing.platform.task.service;

import com.asyncprocessing.platform.exception.TaskNotFoundException;
import com.asyncprocessing.platform.task.dto.CreateTaskRequest;
import com.asyncprocessing.platform.task.dto.TaskPageResponse;
import com.asyncprocessing.platform.task.dto.TaskResponse;
import com.asyncprocessing.platform.task.entity.ProcessingTask;
import com.asyncprocessing.platform.task.enums.TaskType;
import com.asyncprocessing.platform.task.event.TaskCreatedEvent;
import com.asyncprocessing.platform.task.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final TaskCacheService taskCacheService;

    @Transactional
    public TaskResponse createTask(CreateTaskRequest request) {

        ProcessingTask task = new ProcessingTask(
                TaskType.REPORT_SUMMARY,
                request.description());

        ProcessingTask savedTask = taskRepository.save(task);
        // publicar
        eventPublisher.publishEvent(
                new TaskCreatedEvent(savedTask.getId()));

        return TaskResponse.from(savedTask);
    }

    // consultar y listar tareas
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {

        var cachedTask = taskCacheService.getTask(id);

        if (cachedTask.isPresent()) {
            return cachedTask.get();
        }

        ProcessingTask task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        TaskResponse response = TaskResponse.from(task);

        taskCacheService.cacheTask(response);

        return response;
    }

    // consultar y listar tareas
    @Transactional(readOnly = true)
    public TaskPageResponse getTasks(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El número de página no puede ser negativo"
            );
        }

        if (size < 1 || size > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El tamaño de página debe estar entre 1 y 100"
            );
        }

        Page<TaskResponse> tasks = taskRepository.findAll(
                PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                )
        ).map(TaskResponse::from);

        return TaskPageResponse.from(tasks);
    }

}
