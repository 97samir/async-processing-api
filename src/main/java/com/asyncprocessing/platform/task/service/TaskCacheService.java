package com.asyncprocessing.platform.task.service;

import com.asyncprocessing.platform.task.dto.TaskResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskCacheService {

    private static final String CACHE_PREFIX = "task:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public Optional<TaskResponse> getTask(Long taskId) {
        String key = buildKey(taskId);

        try {
            String cachedValue = redisTemplate.opsForValue().get(key);

            if (cachedValue == null) {
                log.debug("Caché no encontrada. taskId={}", taskId);
                return Optional.empty();
            }

            TaskResponse response =
                    objectMapper.readValue(cachedValue, TaskResponse.class);

            log.debug("Tarea obtenida desde Redis. taskId={}", taskId);

            return Optional.of(response);

        } catch (DataAccessException exception) {
            log.warn(
                    "Redis no está disponible para consultar taskId={}. Se usará PostgreSQL.",
                    taskId
            );
            return Optional.empty();

        } catch (JacksonException exception) {
            log.warn(
                    "La caché contiene datos inválidos. taskId={}",
                    taskId
            );

            evictTask(taskId);
            return Optional.empty();
        }
    }

    public void cacheTask(TaskResponse task) {
        String key = buildKey(task.id());

        try {
            String json = objectMapper.writeValueAsString(task);

            redisTemplate.opsForValue().set(
                    key,
                    json,
                    CACHE_TTL
            );

            log.debug("Tarea guardada en Redis. taskId={}", task.id());

        } catch (DataAccessException exception) {
            log.warn(
                    "No se pudo guardar la tarea en Redis. taskId={}",
                    task.id()
            );

        } catch (JacksonException exception) {
            log.warn(
                    "No se pudo convertir la tarea a JSON. taskId={}",
                    task.id()
            );
        }
    }

    public void evictTask(Long taskId) {
        try {
            redisTemplate.delete(buildKey(taskId));

            log.debug("Caché invalidada. taskId={}", taskId);

        } catch (DataAccessException exception) {
            log.warn(
                    "No se pudo invalidar la caché. taskId={}",
                    taskId
            );
        }
    }

    private String buildKey(Long taskId) {
        return CACHE_PREFIX + taskId;
    }
}
