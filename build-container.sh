#!/bin/bash

# Build script for Azure Container Apps deployment
# This script builds the Spring Boot application and creates a Docker image

set -e

echo "Building Spring Boot application..."
mvn clean package -DskipTests

echo "Building Docker image..."
docker build -t springboot-azure-app:latest .

echo "Build completed successfully!"
echo "To run locally: docker run -p 8080:8080 springboot-azure-app:latest"
echo "To push to Azure Container Registry (ACR):"
echo "  docker tag springboot-azure-app:latest <your-acr-name>.azurecr.io/springboot-azure-app:latest"
echo "  docker push <your-acr-name>.azurecr.io/springboot-azure-app:latest"