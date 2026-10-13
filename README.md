# Async Processing API

API REST desarrollada con Java y Spring Boot para crear, procesar y consultar tareas de forma asíncrona.

El proyecto utiliza PostgreSQL para persistir las tareas, RabbitMQ para gestionar el procesamiento en segundo plano y Redis para acelerar las consultas mediante caché.

## Características

- Crear tareas mediante una API REST.
- Procesar tareas de forma asíncrona utilizando RabbitMQ.
- Consultar el estado y el resultado de cada tarea.
- Gestionar los estados `PENDING`, `PROCESSING`, `COMPLETED` y `FAILED`.
- Reintentar automáticamente el procesamiento de mensajes fallidos.
- Enviar mensajes que agotan los reintentos a una Dead Letter Queue (DLQ).
- Persistir las tareas y sus resultados en PostgreSQL.
- Almacenar temporalmente consultas frecuentes en Redis.
- Invalidar la caché cuando cambia el estado de una tarea.
- Validar las solicitudes y centralizar el manejo de errores HTTP.
- Documentar los endpoints mediante Swagger UI y OpenAPI.
- Contar con pruebas unitarias, pruebas de controlador y pruebas de integración.

## Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje de programación |
| Spring Boot | Desarrollo de la aplicación |
| Spring Web | API REST |
| Spring Data JPA | Persistencia y acceso a datos |
| Spring AMQP | Integración con RabbitMQ |
| PostgreSQL | Base de datos relacional |
| RabbitMQ | Mensajería y procesamiento asíncrono |
| Redis | Caché de consultas |
| Maven Wrapper | Compilación y gestión de dependencias |
| Docker Compose | Ejecución de los servicios externos |
| SpringDoc OpenAPI | Documentación interactiva de la API |
| JUnit 5 y Mockito | Pruebas automatizadas |
| Awaitility | Espera y comprobación de procesos asíncronos |

## Arquitectura

```text
                    Cliente HTTP
                         |
                         v
                 API REST (Spring Boot)
                         |
              +----------+----------+
              |                     |
              v                     v
        PostgreSQL                Redis
       Datos persistentes       Caché temporal
              |
              v
       Evento de creación
              |
              v
           RabbitMQ
              |
              v
     Consumidor asíncrono
              |
              v
    Generación del resumen
              |
              v
     Actualización de tarea
              |
              v
          PostgreSQL

       RabbitMQ / Reintentos
              |
              v
       Dead Letter Queue
              |
              v
       Registro del fallo
```

La API responde al cliente sin esperar a que termine el procesamiento. El consumidor trabaja en segundo plano y actualiza el estado de la tarea para que pueda consultarse posteriormente.

## Requisitos previos

Antes de ejecutar el proyecto, necesitas:

- JDK 21.
- Docker Desktop con Docker Compose.
- Git, si deseas clonar el repositorio.

No necesitas instalar Maven por separado porque el proyecto incluye Maven Wrapper.

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd async-processing-api
```

Reemplaza `<URL_DEL_REPOSITORIO>` por la dirección real del repositorio.

### 2. Configurar las variables de entorno

Revisa los archivos `application.yaml`, `application-docker.yml` y `docker-compose.yml` para identificar las variables utilizadas por tu configuración.

Si el repositorio incluye `.env.example`, puedes crear tu archivo local `.env` a partir de esa plantilla y completar los valores necesarios.

En PowerShell:

```powershell
Copy-Item .env.example .env
```

Ejecuta este comando solo si `.env.example` existe en la raíz del proyecto.

No publiques tu archivo `.env` ni contraseñas reales en GitHub. Utiliza credenciales de desarrollo y verifica que `.env` esté incluido en `.gitignore`.

### 3. Iniciar los servicios

Desde la raíz del proyecto:

```powershell
docker compose up -d
```

Comprueba el estado de los contenedores:

```powershell
docker compose ps
```

Los servicios utilizados por la aplicación son PostgreSQL, RabbitMQ y Redis. Sus puertos y credenciales deben coincidir con la configuración efectiva del proyecto.

### 4. Ejecutar la aplicación

En Windows, desde la raíz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

La API utiliza el puerto `8081` en la configuración de desarrollo actual.

Si utilizas IntelliJ IDEA, también puedes ejecutar la clase principal `AsyncProcessingApiApplication`.

## Documentación de la API

Con la aplicación iniciada, abre Swagger UI:

**[Swagger UI](http://localhost:8081/swagger-ui/index.html)**

También puedes consultar la especificación OpenAPI en:

[OpenAPI JSON](http://localhost:8081/v3/api-docs)

Swagger permite revisar los endpoints, sus parámetros, los esquemas de solicitud y respuesta y las respuestas HTTP documentadas, además de ejecutar solicitudes de prueba.

## Endpoints

La API dispone de los siguientes endpoints principales:

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/tasks` | Crea una tarea de resumen |
| `GET` | `/api/v1/tasks/{id}` | Consulta una tarea por su identificador |
| `GET` | `/api/v1/tasks?page=0&size=10` | Lista tareas mediante paginación |

### Crear una tarea

**`POST /api/v1/tasks`**

Crea una tarea de tipo `REPORT_SUMMARY` y devuelve `201 Created` cuando la solicitud se acepta correctamente.

Ejemplo de solicitud:

```json
{
  "description": "Generar resumen del informe mensual"
}
```

La descripción es obligatoria y admite un máximo de 200 caracteres.

Ejemplo de respuesta:

```json
{
  "id": 1,
  "type": "REPORT_SUMMARY",
  "status": "PENDING",
  "description": "Generar resumen del informe mensual",
  "result": null,
  "errorMessage": null,
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-01T10:00:00"
}
```

Los identificadores y las fechas son ilustrativos. Debido al procesamiento asíncrono, el estado puede cambiar poco después de crear la tarea.

### Consultar una tarea

**`GET /api/v1/tasks/{id}`**

Ejemplo:

```http
GET http://localhost:8081/api/v1/tasks/1
```

La respuesta contiene el identificador, el tipo, el estado, la descripción, el resultado, el mensaje de error y las fechas de creación y actualización.

Si la tarea no existe, la API devuelve `404 Not Found`.

### Listar tareas

**`GET /api/v1/tasks?page=0&size=10`**

Permite consultar las tareas registradas mediante paginación.

- `page`: número de página, comenzando en `0`.
- `size`: cantidad de elementos solicitados, entre `1` y `100`.

Las tareas se ordenan por fecha de creación descendente.

La respuesta incluye el contenido de la página y sus metadatos: página actual, tamaño, total de elementos, total de páginas e indicadores de primera y última página.

## Estados de una tarea

| Estado | Descripción |
|---|---|
| `PENDING` | La tarea está pendiente de procesamiento. |
| `PROCESSING` | El consumidor está procesando la tarea. |
| `COMPLETED` | El procesamiento terminó correctamente. |
| `FAILED` | El procesamiento terminó en error después de los intentos configurados. |

La creación de una tarea no implica que su procesamiento haya terminado. Para conocer el resultado, consulta su estado mediante el endpoint de consulta.

## Manejo de errores

La API utiliza un manejador global para devolver respuestas de error con una estructura uniforme.

Los errores incluyen los siguientes campos:

| Campo | Descripción |
|---|---|
| `status` | Código de estado HTTP |
| `error` | Descripción del estado HTTP |
| `message` | Mensaje que explica el problema |
| `path` | Ruta donde ocurrió el error |
| `timestamp` | Fecha y hora del error |

Los principales estados HTTP documentados son:

- `201 Created`: tarea creada correctamente.
- `200 OK`: consulta realizada correctamente.
- `400 Bad Request`: solicitud inválida o parámetros de paginación incorrectos.
- `404 Not Found`: no existe una tarea con el identificador solicitado.

## Procesamiento asíncrono y reintentos

RabbitMQ permite desacoplar la recepción de solicitudes del procesamiento de las tareas.

La configuración actual contempla hasta tres intentos totales, incluido el intento inicial. Si el consumidor agota los intentos configurados, el mensaje se envía a una Dead Letter Queue (DLQ), donde se gestiona el fallo y se actualiza el estado de la tarea.

## Caché con Redis

Las consultas individuales utilizan Redis para reducir las lecturas repetidas a PostgreSQL.

- Las entradas de caché tienen una duración de cinco minutos.
- La caché se invalida cuando se confirma una actualización del estado de la tarea.
- Si Redis no está disponible durante una consulta, la aplicación intenta obtener la tarea desde PostgreSQL.
- PostgreSQL continúa siendo la fuente persistente de los datos.

## Pruebas automatizadas

Ejecutar todas las pruebas:

```powershell
.\mvnw.cmd test
```

Ejecutar las pruebas del flujo de integración asíncrono:

```powershell
.\mvnw.cmd -Dtest=TaskFlowIntegrationIT test
```

Ejecutar las pruebas del consumidor:

```powershell
.\mvnw.cmd -Dtest=TaskMessageConsumerTest test
```

Las pruebas que requieren servicios externos necesitan que esos servicios estén disponibles y correctamente configurados.

## Detener los servicios

Para detener y eliminar los contenedores:

```powershell
docker compose down
```

Los volúmenes nombrados normalmente conservan los datos. Si deseas eliminar también los datos locales persistidos, puedes utilizar `docker compose down -v`, pero ten en cuenta que esta operación puede borrar la información de las bases de datos del entorno de desarrollo.

## Objetivo del proyecto

Este proyecto forma parte de mi portafolio de desarrollo backend y busca demostrar conocimientos prácticos en Java, Spring Boot, APIs REST, persistencia relacional, mensajería asíncrona, caché, manejo de errores y pruebas automatizadas.

El objetivo es construir una API organizada y mantenible que permita procesar tareas en segundo plano y consultar sus resultados de forma fiable.