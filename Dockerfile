# Stage 2: build do backend (com o frontend já nos resources)
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY pom.xml ./
COPY src/ ./src/
RUN mvn package -DskipTests

# Stage 3: imagem final de runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
