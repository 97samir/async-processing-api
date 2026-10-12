package com.asyncprocessing.platform.task;

import com.asyncprocessing.platform.task.repository.TaskRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TaskFlowIntegrationIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TaskRepository taskRepository;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Test
    void shouldCreateAndProcessTaskAsynchronously() throws Exception {

        // Arrange: preparar la solicitud para crear la tarea.
        String requestBody = """
                {
                    "description": "Prueba de integración del procesamiento asíncrono"
                }
                """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:" + port + "/api/v1/tasks"
                ))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        // Act: crear la tarea mediante la API REST.
        HttpResponse<String> createResponse = httpClient.send(
                createRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        // Assert: verificar que la tarea se creó correctamente.
        assertEquals(
                201,
                createResponse.statusCode(),
                "La creación de la tarea debe responder HTTP 201"
        );

        assertTrue(
                createResponse.headers().firstValue("Location").isPresent(),
                "La respuesta debe incluir la cabecera Location"
        );

        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)")
                .matcher(createResponse.body());

        assertTrue(
                matcher.find(),
                "La respuesta debe incluir el ID de la tarea"
        );

        String taskId = matcher.group(1);

        // Assert: esperar hasta que termine el procesamiento asíncrono.
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {

                    HttpRequest getRequest = HttpRequest.newBuilder()
                            .uri(URI.create(
                                    "http://localhost:" + port
                                            + "/api/v1/tasks/" + taskId
                            ))
                            .GET()
                            .build();

                    HttpResponse<String> getResponse = httpClient.send(
                            getRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

                    assertEquals(
                            200,
                            getResponse.statusCode(),
                            "La consulta debe responder HTTP 200"
                    );

                    // Verificar los datos devueltos por la API.
                    assertEquals(
                            Long.parseLong(taskId),
                            ((Number) JsonPath.read(
                                    getResponse.body(), "$.id"
                            )).longValue(),
                            "El ID consultado debe coincidir con el creado"
                    );

                    assertEquals(
                            "COMPLETED",
                            JsonPath.read(
                                    getResponse.body(), "$.status"
                            ),
                            "La tarea debe terminar en estado COMPLETED"
                    );

                    assertEquals(
                            "Prueba de integración del procesamiento asíncrono",
                            JsonPath.read(
                                    getResponse.body(), "$.description"
                            ),
                            "La descripción debe conservarse"
                    );

                    Object result = JsonPath.read(
                            getResponse.body(), "$.result"
                    );

                    assertNotNull(
                            result,
                            "El resultado no debe ser nulo"
                    );

                    assertTrue(
                            !result.toString().isBlank(),
                            "El resultado no debe estar vacío"
                    );

                    // Verificar directamente los datos en PostgreSQL.
                    var persistedTask = taskRepository
                            .findById(Long.parseLong(taskId))
                            .orElseThrow(() -> new AssertionError(
                                    "La tarea no existe en PostgreSQL"
                            ));

                    assertEquals(
                            "COMPLETED",
                            persistedTask.getStatus().name(),
                            "El estado debe estar persistido en PostgreSQL"
                    );

                    assertEquals(
                            "Prueba de integración del procesamiento asíncrono",
                            persistedTask.getDescription(),
                            "La descripción debe persistirse correctamente"
                    );

                    assertNotNull(
                            persistedTask.getResult(),
                            "El resultado debe persistirse en PostgreSQL"
                    );

                    assertTrue(
                            !persistedTask.getResult().isBlank(),
                            "El resultado persistido no debe estar vacío"
                    );
                });
    }

    @Test
    void shouldMarkTaskAsFailedAfterRetriesAreExhausted() throws Exception {

        String requestBody = """
            {
                "description": "[PRUEBA-FALLO] Error simulado de procesamiento"
            }
            """;

        HttpRequest createRequest = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:" + port + "/api/v1/tasks"
                ))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> createResponse = httpClient.send(
                createRequest,
                HttpResponse.BodyHandlers.ofString()
        );

        assertEquals(201, createResponse.statusCode());

        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*(\\d+)")
                .matcher(createResponse.body());

        assertTrue(
                matcher.find(),
                "La respuesta debe incluir el ID de la tarea"
        );

        String taskId = matcher.group(1);

        // Esperar a que terminen los reintentos y se procese la DLQ.
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {

                    HttpRequest getRequest = HttpRequest.newBuilder()
                            .uri(URI.create(
                                    "http://localhost:" + port
                                            + "/api/v1/tasks/" + taskId
                            ))
                            .GET()
                            .build();

                    HttpResponse<String> getResponse = httpClient.send(
                            getRequest,
                            HttpResponse.BodyHandlers.ofString()
                    );

                    assertEquals(200, getResponse.statusCode());

                    assertEquals(
                            "FAILED",
                            JsonPath.read(getResponse.body(), "$.status"),
                            "La tarea debe terminar en FAILED"
                    );

                    var persistedTask = taskRepository
                            .findById(Long.parseLong(taskId))
                            .orElseThrow(() -> new AssertionError(
                                    "La tarea no existe en PostgreSQL"
                            ));

                    assertEquals(
                            "FAILED",
                            persistedTask.getStatus().name(),
                            "El estado FAILED debe persistirse en PostgreSQL"
                    );

                    assertNotNull(
                            persistedTask.getErrorMessage(),
                            "Debe registrarse el motivo del fallo"
                    );

                    assertTrue(
                            !persistedTask.getErrorMessage().isBlank(),
                            "El mensaje de error no debe estar vacío"
                    );
                });
    }

}
