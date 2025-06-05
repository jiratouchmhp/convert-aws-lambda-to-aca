# Code Change Scope Analysis

## AWS Services Migration Matrix

### High Complexity Services (Core Architecture Changes Required)

| AWS Service | Current Usage | Azure Equivalent | Migration Priority | Complexity |
|-------------|---------------|-----------------|-------------------|------------|
| **AWS Lambda** | 4 Functions:<br/>- GetProductByIdFunction<br/>- GetAllProductsFunction<br/>- CreateProductFunction<br/>- DeleteProductFunction | Azure Container Apps + Spring Boot Controllers | 1 | High |
| **API Gateway** | REST API with 4 endpoints:<br/>- GET /products/{id}<br/>- GET /products<br/>- PUT /products/{id}<br/>- DELETE /products/{id} | Azure Container Apps HTTP ingress | 1 | High |
| **DynamoDB** | ProductsTable with CRUD operations | Azure Cosmos DB (NoSQL API) | 2 | High |
| **Spring Cloud Function** | AWS adapter for Lambda integration | Spring Boot Web + REST Controllers | 1 | High |

### Medium Complexity Services (SDK/Library Changes)

| AWS Service | Current Usage | Azure Equivalent | Migration Priority | Complexity |
|-------------|---------------|-----------------|-------------------|------------|
| **AWS X-Ray** | Distributed tracing with TracingInterceptor | Azure Application Insights | 3 | Medium |
| **AWS SDK v2** | DynamoDB client configuration | Azure SDK for Java | 2 | Medium |
| **AWS Regions** | Region-based client configuration | Azure regions configuration | 3 | Medium |

### Low Complexity Services (Configuration Changes)

| AWS Service | Current Usage | Azure Equivalent | Migration Priority | Complexity |
|-------------|---------------|-----------------|-------------------|------------|
| **Environment Variables** | PRODUCT_TABLE_NAME, AWS_REGION | Azure App Configuration / Environment | 4 | Low |
| **IAM Policies** | DynamoDB permissions in SAM template | Azure RBAC / Managed Identity | 4 | Low |

## Detailed Analysis by Component

### Lambda Functions Analysis
- **File Pattern**: `src/main/java/software/amazonaws/example/product/product/handler/*Function.java`
- **Dependencies**: 
  - `com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent`
  - `com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent`
  - `java.util.function.Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>`
- **Transformation Required**: Convert to Spring Boot `@RestController` with appropriate HTTP mappings

### DynamoDB Integration Analysis
- **File**: `src/main/java/software/amazonaws/example/product/product/dao/DynamoProductDao.java`
- **Dependencies**: 
  - `software.amazon.awssdk.services.dynamodb.DynamoDbClient`
  - `software.amazon.awssdk.services.dynamodb.model.*`
- **Operations**: GetItem, PutItem, DeleteItem, Scan, DescribeTable
- **Transformation Required**: Replace with Azure Cosmos DB client

### X-Ray Tracing Analysis
- **Usage**: TracingInterceptor in DynamoDB client configuration
- **Files Affected**: DynamoProductDao.java, SpringBootSampleApplication.java
- **Transformation Required**: Replace with Azure Application Insights telemetry

### Maven Dependencies Analysis
- **AWS-Specific Dependencies**: 13 dependencies to be replaced
- **Spring Cloud Function**: AWS adapter to be removed
- **Native Compilation**: GraalVM configuration for AWS Lambda (may be simplified for containers)

## Migration Prioritization

1. **Phase 1 (Critical Path)**: Lambda → Controllers, API Gateway → Container Apps
2. **Phase 2 (Data Layer)**: DynamoDB → Cosmos DB  
3. **Phase 3 (Observability)**: X-Ray → Application Insights
4. **Phase 4 (Configuration)**: Environment variables and security

## Risk Assessment

- **High Risk**: Function signature changes affecting business logic
- **Medium Risk**: Data access patterns differences between DynamoDB and Cosmos DB
- **Low Risk**: Configuration and environment variable changes

## Success Criteria

- [ ] All AWS SDK dependencies removed
- [ ] Application builds successfully with Azure dependencies
- [ ] All business logic preserved in controller migration
- [ ] Database operations work with Cosmos DB
- [ ] Tracing functional with Application Insights
- [ ] Application deployable to Azure Container Apps