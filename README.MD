# Sistema de Gestión de Libros

[![Java](https://img.shields.io/badge/Java-21-red.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.10-green.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-28.0.1-blue.svg)](https://www.docker.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.1-336791.svg)](https://www.postgresql.org/)

Sistema de Gestión de libros y autores.

## Estructura del Proyecto

```
Biblioteca/
├── db/
│   └── sql/
│       └── functions/
│       └── storeProcedure/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/weiz/Biblioteca/
│   │   │       ├── api/
│   │   │       │   ├── controllers/
│   │   │       │   │   └── errorHandler/
│   │   │       │   ├── requests/
│   │   │       │   └── responses/
│   │   │       │       └── errors/
│   │   │       ├── domain/
│   │   │       │   ├── entities/
│   │   │       │   └── repositories/
│   │   │       ├── infraestructure/
│   │   │       │   ├── abstracts/
│   │   │       │   └── services/
│   │   │       └── util/
│   │   │           └── Exceptions/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── docker-compose.yml
└── pom.xml
```

### Backend
- **Java 21**: Aprovechando las últimas características del lenguaje como Records, Pattern Matching y Sealed Classes
- **Spring Boot 3.3.10**: Framework principal con soporte para:
  - Spring Data JPA para persistencia de datos
  - Spring Validation para validación de datos
- **PostgreSQL 16.1**: Sistema de gestión de base de datos robusto y escalable
- **Arquitectura de Microservicios**: Implementación de una arquitectura de microservicios, cada uno con su propia responsabilidad y comunicación a través de API REST
- **Docker compose**: Orquestación de contenedores para desarrollo y producción


### IDEs Recomendados
- **Backend**: IntelliJ IDEA 2024.3.5 o sus versiones posteriores
- **Base de Datos**: DBeaver 23.2.x, DataGrip o PgAdmin 4.3.x

## Configuración del Entorno de Desarrollo

### 1. Clonar el Repositorio e ingresamos al directorio del proyecto
```bash
git clone https://github.com/weizmanfabian/administracion-libros.git
cd administracion-libros
```

### 2. Configuración del Backend

#### 2.1. Configuración de la Base de Datos
Crear un contenedor de PostgreSQL con Docker y ejecutar
```bash
cd Biblioteca
docker-compose up -d
```

Nota: Cuando termine de probar la aplicación y desee remover el contenedor, ejecutar `docker-compose down`


El archivo `docker-compose.yml` incluye:
- PostgreSQL 16.1 con persistencia de datos
- Volúmenes para persistencia de datos y datos iniciales
- Variables de entorno configuradas para desarrollo

#### 2.2. Credenciales de Base de Datos
```properties
Host: localhost
Puerto: 5438
Usuario: weizman
Contraseña: YourStrong#Passw0rd
Base de datos: biblioteca
```

#### 2.3. Ejecución del Backend
```bash
mvn clean install
mvn spring-boot:run
```
NOTA: también lo puede abrir primero en un entorno de desarrollo como **IntelliJ IDEA** y lo ejecuta desde ahí.

La aplicación estará disponible en:
- API REST: `http://localhost:8088/biblioteca`

#### 2.3.1. Importar pruebas de Postman (Opcional)
 - Abrir Postman e importar `Biblioteca.postman_collection.json` que se encuentra en directorio `./Biblioteca` y ejecutar pruebas de la api.
