# Multi-stage Dockerfile for Mithaq Spring Boot application

# Stage 1: Build stage with Maven
FROM maven:3.9.6 AS build

# Set the working directory
WORKDIR /app

# Copy the Maven project files
COPY pom.xml .
COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn

# Copy the source code
COPY src ./src

# Build the application with Maven
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jdk

# Set the working directory inside the container
WORKDIR /app

# Copy the built Spring Boot JAR from the build stage
COPY --from=build /app/target/*.jar /app/mithaq.jar

# Expose the port your Spring Boot application listens on (3000 inside container)
EXPOSE 3000

# Define the command to run your Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/mithaq.jar"]