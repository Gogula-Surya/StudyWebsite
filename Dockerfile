# Use official OpenJDK 17 image from Docker Hub
FROM openjdk:17-jdk-slim

# Set working directory inside the container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/login-0.0.1-SNAPSHOT.jar app.jar

# Expose port 8080 (default for Spring Boot)
EXPOSE 20060

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
