package com.asyncprocessing.platform.task.service;

import com.asyncprocessing.platform.task.dto.TaskResponse;
import com.asyncprocessing.platform.task.enums.TaskStatus;
import com.asyncprocessing.platform.task.enums.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskCacheServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;

    private TaskCacheService taskCacheService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        taskCacheService = new TaskCacheService(
                redisTemplate,
                objectMapper
        );
    }

    @Test
    void shouldReturnTaskWhenItExistsInCache() throws Exception {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        TaskResponse expected = createTaskResponse();
        String json = objectMapper.writeValueAsString(expected);

        when(valueOperations.get("task:7"))
                .thenReturn(json);

        Optional<TaskResponse> result =
                taskCacheService.getTask(7L);

        assertTrue(result.isPresent());
        assertEquals(7L, result.get().id());
        assertEquals(
                TaskStatus.COMPLETED,
                result.get().status()
        );

        verify(valueOperations).get("task:7");
        verify(redisTemplate, never()).delete("task:7");
    }

    @Test
    void shouldReturnEmptyWhenTaskIsNotCached() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get("task:8"))
                .thenReturn(null);

        Optional<TaskResponse> result =
                taskCacheService.getTask(8L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSaveTaskWithFiveMinuteTtl() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        TaskResponse task = createTaskResponse();

        taskCacheService.cacheTask(task);

        verify(valueOperations).set(
                eq("task:7"),
                anyString(),
                eq(Duration.ofMinutes(5))
        );
    }

    @Test
    void shouldContinueWhenRedisIsUnavailable() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get("task:7"))
                .thenThrow(new RedisConnectionFailureException(
                        "Redis no disponible"
                ));

        Optional<TaskResponse> result =
                taskCacheService.getTask(7L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldEvictInvalidJsonAndReturnEmpty() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        when(valueOperations.get("task:7"))
                .thenReturn("{json-invalido");

        Optional<TaskResponse> result =
                taskCacheService.getTask(7L);

        assertTrue(result.isEmpty());

        verify(redisTemplate).delete("task:7");
    }

    @Test
    void shouldNotThrowWhenRedisFailsToSaveTask() {

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        TaskResponse task = createTaskResponse();

        doThrow(new RedisConnectionFailureException(
                "Redis no disponible"
        )).when(valueOperations).set(
                eq("task:7"),
                anyString(),
                eq(Duration.ofMinutes(5))
        );

        assertDoesNotThrow(
                () -> taskCacheService.cacheTask(task)
        );
    }

    @Test
    void shouldNotThrowWhenRedisFailsToDeleteTask() {
        doThrow(new RedisConnectionFailureException(
                "Redis no disponible"
        )).when(redisTemplate).delete("task:7");

        assertDoesNotThrow(
                () -> taskCacheService.evictTask(7L)
        );
    }

    private TaskResponse createTaskResponse() {
        return new TaskResponse(
                7L,
                TaskType.REPORT_SUMMARY,
                TaskStatus.COMPLETED,
                "Generar resumen mensual",
                "Resumen generado correctamente",
                null,
                null,
                null
        );
    }
}
