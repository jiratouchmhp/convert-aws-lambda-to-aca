# Dockerfile for Azure Container Apps
# Expects the JAR to be built on the host first using: mvn clean package

# Use distroless for security and lean container
FROM gcr.io/distroless/java17-debian11:nonroot

# Copy the pre-built JAR file
COPY target/*.jar /app/application.jar

# Expose the port that Spring Boot runs on
EXPOSE 8080

# Set the entry point
ENTRYPOINT ["java", "-jar", "/app/application.jar"]