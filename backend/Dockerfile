# Stage 1: Build the Spring Boot application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
# Copy the backend files into the container
COPY . .
# Build the jar file while skipping tests for a faster deployment
RUN mvn clean package -DskipTests

# Stage 2: Run the application using a lightweight Java Runtime
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
# Copy the compiled jar from the build stage
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]