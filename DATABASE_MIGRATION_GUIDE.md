# Database Migration Guide: DynamoDB to Azure Cosmos DB

This document outlines the migration from AWS DynamoDB to Azure Cosmos DB for the Product Service.

## Overview

The migration involves:
1. **Data Access Layer**: New `CosmosProductDao` implementation
2. **Configuration**: Azure Cosmos DB connection settings
3. **Data Model**: `CosmosProductDocument` for document storage
4. **Deployment**: Conditional activation via Spring profiles

## Architecture Changes

### Before (DynamoDB)
- `DynamoProductDao` using AWS SDK v2
- Direct DynamoDB table operations
- AWS X-Ray tracing integration
- Environment-based configuration

### After (Azure Cosmos DB)
- `CosmosProductDao` using Azure SDK
- Cosmos DB NoSQL API operations
- Azure Application Insights integration
- Spring profiles for environment separation

## Configuration

### Spring Profiles

**Default Profile**: Uses DynamoDB (existing functionality)
```bash
# Default - no profile specified
java -jar application.jar
```

**Azure Profile**: Uses Cosmos DB
```bash
# Enable Azure Cosmos DB
java -jar application.jar --spring.profiles.active=azure
```

### Environment Variables

For Azure Cosmos DB deployment, set these environment variables:

```bash
# Required for Azure profile
AZURE_COSMOS_ENDPOINT=https://your-cosmos-account.documents.azure.com:443/
AZURE_COSMOS_KEY=your-cosmos-key-here
AZURE_COSMOS_DATABASE=productdb
AZURE_COSMOS_CONTAINER=products

# Optional - for Azure Application Insights
AZURE_APPINSIGHTS_CONNECTION_STRING=your-connection-string
```

## Data Model Mapping

### DynamoDB Schema
```json
{
  "PK": "product-id",
  "name": "Product Name", 
  "price": "29.99"
}
```

### Cosmos DB Schema
```json
{
  "id": "product-id",
  "name": "Product Name",
  "price": 29.99,
  "_partitionKey": "product-id"
}
```

## Migration Steps

### 1. Database Setup

Create Azure Cosmos DB resources:
```bash
# Create Cosmos DB account
az cosmosdb create \
  --name your-cosmos-account \
  --resource-group your-rg \
  --default-consistency-level Session

# Create database
az cosmosdb sql database create \
  --account-name your-cosmos-account \
  --resource-group your-rg \
  --name productdb

# Create container
az cosmosdb sql container create \
  --account-name your-cosmos-account \
  --resource-group your-rg \
  --database-name productdb \
  --name products \
  --partition-key-path "/id" \
  --throughput 400
```

### 2. Data Migration

Use Azure Data Factory or custom migration scripts:

```java
// Example migration logic
public void migrateData() {
    // Read from DynamoDB
    Products allProducts = dynamoProductDao.getAllProduct();
    
    // Write to Cosmos DB
    for (Product product : allProducts.products()) {
        cosmosProductDao.putProduct(product);
    }
}
```

### 3. Application Deployment

Deploy with Azure profile:
```bash
# Container deployment
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=azure \
  -e AZURE_COSMOS_ENDPOINT=https://your-cosmos-account.documents.azure.com:443/ \
  -e AZURE_COSMOS_KEY=your-key \
  springboot-azure-app:latest
```

## Performance Considerations

### Request Units (RUs)
- Monitor RU consumption in Azure portal
- Adjust throughput based on usage patterns
- Consider autoscale for variable workloads

### Partition Strategy
- Using product ID as partition key
- Provides even distribution for CRUD operations
- Optimal for single-item operations

### Connection Optimization
- Connection pooling configured in `CosmosDbConfiguration`
- Direct mode for better performance
- Gateway mode as fallback

## Testing

### Unit Tests
```bash
mvn test -Dtest=CosmosProductDaoTest
mvn test -Dtest=CosmosProductDocumentTest
```

### Integration Tests
Use Azure Cosmos DB Emulator for local testing:
```bash
# Start Cosmos DB Emulator
docker run -p 8081:8081 mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator

# Run tests with emulator
mvn test -Dspring.profiles.active=dev
```

### Load Testing
- Use Azure Load Testing service
- Monitor RU consumption and latency
- Compare with DynamoDB performance metrics

## Monitoring and Observability

### Azure Application Insights
- Automatic dependency tracking
- Custom telemetry for business metrics
- Performance and exception monitoring

### Health Checks
- Cosmos DB health indicator available at `/actuator/health`
- Readiness and liveness probes for Kubernetes

## Rollback Strategy

### Blue-Green Deployment
1. Deploy new version with Cosmos DB (green)
2. Test functionality thoroughly
3. Route traffic gradually
4. Keep DynamoDB version (blue) as fallback

### Configuration Rollback
```bash
# Switch back to DynamoDB
kubectl set env deployment/product-service SPRING_PROFILES_ACTIVE=default
```

## Cost Optimization

### Cosmos DB Pricing
- Pay for provisioned throughput (RUs)
- Storage costs separate
- Free tier: 1000 RU/s and 25 GB storage

### Optimization Tips
- Use appropriate consistency level
- Optimize query patterns
- Consider reserved capacity for production

## Security

### Authentication
- Use Azure Managed Identity in production
- Key-based auth for development/testing
- Rotate keys regularly

### Network Security
- Enable firewall rules
- Use private endpoints for production
- Encrypt data in transit and at rest

## Troubleshooting

### Common Issues
1. **Connection timeouts**: Check network connectivity and firewall rules
2. **Throttling (429 errors)**: Increase provisioned throughput
3. **Large response sizes**: Implement pagination
4. **Hot partitions**: Review partition key strategy

### Debugging
```bash
# Enable debug logging
logging.level.com.azure=DEBUG
logging.level.software.amazonaws.example=DEBUG
```

### Support Resources
- [Azure Cosmos DB Documentation](https://docs.microsoft.com/azure/cosmos-db/)
- [Azure Support](https://azure.microsoft.com/support/)
- Application logs via Azure Application Insights