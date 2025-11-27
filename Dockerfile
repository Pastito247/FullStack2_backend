# Etapa 1: construir el JAR con Maven (imagen ya trae Maven + JDK 17)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copiamos el pom primero para aprovechar cache
COPY pom.xml .

# Descargamos dependencias
RUN mvn -q -DskipTests dependency:go-offline

# Copiamos el código fuente
COPY src ./src

# Construimos el JAR
RUN mvn -q -DskipTests clean package

# Etapa 2: imagen liviana solo con JRE
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiamos el JAR desde la etapa de build
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar
# 👆 Aquí usamos el nombre correcto según tu pom.xml

# Exponemos el puerto de Spring Boot
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
