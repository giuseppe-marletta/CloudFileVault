# This Dockerfile is used to build a Docker image for the notification-service application.
# It uses the OpenJDK 21 base image with Alpine Linux for a lightweight container.
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory
WORKDIR /app

# Copy the JAR file into the container
COPY target/notification-service-0.0.1-SNAPSHOT.jar notification-service.jar

EXPOSE 8080

ENTRYPOINT [ "java", "-jar", "notification-service.jar" ]
