# Etapa 1: construir el JAR con Maven
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copiar archivos de Maven wrapper (si los tienes)
COPY mvnw .
COPY .mvn .mvn

# Copiar pom.xml y descargar dependencias
COPY pom.xml .
RUN ./mvnw -q -DskipTests dependency:go-offline || mvn -q -DskipTests dependency:go-offline

# Copiar el código fuente
COPY src src

# Construir el JAR
RUN ./mvnw -q -DskipTests clean package || mvn -q -DskipTests clean package

# Etapa 2: imagen liviana para correr la app
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiar el JAR construido
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Puerto que expone Spring Boot
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
