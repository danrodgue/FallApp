## Dockerfile para la API REST de FallApp (Spring Boot)
##
## Multi-stage build:
## 1) Imagen Maven para compilar el JAR
## 2) Imagen ligera con JRE para ejecutar la aplicación

# Etapa de compilación
FROM maven:3.9.8-eclipse-temurin-17 AS build

WORKDIR /app

# Copiamos el descriptor de dependencias primero para aprovechar la caché
COPY pom.xml .

# Copiamos el código fuente
COPY src ./src

# Compilamos el proyecto (sin tests para acelerar)
RUN mvn -B -DskipTests clean package

# Etapa de ejecución
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copiamos el JAR generado desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Puerto en el que expone Spring Boot (configurable vía env PORT)
EXPOSE 8080

# Variables de entorno típicas (pueden sobreescribirse en docker-compose / despliegue)
ENV SPRING_PROFILES_ACTIVE=docker

# Comando de arranque
ENTRYPOINT ["java","-jar","/app/app.jar"]

