# Azure Container Apps Deployment Guide

This guide provides complete instructions for deploying the migrated Product Service to Azure Container Apps.

## Prerequisites

- Azure CLI installed and authenticated
- Docker installed for local builds
- Azure subscription with appropriate permissions
- Azure Container Registry (ACR) or access to container registry

## Architecture Overview

```
Internet → Azure Container Apps → Azure Cosmos DB
         ↓
    Application Insights (Monitoring)
```

## Step 1: Create Azure Resources

### 1.1 Create Resource Group
```bash
az group create \
  --name rg-product-service \
  --location eastus
```

### 1.2 Create Azure Container Registry (Optional)
```bash
az acr create \
  --resource-group rg-product-service \
  --name acrproductservice \
  --sku Basic \
  --admin-enabled true
```

### 1.3 Create Azure Cosmos DB Account
```bash
# Create Cosmos DB account
az cosmosdb create \
  --name cosmos-product-service \
  --resource-group rg-product-service \
  --default-consistency-level Session \
  --locations regionName=eastus failoverPriority=0 isZoneRedundant=false

# Create database and container
az cosmosdb sql database create \
  --account-name cosmos-product-service \
  --resource-group rg-product-service \
  --name productdb

az cosmosdb sql container create \
  --account-name cosmos-product-service \
  --resource-group rg-product-service \
  --database-name productdb \
  --name products \
  --partition-key-path "/id" \
  --throughput 400
```

### 1.4 Create Application Insights
```bash
az monitor app-insights component create \
  --app product-service-insights \
  --location eastus \
  --resource-group rg-product-service \
  --application-type web
```

### 1.5 Create Container Apps Environment
```bash
az containerapp env create \
  --name env-product-service \
  --resource-group rg-product-service \
  --location eastus
```

## Step 2: Build and Push Container Image

### 2.1 Build Application
```bash
# Build the JAR file
mvn clean package -DskipTests

# Build Docker image
docker build -t product-service:latest .
```

### 2.2 Push to Azure Container Registry
```bash
# Tag for ACR
docker tag product-service:latest acrproductservice.azurecr.io/product-service:latest

# Login to ACR
az acr login --name acrproductservice

# Push image
docker push acrproductservice.azurecr.io/product-service:latest
```

## Step 3: Configure Environment Variables

### 3.1 Get Azure Cosmos DB Connection Details
```bash
# Get Cosmos DB endpoint
COSMOS_ENDPOINT=$(az cosmosdb show \
  --name cosmos-product-service \
  --resource-group rg-product-service \
  --query documentEndpoint -o tsv)

# Get Cosmos DB key
COSMOS_KEY=$(az cosmosdb keys list \
  --name cosmos-product-service \
  --resource-group rg-product-service \
  --query primaryMasterKey -o tsv)

echo "Cosmos Endpoint: $COSMOS_ENDPOINT"
echo "Cosmos Key: $COSMOS_KEY"
```

### 3.2 Get Application Insights Connection String
```bash
APP_INSIGHTS_CONNECTION_STRING=$(az monitor app-insights component show \
  --app product-service-insights \
  --resource-group rg-product-service \
  --query connectionString -o tsv)

echo "App Insights Connection String: $APP_INSIGHTS_CONNECTION_STRING"
```

## Step 4: Deploy Container App

### 4.1 Create Container App with Azure Cosmos DB
```bash
az containerapp create \
  --name product-service \
  --resource-group rg-product-service \
  --environment env-product-service \
  --image acrproductservice.azurecr.io/product-service:latest \
  --target-port 8080 \
  --ingress external \
  --registry-server acrproductservice.azurecr.io \
  --query properties.configuration.ingress.fqdn \
  --env-vars \
    SPRING_PROFILES_ACTIVE=azure \
    AZURE_COSMOS_ENDPOINT="$COSMOS_ENDPOINT" \
    AZURE_COSMOS_KEY="$COSMOS_KEY" \
    AZURE_COSMOS_DATABASE=productdb \
    AZURE_COSMOS_CONTAINER=products \
    AZURE_APPINSIGHTS_CONNECTION_STRING="$APP_INSIGHTS_CONNECTION_STRING" \
    JAVA_OPTS="-Xmx512m -Xms256m"
```

### 4.2 Alternative: Using Azure YAML Configuration
Create `containerapp.yaml`:
```yaml
location: eastus
resourceGroup: rg-product-service
type: Microsoft.App/containerApps
name: product-service
properties:
  environmentId: /subscriptions/{subscription-id}/resourceGroups/rg-product-service/providers/Microsoft.App/managedEnvironments/env-product-service
  configuration:
    ingress:
      external: true
      targetPort: 8080
      allowInsecure: false
    registries:
      - server: acrproductservice.azurecr.io
        username: acrproductservice
        passwordSecretRef: registry-password
    secrets:
      - name: registry-password
        value: "{acr-admin-password}"
      - name: cosmos-key
        value: "{cosmos-primary-key}"
      - name: app-insights-connection-string
        value: "{app-insights-connection-string}"
  template:
    containers:
      - name: product-service
        image: acrproductservice.azurecr.io/product-service:latest
        env:
          - name: SPRING_PROFILES_ACTIVE
            value: azure
          - name: AZURE_COSMOS_ENDPOINT
            value: "{cosmos-endpoint}"
          - name: AZURE_COSMOS_KEY
            secretRef: cosmos-key
          - name: AZURE_COSMOS_DATABASE
            value: productdb
          - name: AZURE_COSMOS_CONTAINER
            value: products
          - name: AZURE_APPINSIGHTS_CONNECTION_STRING
            secretRef: app-insights-connection-string
        resources:
          cpu: 0.5
          memory: 1Gi
        probes:
          - type: Liveness
            httpGet:
              path: /api/actuator/health/liveness
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          - type: Readiness
            httpGet:
              path: /api/actuator/health/readiness
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
    scale:
      minReplicas: 1
      maxReplicas: 5
      rules:
        - name: http-rule
          http:
            metadata:
              concurrentRequests: "10"
```

Deploy with YAML:
```bash
az containerapp create --resource-group rg-product-service --yaml containerapp.yaml
```

## Step 5: Verify Deployment

### 5.1 Get Container App URL
```bash
APP_URL=$(az containerapp show \
  --name product-service \
  --resource-group rg-product-service \
  --query properties.configuration.ingress.fqdn -o tsv)

echo "Application URL: https://$APP_URL"
```

### 5.2 Test API Endpoints
```bash
# Health check
curl https://$APP_URL/api/actuator/health

# Get all products
curl https://$APP_URL/api/products

# Create a product
curl -X PUT https://$APP_URL/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{"id":"1","name":"Test Product","price":29.99}'

# Get specific product
curl https://$APP_URL/api/products/1

# Delete product
curl -X DELETE https://$APP_URL/api/products/1
```

## Step 6: Configure Monitoring and Scaling

### 6.1 View Application Insights
1. Navigate to Azure Portal → Application Insights → product-service-insights
2. Check Application Map, Performance, and Failures
3. Set up alerts for key metrics

### 6.2 Configure Auto-scaling
```bash
az containerapp update \
  --name product-service \
  --resource-group rg-product-service \
  --min-replicas 1 \
  --max-replicas 10 \
  --scale-rule-name http-scale-rule \
  --scale-rule-http-concurrency 20
```

### 6.3 Configure Log Analytics
```bash
# View logs
az containerapp logs show \
  --name product-service \
  --resource-group rg-product-service \
  --follow
```

## Step 7: Production Considerations

### 7.1 Security Best Practices
- Use Azure Key Vault for secrets instead of environment variables
- Enable managed identity for Cosmos DB access
- Configure network restrictions and private endpoints
- Implement HTTPS only

### 7.2 Performance Optimization
- Monitor Cosmos DB RU consumption
- Adjust container app CPU/memory based on load
- Consider using reserved capacity for Cosmos DB
- Implement caching if needed

### 7.3 Backup and Disaster Recovery
- Enable automatic backup for Cosmos DB
- Set up geo-replication for multi-region deployments
- Configure Container Apps in multiple regions
- Implement health checks and automated failover

## Step 8: CI/CD Pipeline Setup

### 8.1 GitHub Actions Example
Create `.github/workflows/deploy.yml`:
```yaml
name: Deploy to Azure Container Apps

on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: mvn clean package -DskipTests
    
    - name: Build Docker image
      run: docker build -t ${{ secrets.ACR_NAME }}.azurecr.io/product-service:${{ github.sha }} .
    
    - name: Login to ACR
      uses: azure/docker-login@v1
      with:
        login-server: ${{ secrets.ACR_NAME }}.azurecr.io
        username: ${{ secrets.ACR_USERNAME }}
        password: ${{ secrets.ACR_PASSWORD }}
    
    - name: Push to ACR
      run: docker push ${{ secrets.ACR_NAME }}.azurecr.io/product-service:${{ github.sha }}
    
    - name: Deploy to Container Apps
      uses: azure/container-apps-deploy-action@v1
      with:
        containerAppName: product-service
        resourceGroup: rg-product-service
        imageToDeploy: ${{ secrets.ACR_NAME }}.azurecr.io/product-service:${{ github.sha }}
```

## Troubleshooting

### Common Issues
1. **Container fails to start**: Check logs and environment variables
2. **Cosmos DB connection errors**: Verify endpoint, key, and network access
3. **Health check failures**: Ensure actuator endpoints are accessible
4. **Performance issues**: Monitor CPU, memory, and Cosmos DB RUs

### Useful Commands
```bash
# View container app details
az containerapp show --name product-service --resource-group rg-product-service

# View logs
az containerapp logs show --name product-service --resource-group rg-product-service

# Update container app
az containerapp update --name product-service --resource-group rg-product-service --image new-image:tag

# Scale manually
az containerapp update --name product-service --resource-group rg-product-service --replicas 3
```

## Cost Optimization

- Use consumption-based pricing for Container Apps
- Monitor Cosmos DB RU usage and optimize queries
- Set appropriate min/max replicas for auto-scaling
- Consider using Azure Cost Management for monitoring

This completes the deployment of the migrated Product Service to Azure Container Apps with full observability and scalability.