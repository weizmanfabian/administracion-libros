
# Sistema de Gestión de Libros

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.oracle.com/java/technologies/javase/jdk21-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.10-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-28.0.1-blue.svg)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.1-336791.svg)](https://www.postgresql.org/)

Sistema de Gestión de Libros y Autores, diseñado para gestionar libros y sus autores mediante una API RESTful.

## Descripción

Este proyecto implementa un sistema backend para la gestión de libros y autores, utilizando una **arquitectura hexagonal** (Ports and Adapters). La aplicación permite realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) sobre libros y autores, con persistencia en una base de datos PostgreSQL. Está construida con Spring Boot y se despliega utilizando Docker para facilitar el desarrollo y la producción.

El diseño sigue los principios de la arquitectura hexagonal para garantizar:
- **Modularidad**: Separación clara entre la lógica de negocio, los adaptadores de entrada (controladores REST) y los adaptadores de salida (acceso a datos).
- **Testabilidad**: La lógica de negocio es independiente de la infraestructura, lo que facilita las pruebas unitarias.
- **Mantenibilidad**: Cambios en la infraestructura (como cambiar la base de datos) no afectan la lógica de negocio.

## Tecnologías Utilizadas

### Backend
- **Java 21**: Aprovecha las últimas características del lenguaje, como Records y Pattern Matching.
- **Spring Boot 3.3.10**: Framework principal con soporte para:
  - **Spring Web**: Para la creación de APIs RESTful.
  - **Spring Validation**: Para la validación de datos.
  - **Spring JDBC**: Para el acceso a datos mediante `JdbcTemplate` y procedimientos almacenados.
- **PostgreSQL 16.1**: Base de datos relacional robusta y escalable.
- **Docker Compose**: Orquestación de contenedores para desarrollo y producción.
- **Arquitectura Hexagonal**: Implementación modular con comunicación a través de API REST.

### Herramientas Recomendadas
- **IDE Backend**: IntelliJ IDEA 2024.3.5 o versiones posteriores.
- **Gestión de Base de Datos**: DBeaver 23.2.x, DataGrip, o PgAdmin 4.3.x.
- **Pruebas de API**: Postman (para importar y probar la colección de solicitudes).

## Diagrama de Arquitectura

```plaintext
+---------------------+       +---------------------+       +---------------------+
| API (Controladores) | ----> | Application (Casos  | ----> | Infraestructura     |
| (Adaptadores de     |       | de Uso - Lógica de  |       | (Adapters - Acceso  |
| Entrada)            |       | Negocio)            |       | a Datos)            |
+---------------------+       +---------------------+       +---------------------+
```

## Estructura del Proyecto

A continuación se presenta la estructura de carpetas del proyecto `Biblioteca`, que sigue el patrón de arquitectura hexagonal para garantizar modularidad, testabilidad y mantenibilidad.

```
Biblioteca/
├── db/                                    # Scripts SQL para la base de datos (PostgreSQL)
│   └── (archivos SQL para procedimientos almacenados y funciones, e.g., uspLibroInsert.sql)
├── src/                                   # Código fuente del proyecto
│   ├── main/                              # Código principal
│   │   ├── java/                          # Código Java
│   │   │   └── com/weiz/Biblioteca/       # Paquete raíz del proyecto
│   │   │       ├── api/                   # Adaptadores de entrada (controladores, DTOs)
│   │   │       │   ├── controllers/       # Controladores REST (adaptadores de entrada)
│   │   │       │   │   ├── AutorController.java
│   │   │       │   │   ├── LibroController.java
│   │   │       │   │   └── TestController.java
│   │   │       │   ├── requests/          # DTOs de entrada (solicitudes)
│   │   │       │   │   ├── AutorRequest.java
│   │   │       │   │   └── LibroRequest.java
│   │   │       │   └── responses/         # DTOs de salida (respuestas)
│   │   │       │       ├── errors/        # Clases para manejar errores en respuestas
│   │   │       │       │   ├── BaseErrorResponse.java
│   │   │       │       │   └── ErrorResponse.java
│   │   │       │       ├── AutorResponse.java
│   │   │       │       └── LibroResponse.java
│   │   │       ├── application/           # Núcleo de la aplicación (lógica de negocio)
│   │   │       │   ├── port/              # Puertos (interfaces)
│   │   │       │   │   ├── in/            # Puertos de entrada (casos de uso)
│   │   │       │   │   │   ├── AutorUseCase.java
│   │   │       │   │   │   └── LibroUseCase.java
│   │   │       │   │   └── out/           # Puertos de salida (acceso a datos)
│   │   │       │   │       ├── AutorPort.java
│   │   │       │   │       └── LibroPort.java
│   │   │       │   └── service/           # Implementaciones de la lógica de negocio
│   │   │       │       ├── AutorService.java
│   │   │       │       └── LibroService.java
│   │   │       ├── domain/                # Dominio de la aplicación
│   │   │       │   ├── entities/          # Entidades del dominio
│   │   │       │   │   ├── AutorEntity.java
│   │   │       │   │   └── LibroEntity.java
│   │   │       │   └── repositories/      # Repositorios (no usados actualmente)
│   │   │       │       ├── AutorRepository.java
│   │   │       │       └── LibroRepository.java
│   │   │       ├── infraestructure/       # Adaptadores de salida (infraestructura)
│   │   │       │   └── adapter/           # Implementaciones de puertos de salida
│   │   │       │       ├── AutorAdapter.java
│   │   │       │       └── LibroAdapter.java
│   │   │       └── util/                  # Utilidades generales
│   │   │           ├── enums/             # Enums para constantes
│   │   │           │   └── StoredProcedure.java
│   │   │           ├── errors/            # Excepciones personalizadas
│   │   │           │   └── Exceptions/
│   │   │           │       ├── CustomException.java
│   │   │           │       └── IdNotFoundException.java
│   │   │           ├── DataAccessUtils.java
│   │   │           └── ErrorMessageExtractor.java
│   │   └── resources/                     # Recursos (configuraciones)
│   │       └── application.properties    # Configuración de la aplicación (e.g., conexión a BD)
│   └── test/                              # Pruebas
│       └── java/
│           └── com/weiz/Biblioteca/
│               └── util/
│                   └── ErrorMessageExtractorTest.java
├── target/                                # Archivos generados por Maven (compilación)
├── .env                                   # Variables de entorno
├── .gitattributes                         # Configuración de Git
├── .gitignore                             # Archivos ignorados por Git
├── Biblioteca.postman_collection.json     # Colección de Postman para pruebas de API
├── docker-compose.yml                     # Configuración de Docker Compose
└── Dockerfile                             # Configuración de Docker para el contenedor
```

### Descripción de los Paquetes

- **`api`**: Contiene los adaptadores de entrada (controladores REST) y los DTOs para solicitudes (`requests`) y respuestas (`responses`).
  - **`controllers`**: Controladores REST que manejan las solicitudes HTTP y delegan a los casos de uso.
  - **`requests`**: DTOs para recibir datos de las solicitudes HTTP.
  - **`responses`**: DTOs para enviar datos en las respuestas HTTP, incluyendo manejo de errores.

- **`application`**: Núcleo de la aplicación, donde reside la lógica de negocio.
  - **`port/in`**: Puertos de entrada (interfaces que definen los casos de uso).
  - **`port/out`**: Puertos de salida (interfaces que definen el acceso a datos).
  - **`service`**: Implementaciones de los casos de uso, que contienen la lógica de negocio.

- **`domain`**: Representa el dominio de la aplicación.
  - **`entities`**: Entidades del dominio (modelos de datos).
  - **`repositories`**: Repositorios de Spring Data JPA (no usados actualmente, ya que se usa `JdbcTemplate`).

- **`infraestructure`**: Adaptadores de salida (infraestructura).
  - **`adapter`**: Implementaciones de los puertos de salida, que conectan la lógica de negocio con la base de datos (usando `JdbcTemplate` y procedimientos almacenados).

- **`util`**: Utilidades generales.
  - **`enums`**: Enums para constantes, como los nombres de los procedimientos almacenados.
  - **`errors/Exceptions`**: Excepciones personalizadas.
  - **`DataAccessUtils`**: Utilidad para manejar operaciones de acceso a datos y excepciones.
  - **`ErrorMessageExtractor`**: Utilidad para mapear mensajes de error de excepciones.

- **`resources`**: Archivos de configuración, como `application.properties` para la conexión a la base de datos.

- **`test`**: Pruebas unitarias y de integración.
  - Actualmente incluye pruebas para `ErrorMessageExtractor`. Se recomienda añadir pruebas para los adaptadores y servicios.

## Requisitos Previos

Antes de comenzar, asegúrate de tener instalados los siguientes programas:

- **Java 21**: Descarga e instala desde [aquí](https://www.oracle.com/java/technologies/javase/jdk21-downloads.html).
- **Docker** (versión 28.0.1 o superior) y **Docker Compose**.
- **Git** (para clonar el repositorio).
- **Maven** (para construir el proyecto localmente, si no usas Docker).
- Opcional: **IntelliJ IDEA** (para desarrollo local sin Docker).
- Opcional: **Postman** (para probar la API).

## Configuración del Entorno de Desarrollo

### 1. Clonar el Repositorio

Clona el repositorio e ingresa al directorio del proyecto:

```bash
git clone https://github.com/weizmanfabian/administracion-libros.git
cd administracion-libros/Biblioteca
```

### 2. Configuración del Backend

#### 2.1. Verificar el Archivo `.env`

En el directorio `Biblioteca/`, encontrarás un archivo `.env` que contiene la configuración de la aplicación y la base de datos, incluyendo los puertos internos (`IN`) y los expuestos (`OUT`):

```properties
APP_NAME=Biblioteca
APP_CONTEXT_PATH=/biblioteca
APP_PORT_IN=8080
APP_PORT_OUT=8088

DB_NAME=biblioteca
DB_HOST=localhost
DB_USER=weizman
DB_PASSWORD=YourStrong#Passw0rd
DB_PORT_IN=5432
DB_PORT_OUT=5438
```

**Nota**: Asegúrate de que el puerto `5438` (`DB_PORT_OUT`) no esté ocupado en tu máquina local, ya que será usado para mapear el puerto interno de PostgreSQL (`5432`).

#### 2.2. Construir y Ejecutar los Contenedores con Docker Compose

Desde el directorio `Biblioteca/`, ejecuta el siguiente comando para construir y levantar los contenedores:

```bash
docker-compose up --build
```

Esto hará lo siguiente:
- Construirá la imagen de la aplicación Spring Boot usando el `Dockerfile`.
- Iniciará un contenedor de PostgreSQL (`db`) con la base de datos inicializada mediante los scripts SQL en `db/`.
- Iniciará el contenedor de la aplicación (`app`) y lo conectará a la base de datos.

**Nota**: Si deseas detener y eliminar los contenedores después de probar la aplicación, ejecuta:

```bash
docker-compose down
```

#### 2.3. Acceder a la Aplicación

Una vez que los contenedores estén en ejecución, la aplicación estará disponible en:

- **API REST**: `http://localhost:8088/biblioteca`
- **Base de Datos**: Conéctate a PostgreSQL en `localhost:5438` usando las credenciales definidas en `.env`:
  - Host: `localhost`
  - Puerto: `5438`
  - Usuario: `weizman`
  - Contraseña: `YourStrong#Passw0rd`
  - Base de datos: `biblioteca`

### 3. Probar la API

#### 3.1. Importar la Colección de Postman

1. Abre Postman.
2. Importa el archivo `Biblioteca.postman_collection.json` que se encuentra en el directorio `Biblioteca/`.
3. Ejecuta las solicitudes de la colección para probar los endpoints de la API.

#### 3.2. Endpoints Principales

##### **Libros**
- **POST /biblioteca/libros**: Crear un libro.
  - **Ejemplo de solicitud**:
    ```json
    {
        "titulo": "El caballero de la armadura oxidada",
        "anioPublicacion": 2005,
        "autorId": 1
    }
    ```
- **GET /biblioteca/libros/{id}**: Obtener un libro por ID.
- **GET /biblioteca/libros**: Listar todos los libros.
- **PUT /biblioteca/libros/{id}**: Actualizar un libro.
- **DELETE /biblioteca/libros/{id}**: Eliminar un libro.

##### **Autores**
- **POST /biblioteca/autores**: Crear un autor.
  - **Ejemplo de solicitud**:
    ```json
    {
        "nombre": "Robert",
        "apellido": "Fisher",
        "nacionalidad": "Estadounidense"
    }
    ```
- **GET /biblioteca/autores/{id}**: Obtener un autor por ID.
- **GET /biblioteca/autores**: Listar todos los autores.
- **PUT /biblioteca/autores/{id}**: Actualizar un autor.
- **DELETE /biblioteca/autores/{id}**: Eliminar un autor.

### 4. Ejecución Local sin Docker (Opcional)

Si prefieres ejecutar la aplicación localmente sin Docker:

1. Asegúrate de tener PostgreSQL instalado y corriendo en `localhost:5438` (o ajusta `DB_PORT_OUT` en `.env` según tu configuración).
2. Ejecuta los scripts SQL en `db/` para inicializar la base de datos.
3. Asegúrate de que `application.properties` esté configurado correctamente con las credenciales de la base de datos:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5438/biblioteca
   spring.datasource.username=weizman
   spring.datasource.password=YourStrong#Passw0rd
   ```
4. Construye el proyecto con Maven:
   ```bash
   mvn clean install
   ```
5. Ejecuta la clase principal `BibliotecaApplication` desde tu IDE (o con Maven: `mvn spring-boot:run`).
6. La aplicación estará disponible en `http://localhost:8088/biblioteca`.

## Notas Adicionales

- **Persistencia de Datos**: Los scripts SQL en `db/` inicializan el esquema, datos, funciones y procedimientos almacenados en la base de datos PostgreSQL al iniciar el contenedor `db`.
- **Depuración**: Si encuentras problemas, revisa los logs de los contenedores con:
  ```bash
  docker-compose logs app
  docker-compose logs db
  ```
- **Entorno de Producción**: Para un entorno de producción, considera:
  - Usar un volumen persistente para los datos de PostgreSQL.
  - Configurar una contraseña segura para la base de datos en `.env`.
  - Ajustar los límites de recursos en `docker-compose.yml`.

## Pruebas

El proyecto incluye pruebas unitarias para algunas utilidades, como `ErrorMessageExtractor`. Para ejecutar las pruebas:

```bash
mvn test
```


## Contribuciones

Si deseas contribuir al proyecto, sigue estos pasos:

1. Haz un fork del repositorio.
2. Crea una nueva rama (`git checkout -b feature/nueva-funcionalidad`).
3. Realiza tus cambios y haz commit (`git commit -m "Añadir nueva funcionalidad"`).
4. Sube tus cambios (`git push origin feature/nueva-funcionalidad`).
5. Crea un Pull Request en GitHub.

## Licencia

Este proyecto está licenciado bajo la [MIT License](LICENSE).
