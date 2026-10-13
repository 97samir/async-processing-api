# Async Processing API

API REST desarrollada con Java 21 y Spring Boot para crear, procesar y consultar tareas de forma asíncrona.

El proyecto utiliza **PostgreSQL** para persistir las tareas, **RabbitMQ** para gestionar el procesamiento en segundo plano y **Redis** para acelerar las consultas mediante caché. Incluye validación de solicitudes, manejo centralizado de errores, reintentos de procesamiento, una Dead Letter Queue (DLQ), pruebas automatizadas y documentación interactiva con OpenAPI.

## Tabla de contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura](#arquitectura)
- [Entorno de producción](#entorno-de-producción)
- [Requisitos previos](#requisitos-previos)
- [Instalación y ejecución local](#instalación-y-ejecución-local)
- [Documentación de la API](#documentación-de-la-api)
- [Endpoints](#endpoints)
- [Estados de las tareas](#estados-de-las-tareas)
- [Manejo de errores](#manejo-de-errores)
- [Procesamiento asíncrono y reintentos](#procesamiento-asíncrono-y-reintentos)
- [Caché con Redis](#caché-con-redis)
- [Pruebas automatizadas](#pruebas-automatizadas)
- [Detener los servicios](#detener-los-servicios)
- [Objetivo del proyecto](#objetivo-del-proyecto)

## Características

- Crear tareas mediante una API REST.
- Procesar tareas en segundo plano mediante RabbitMQ.
- Consultar el estado y el resultado de cada tarea.
- Gestionar los estados `PENDING`, `PROCESSING`, `COMPLETED` y `FAILED`.
- Reintentar automáticamente el procesamiento de mensajes fallidos.
- Enviar a una Dead Letter Queue (DLQ) los mensajes que agotan los reintentos configurados.
- Persistir las tareas y sus resultados en PostgreSQL.
- Almacenar temporalmente en Redis las consultas individuales.
- Invalidar la caché cuando cambia el estado de una tarea.
- Validar las solicitudes y centralizar el manejo de errores HTTP.
- Documentar los endpoints mediante Swagger UI y OpenAPI.
- Incluir pruebas unitarias, de controlador y de integración.

## Tecnologías

| Tecnología | Uso |
|---|---|
| Java 21 | Lenguaje de programación |
| Spring Boot | Desarrollo de la aplicación |
| Spring Web | Construcción de la API REST |
| Spring Data JPA | Persistencia y acceso a datos |
| Spring AMQP | Integración con RabbitMQ |
| PostgreSQL | Base de datos relacional |
| RabbitMQ | Mensajería y procesamiento asíncrono |
| Redis | Caché de consultas |
| Maven Wrapper | Compilación y gestión de dependencias |
| Docker Compose | Ejecución local de los servicios |
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
      Publicación del mensaje
              |
              v
           RabbitMQ
              |
              v
      Consumidor asíncrono
              |
              v
     Procesamiento del resumen
              |
              v
      Actualización de la tarea
              |
              v
          PostgreSQL

      Mensajes con reintentos agotados
                         |
                         v
                  Dead Letter Queue
                         |
                         v
                Gestión del fallo
```

El cliente recibe una respuesta sin esperar a que finalice el procesamiento de la tarea. RabbitMQ desacopla la recepción de solicitudes del trabajo en segundo plano, mientras que PostgreSQL conserva la información y Redis permite reutilizar temporalmente los resultados de consultas individuales.

## Entorno de producción

La API está desplegada en Render y cuenta con documentación interactiva disponible públicamente.

- **API y Swagger UI:** [https://async-processing-api.onrender.com/swagger-ui/index.html](https://async-processing-api.onrender.com/swagger-ui/index.html)
- **Especificación OpenAPI:** [https://async-processing-api.onrender.com/v3/api-docs](https://async-processing-api.onrender.com/v3/api-docs)

La aplicación utiliza servicios administrados para la infraestructura externa configurada en el entorno de producción. No se deben publicar en este repositorio contraseñas, cadenas de conexión ni otras credenciales.

> Nota: el acceso a Swagger confirma que la interfaz de documentación está disponible. Para confirmar el funcionamiento integral en producción, también se debe comprobar la creación y el procesamiento de tareas y la conexión con los servicios externos.

## Requisitos previos

Para ejecutar el proyecto localmente necesitas:

- JDK 21.
- Docker Desktop con Docker Compose.
- Git, si deseas clonar el repositorio.

No necesitas instalar Maven por separado, ya que el proyecto incluye Maven Wrapper.

## Instalación y ejecución local

### 1. Clonar el repositorio

```powershell
git clone https://github.com/97samir/async-processing-api.git
cd async-processing-api
```

### 2. Configurar las variables de entorno

Si el repositorio incluye `.env.example`, crea una copia para tu entorno local:

```powershell
Copy-Item .env.example .env
```

Completa las variables con valores de desarrollo que coincidan con la configuración de Docker Compose y de la aplicación. Revisa `application.yaml`, `application-docker.yml` y `docker-compose.yml` para identificar las variables requeridas por la configuración efectiva.

No subas el archivo `.env` ni credenciales reales a GitHub. Comprueba que `.env` esté incluido en `.gitignore`.

### 3. Iniciar los servicios

Desde la raíz del proyecto, ejecuta:

```powershell
docker compose up -d
```

Comprueba el estado de los contenedores:

```powershell
docker compose ps
```

Docker Compose se encarga de iniciar los servicios definidos en el archivo de configuración, incluidos PostgreSQL, RabbitMQ y Redis.

### 4. Ejecutar la aplicación

En Windows, desde la raíz del proyecto:

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación utiliza el puerto `8081` en la configuración local actual.

También puedes ejecutar la clase principal `AsyncProcessingApiApplication` desde IntelliJ IDEA.

## Documentación de la API

Con la aplicación local iniciada, abre:

- **Swagger UI local:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
- **OpenAPI JSON local:** [http://localhost:8081/v3/api-docs](http://localhost:8081/v3/api-docs)

Swagger UI permite consultar los endpoints, los parámetros, los esquemas de solicitud y respuesta y los códigos HTTP documentados. También permite enviar solicitudes manuales a la API.

## Endpoints

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/v1/tasks` | Crea una tarea de resumen |
| `GET` | `/api/v1/tasks/{id}` | Consulta una tarea por su identificador |
| `GET` | `/api/v1/tasks?page=0&size=10` | Lista tareas mediante paginación |

### Crear una tarea

**`POST /api/v1/tasks`**

Crea una tarea de tipo `REPORT_SUMMARY`. Si la solicitud se acepta correctamente, devuelve `201 Created`.

Ejemplo de solicitud:

```json
{
  "description": "Generar resumen del informe mensual"
}
```

La descripción es obligatoria y admite un máximo de 200 caracteres.

Ejemplo ilustrativo de respuesta:

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

El identificador y las fechas son ejemplos, no datos reales de producción. Debido al procesamiento asíncrono, el estado puede cambiar poco después de crear la tarea.

### Consultar una tarea

**`GET /api/v1/tasks/{id}`**

Ejemplo de solicitud local:

```http
GET http://localhost:8081/api/v1/tasks/1
```

La respuesta contiene el identificador, el tipo, el estado, la descripción, el resultado, el mensaje de error y las fechas de creación y actualización.

Si la tarea no existe, la API devuelve `404 Not Found`.

### Listar tareas

**`GET /api/v1/tasks?page=0&size=10`**

Permite consultar las tareas registradas mediante paginación.

- `page`: número de página; comienza en `0`.
- `size`: cantidad de elementos solicitados; admite valores entre `1` y `100`.

Las tareas se ordenan por fecha de creación descendente. La respuesta incluye el contenido de la página y sus metadatos, como la página actual, el tamaño, el total de elementos, el total de páginas y los indicadores de primera y última página.

## Estados de las tareas

| Estado | Descripción |
|---|---|
| `PENDING` | La tarea está pendiente de procesamiento. |
| `PROCESSING` | El consumidor está procesando la tarea. |
| `COMPLETED` | El procesamiento terminó correctamente. |
| `FAILED` | El procesamiento terminó con error después de los intentos configurados. |

Crear una tarea no significa que su procesamiento haya terminado. Consulta su estado mediante el endpoint de consulta para conocer el resultado final.

## Manejo de errores

La API utiliza un manejador global para devolver respuestas de error con una estructura uniforme.

Los campos de la respuesta de error son:

| Campo | Descripción |
|---|---|
| `status` | Código de estado HTTP |
| `error` | Descripción del estado HTTP |
| `message` | Mensaje que explica el problema |
| `path` | Ruta donde ocurrió el error |
| `timestamp` | Fecha y hora del error |

Entre los principales códigos HTTP utilizados se encuentran:

- `200 OK`: consulta realizada correctamente.
- `201 Created`: tarea creada correctamente.
- `400 Bad Request`: solicitud inválida o parámetros incorrectos.
- `404 Not Found`: no existe una tarea con el identificador solicitado.

## Procesamiento asíncrono y reintentos

RabbitMQ permite desacoplar la recepción de solicitudes del procesamiento de las tareas. De este modo, la API puede aceptar una solicitud sin esperar a que termine el trabajo en segundo plano.

La configuración contempla hasta **tres intentos totales**, incluido el intento inicial. Si el consumidor agota los intentos configurados, el mensaje se envía a una Dead Letter Queue (DLQ) y se gestiona el fallo actualizando el estado de la tarea.

Para facilitar la comprobación del flujo de error en el entorno de desarrollo, el proyecto contempla una descripción que contiene `[PRUEBA-FALLO]` como mecanismo de fallo simulado.

## Caché con Redis

Redis almacena temporalmente las respuestas de las consultas individuales para reducir lecturas repetidas a PostgreSQL.

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

Las pruebas que necesitan servicios externos requieren que dichos servicios estén disponibles y correctamente configurados.

## Detener los servicios

Para detener y eliminar los contenedores:

```powershell
docker compose down
```

Los volúmenes nombrados normalmente conservan los datos. Si también deseas eliminar los datos locales persistidos, puedes ejecutar:

```powershell
docker compose down -v
```

**Precaución:** `docker compose down -v` puede eliminar los datos guardados en los volúmenes del entorno local. Úsalo únicamente cuando estés seguro de que no necesitas conservarlos.

## Objetivo del proyecto

Este proyecto forma parte de mi portafolio de desarrollo backend y busca demostrar conocimientos prácticos en Java, Spring Boot, APIs REST, persistencia relacional, mensajería asíncrona, caché, manejo de errores y pruebas automatizadas.

El objetivo es construir una API organizada y mantenible que permita procesar tareas en segundo plano y consultar sus resultados de forma fiable.
