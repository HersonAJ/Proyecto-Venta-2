# Etapa 1: build
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace
COPY ventas/pom.xml .
COPY ventas/src ./src
COPY ventas/mvnw .
COPY ventas/.mvn ./.mvn
RUN mvn clean package -DskipTests

# Etapa 2: runtime
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /workspace/target/ventas-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

