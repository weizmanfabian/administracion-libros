# Etapa 1: Construir la aplicación con Maven
FROM maven:3.9.9-eclipse-temurin-21 AS builder

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el archivo pom.xml y descargar las dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el resto del código fuente y construir la aplicación
COPY src ./src
RUN mvn package -DskipTests

# Etapa 2: Crear la imagen final para ejecutar la aplicación
FROM eclipse-temurin:21-jre-jammy

# Establecer el directorio de trabajo
WORKDIR /app

# Copiar el .jar generado desde la etapa de construcción
COPY --from=builder /app/target/*.jar app.jar

# Exponer el puerto (el puerto interno de la aplicación)
EXPOSE ${APP_PORT_IN}

# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]