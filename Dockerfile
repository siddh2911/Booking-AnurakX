# Use a base image with Java 17, suitable for Spring Boot applications
FROM eclipse-temurin:17-jdk-focal

# Set the working directory in the container
WORKDIR /app

# Copy the Gradle wrapper files and the project files
COPY gradlew .
COPY .gradle .gradle
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# Make the Gradle wrapper executable
RUN chmod +x gradlew

# Build the Spring Boot application, creating an executable JAR
# The build output (JAR) will be in build/libs/
RUN ./gradlew bootJar

# Expose the port that the Spring Boot application runs on (default is 8080)
EXPOSE 8080

# Command to run the application
# Assumes the JAR name is booking-system-0.0.1-SNAPSHOT.jar based on the folder structure
ENTRYPOINT ["java", "-jar", "build/libs/booking-system-0.0.1-SNAPSHOT.jar"]
