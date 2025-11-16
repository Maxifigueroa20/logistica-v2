# Logística API – Grupo 02 POO2025
API REST desarrollada para la gestión integral de un sistema logístico: clientes, paquetes, vehículos, rutas y envíos.
Incluye validaciones de negocio, seguimiento por código de tracking y documentación interactiva.


## Descripción del Proyecto

Este proyecto implementa una API para administrar el ciclo completo de un envío:
-   Registro y búsqueda de clientes (remitentes/destinatarios).
-   Alta y consulta de paquetes frágiles y refrigerados.
-   Manejo de vehículos, incluyendo capacidad y temperatura.
-   Gestión de rutas logísticas y asignación de envíos.
-   Creación, actualización de estado y seguimiento de envíos.
-   Historial completo de cambios de estado.

Toda la API está documentada con Swagger/OpenAPI y lista para ser consumida desde Postman o cualquier cliente HTTP.

## Stack Utilizado

-   Java 21
-   Spring Boot 3.5.6
-   Spring Web / Spring Data JPA / Validation
-   MySQL 8
-   Lombok
-   OpenAPI 3 (springdoc-openapi)
-   Perfil default y perfil test para pruebas automatizadas
-   Semilla de datos para entorno demo (app.demo=true)

## Cómo ejecutar el proyecto
Configurar la base de datos MySQL:

CREATE DATABASE logistica;
CREATE DATABASE logistica_test;


Verificar usuario/contraseña configurados en application.yml.

Ejecutar el proyecto con:

./mvnw spring-boot:run


o desde un IDE (Spring Boot Run).

El proyecto se inicia por defecto en http://localhost:8080

## Documentación de la API
La documentación se genera automáticamente con Swagger.

👉 URL principal:
http://localhost:8080

(la aplicación redirige automáticamente a /swagger-ui.html)

Incluye descripción detallada, parámetros, respuestas y ejemplos para cada endpoint.

## Colección de Postman
En la raíz del proyecto se incluye:

*   Logística API - Grupo 02.postman_collection.json

Esta colección contiene todas las rutas agrupadas y listas para ser ejecutadas.

## Equipo de Desarrollo
*   Gabriel Flores
*   Maximiliano Figueroa
