# Start with an OpenJDK base image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built jar file into the container
COPY todomon-core/build/libs/todomon-core-0.0.1-SNAPSHOT.jar /app/webapp.jar

# Expose the port that the application will run on
EXPOSE 8080

# Run the jar file when the container starts
ENTRYPOINT ["java", "-jar", "/app/webapp.jar"]
