# Async Processing API

API REST desarrollada con Java y Spring Boot para crear y procesar tareas de forma asíncrona. Utiliza RabbitMQ para gestionar los mensajes, PostgreSQL para almacenar las tareas y Redis para acelerar las consultas mediante caché.

## Características

- Crear tareas mediante una API REST.
- Procesar tareas de forma asíncrona utilizando RabbitMQ.
- Consultar el estado y el resultado de cada tarea.
- Gestionar estados: `PENDING`, `PROCESSING`, `COMPLETED` y `FAILED`.
- Reintentar automáticamente los mensajes fallidos.
- Enviar los mensajes que agotan los reintentos a una Dead Letter Queue (DLQ).
- Guardar las tareas y sus resultados en PostgreSQL.
- Utilizar Redis para almacenar temporalmente las consultas frecuentes.
- Invalidar la caché cuando cambia el estado de una tarea.
- Validar las solicitudes y manejar errores de procesamiento.
- Contar con pruebas unitarias, pruebas de controlador y pruebas de integración.

## Tecnologías

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring AMQP
- PostgreSQL
- RabbitMQ
- Redis
- Maven
- Docker Compose
- JUnit 5, Mockito y Awaitility

## Arquitectura

```text
Cliente HTTP
    |
    v
API REST (Spring Boot)
    |
    v
PostgreSQL <--- Registro de la tarea
    |
    v
RabbitMQ <--- Mensaje con el ID de la tarea
    |
    v
Consumidor asíncrono
    |
    v
Procesamiento del reporte
    |
    v
PostgreSQL <--- Estado y resultado final

Redis <--- Caché de consultas de tareas

RabbitMQ ---> Reintentos ---> Dead Letter Queue
                                |
                                v
                       Registro del fallo
```

## Requisitos previos

Antes de ejecutar el proyecto, instala:

- JDK 21.
- Docker Desktop con Docker Compose.
- Git, si deseas clonar el repositorio.

El proyecto incluye Maven Wrapper, por lo que no necesitas instalar Maven por separado.

## Instalación y ejecución

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd async-processing-api
```

### 2. Iniciar los servicios

Desde la raíz del proyecto, inicia PostgreSQL, RabbitMQ y Redis mediante Docker Compose:

```bash
docker compose up -d
```

Verifica que los contenedores estén funcionando:

```bash
docker compose ps
```

### 3. Configurar las variables de entorno

La aplicación permite configurar los servicios mediante estas variables:

| Variable | Valor predeterminado |
|---|---|
| `APP_NAME` | `async-processing-api` |
| `PORT` | `8081` |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `async_processing` |
| `DB_USER` | `postgres` |
| `DB_PASSWORD` | `admin` |
| `DB_SSL_MODE` | `disable` |
| `RABBITMQ_HOST` | `localhost` |
| `RABBITMQ_PORT` | `5672` |
| `RABBITMQ_USER` | `async_user` |
| `RABBITMQ_PASSWORD` | `admin` |
| `REDIS_HOST` | `localhost` |
| `REDIS_PORT` | `6379` |

Estos son los valores predeterminados de la aplicación. Comprueba que coincidan con la configuración de tu archivo Docker Compose.

**Importante:** las credenciales anteriores son únicamente valores locales de desarrollo. No publiques contraseñas reales ni credenciales de producción en el repositorio.

### 4. Ejecutar la aplicación

En Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

La API estará disponible en:

`http://localhost:8081`

## Endpoints

### Crear una tarea

`POST /api/v1/tasks`

Solicitud:

```json
{
  "description": "Generar resumen del informe mensual"
}
```

La respuesta esperada es `201 Created`, junto con la ubicación del recurso creado.

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

Los valores de fecha e identificador son ilustrativos. El estado puede cambiar rápidamente porque el procesamiento es asíncrono.

### Consultar una tarea

`GET /api/v1/tasks/{id}`

Ejemplo:

```http
GET http://localhost:8081/api/v1/tasks/1
```

La respuesta incluye el estado actual, el resultado generado y el mensaje de error, si corresponde.

### Listar tareas

`GET /api/v1/tasks?page=0&size=10`

Permite consultar las tareas mediante paginación.

- `page`: número de página, comenzando en `0`.
- `size`: cantidad de elementos por página, entre `1` y `100`.

Los resultados se ordenan por fecha de creación descendente.

## Estados de una tarea

| Estado | Descripción |
|---|---|
| `PENDING` | La tarea está pendiente de procesamiento. |
| `PROCESSING` | El consumidor está procesando la tarea. |
| `COMPLETED` | El procesamiento terminó correctamente. |
| `FAILED` | El procesamiento falló después de los intentos configurados. |

El procesamiento ocurre en segundo plano. Por eso, una tarea creada no necesariamente estará completada cuando el cliente reciba la respuesta inicial.

## Manejo de errores y reintentos

RabbitMQ utiliza reintentos automáticos cuando el consumidor falla al procesar un mensaje.

La configuración actual contempla hasta tres intentos totales, incluyendo el inicial. Cuando se agotan los intentos, el mensaje se dirige a una Dead Letter Queue (DLQ), donde se gestiona el fallo y se actualiza el estado de la tarea.

## Caché con Redis

Las consultas individuales de tareas utilizan Redis para reducir las lecturas repetidas a PostgreSQL.

La caché tiene una duración de cinco minutos. Cuando una tarea cambia de estado, la aplicación invalida su entrada de caché después de confirmar la transacción correspondiente.

PostgreSQL continúa siendo la fuente persistente de los datos.

## Pruebas

Ejecutar todas las pruebas:

```powershell
.\mvnw.cmd test
```

Ejecutar las pruebas de integración del flujo asíncrono:

```powershell
.\mvnw.cmd -Dtest=TaskFlowIntegrationIT test
```

Ejecutar las pruebas del consumidor:

```powershell
.\mvnw.cmd -Dtest=TaskMessageConsumerTest test
```

Las pruebas de integración requieren que los servicios externos utilizados por ellas estén disponibles y correctamente configurados.

## Detener los servicios

Para detener los contenedores:

```bash
docker compose down
```

Este comando detiene y elimina los contenedores del proyecto, pero normalmente conserva los datos almacenados en volúmenes nombrados. Para eliminar también esos volúmenes, revisa primero la configuración de Docker Compose y utiliza `docker compose down -v` solo si realmente deseas borrar los datos locales.

## Objetivo del proyecto

Este proyecto busca demostrar conocimientos prácticos de desarrollo backend con Java y Spring Boot, integración de mensajería asíncrona, persistencia relacional, caché, gestión de fallos y pruebas automatizadas.