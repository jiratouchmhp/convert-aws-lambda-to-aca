# STEP1: Dependencies Migration Guide

## Overview
This guide details the migration of AWS Java SDK dependencies to Azure SDK equivalents for Azure Container Apps deployment.

## Before Migration (AWS Dependencies)

### AWS-Specific Dependencies Removed
```xml
<!-- AWS Lambda Runtime -->
<dependency>
  <groupId>com.amazonaws</groupId>
  <artifactId>aws-lambda-java-serialization</artifactId>
  <version>1.0.0</version>
</dependency>

<!-- Spring Cloud Function AWS Adapter -->
<dependency>
  <groupId>org.springframework.cloud</groupId>
  <artifactId>spring-cloud-function-adapter-aws</artifactId>
</dependency>

<!-- AWS Native/SnapStart Dependencies -->
<dependency>
  <groupId>io.github.crac</groupId>
  <artifactId>org-crac</artifactId>
  <version>0.1.3</version>
</dependency>

<dependency>
  <groupId>org.springframework.experimental</groupId>
  <artifactId>spring-native</artifactId>
  <version>${spring-native.version}</version>
</dependency>

<!-- Additional X-Ray Dependencies -->
<dependency>
  <groupId>com.amazonaws</groupId>
  <artifactId>aws-xray-recorder-sdk-apache-http</artifactId>
</dependency>
<dependency>
  <groupId>com.amazonaws</groupId>
  <artifactId>aws-xray-recorder-sdk-aws-sdk</artifactId>
</dependency>
<dependency>
  <groupId>com.amazonaws</groupId>
  <artifactId>aws-xray-recorder-sdk-aws-sdk-instrumentor</artifactId>
</dependency>
```

### Maven Plugins Removed
```xml
<!-- AWS-Specific Shade Plugin Configuration -->
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-shade-plugin</artifactId>
  <configuration>
    <createDependencyReducedPom>false</createDependencyReducedPom>
    <shadedArtifactAttached>true</shadedArtifactAttached>
    <shadedClassifierName>aws</shadedClassifierName>
  </configuration>
</plugin>

<!-- GraalVM Native Build Profile -->
<profile>
  <id>native</id>
  <!-- Complete native build configuration removed -->
</profile>
```

## After Migration (Azure Dependencies)

### Azure SDK Dependencies Added
```xml
<!-- Azure Cosmos DB (replaces DynamoDB) -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-cosmos</artifactId>
  <version>${azure-cosmos.version}</version>
</dependency>

<!-- Azure Identity (for authentication) -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-identity</artifactId>
  <version>${azure-identity.version}</version>
</dependency>

<!-- Azure Blob Storage (for file storage) -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-blob</artifactId>
  <version>${azure-storage-blob.version}</version>
</dependency>

<!-- Azure Application Insights (replaces X-Ray) -->
<dependency>
  <groupId>com.microsoft.azure</groupId>
  <artifactId>applicationinsights-spring-boot-starter</artifactId>
  <version>2.6.4</version>
</dependency>
```

### Spring Boot Dependencies Enhanced
```xml
<!-- Spring Boot Web (replaces Lambda) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Boot Actuator (health checks) -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

### Azure SDK BOM Added
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-sdk-bom</artifactId>
  <version>1.2.19</version>
  <type>pom</type>
  <scope>import</scope>
</dependency>
```

## Version Properties Migration

### Before
```xml
<properties>
  <java.version>17</java.version>
  <azure.version>1.2.19</azure.version>
</properties>
```

### After
```xml
<properties>
  <java.version>17</java.version>
  <azure.version>1.2.19</azure.version>
  <spring-cloud.version>2022.0.3</spring-cloud.version>
  <spring-native.version>0.12.1</spring-native.version>
  <azure-sdk.version>1.2.19</azure-sdk.version>
  <azure-cosmos.version>4.53.1</azure-cosmos.version>
  <azure-identity.version>1.11.1</azure-identity.version>
  <azure-storage-blob.version>12.24.1</azure-storage-blob.version>
</properties>
```

## Maven Profile Migration

### Before (AWS Lambda Focus)
```xml
<profile>
  <id>jvm</id>
  <!-- Heavy AWS Lambda shade plugin configuration -->
</profile>
```

### After (Container Apps Focus)
```xml
<profile>
  <id>azure-container-apps</id>
  <activation>
    <activeByDefault>true</activeByDefault>
  </activation>
  <!-- Simple Spring Boot plugin configuration -->
</profile>
```

## Temporary Dependencies (Gradual Migration Strategy)

To enable gradual migration, some AWS dependencies are temporarily kept:

```xml
<!-- Keep AWS Lambda dependencies temporarily for gradual migration -->
<dependency>
  <groupId>com.amazonaws</groupId>
  <artifactId>aws-lambda-java-core</artifactId>
  <version>1.2.2</version>
  <scope>compile</scope>
  <!-- Will be removed in Phase 4 -->
</dependency>

<!-- Keep AWS SDK for gradual migration -->
<dependency>
  <groupId>software.amazon.awssdk</groupId>
  <artifactId>dynamodb</artifactId>
  <scope>compile</scope>
  <!-- Will be replaced with Azure Cosmos DB in Phase 2 -->
</dependency>

<!-- Keep X-Ray temporarily for gradual migration -->
<dependency>
  <groupId>com.amazonaws</groupId>
  <artifactId>aws-xray-recorder-sdk-core</artifactId>
  <scope>compile</scope>
  <!-- Will be removed in Phase 3 -->
</dependency>
```

## Build Validation

After migration, validate the build:

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Build package
mvn package

# Start application (for testing)
mvn spring-boot:run
```

## Migration Benefits

1. **Simplified Build**: Removed complex native compilation and shading
2. **Modern Architecture**: Standard Spring Boot web application
3. **Azure Native**: Optimized for Azure Container Apps
4. **Maintainability**: Cleaner dependency tree
5. **Development Experience**: Standard Spring Boot development workflow

## Next Steps

1. **Phase 3**: Convert Lambda handlers to REST controllers
2. **Phase 4**: Replace DynamoDB with Azure Cosmos DB
3. **Phase 5**: Replace X-Ray with Application Insights
4. **Phase 6**: Remove remaining AWS dependencies

## Troubleshooting

### Common Issues
1. **Version Conflicts**: Use Azure SDK BOM for consistent versions
2. **Missing Dependencies**: Ensure all Azure dependencies are included
3. **Build Failures**: Check for removed AWS-specific annotations

### Validation Checklist
- [ ] Application compiles successfully
- [ ] No AWS-specific import errors
- [ ] Spring Boot starts without errors
- [ ] All Azure dependencies resolved
- [ ] Build artifacts created correctly