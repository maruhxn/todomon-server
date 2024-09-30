# Start with an OpenJDK base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built jar file into the container
COPY todomon-core/build/libs/todomon-app.jar /app/todomon-app.jar

# Expose the port that the application will run on
EXPOSE 8080

# Run the jar file when the container starts with Pinpoint agent
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/todomon-app.jar"]
