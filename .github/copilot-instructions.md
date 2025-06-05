
Custom Instruction: Migration from AWS Serverless to Azure Container Apps
=========================================================================

Overview:
---------
This guide outlines a custom migration process for transforming an AWS serverless Java-based architecture (using AWS Lambda, API Gateway, DynamoDB, etc.) into an Azure-native solution using Azure Container Apps. The steps leverage detailed documentation for dependency, configuration, infrastructure, and code migration.


Phase-by-Phase Migration Instructions:
--------------------------------------

Phase 1: Analyze Code Change Scope
----------------------------------
Source: CODE_CHANGE_SCOPE_ANALYSIS.md

- Identify all AWS-specific services (Lambda, S3, DynamoDB, SNS, etc.).
- Audit Lambda handlers, API Gateway paths, and custom logic.
- Classify services into tiers (High, Medium, Low) based on complexity and cloud dependency.

Output:
- Migration scope matrix with services categorized for prioritization.

---

Phase 2: Migrate Dependencies
-----------------------------
Source: STEP1_DEPENDENCIES_MIGRATION.md

- Replace AWS Java SDK dependencies with Azure SDK equivalents.
- Remove AWS-specific Gradle/Maven plugins.
- Add libraries for Azure Identity, Azure Blob Storage, Azure Cosmos DB, Application Insights.

Output:
- Updated `pom.xml` or `build.gradle` aligned with Azure ecosystem.

---

Phase 3: Migrate Application Configuration
-----------------------------------------
Source: STEP2_CONFIGURATION_MIGRATION.md

- Externalize configuration via Azure App Configuration or Key Vault.
- Replace environment variable logic to use Azure conventions.
- Remove embedded secrets and ensure secure configuration loading.

Output:
- Environment-specific Azure-ready configuration (e.g., `application.yml`)

---

Phase 4: Refactor Lambda to Spring Boot Controller
--------------------------------------------------
Source: LAMBDA_TO_CONTROLLER_TRANSFORMATION.md

- Convert Lambda handlers into annotated Spring Boot REST endpoints.
- Use `@RestController`, `@GetMapping`, `@PostMapping`, and `@RequestBody`.
- Organize logic into services and DTO layers for clarity and testing.

Output:
- Controller-based service logic compatible with HTTP interface in Azure Container Apps.

---

Phase 5: Containerize the Application
-------------------------------------
Source: CONTAINER_CONFIGURATION_GUIDE.md

- Write a secure Dockerfile with health check support.
- Use `distroless` or `azul/zulu-openjdk` base images for lean builds.
- Push container images to Azure Container Registry (ACR).

Output:
- Production-ready container images for each microservice.

---

Phase 6: Database Migration
---------------------------
Source: DATABASE_MIGRATION_GUIDE.md

- Migrate from AWS DynamoDB or Aurora to Azure Cosmos DB or PostgreSQL.
- Apply schema transformations using tools like DMS or pgloader.
- Refactor repository/data access logic to new Azure SDK or JDBC.

Output:
- Database schema and data successfully migrated and validated.

---

Phase 7: Code Integration and Validation
----------------------------------------
Source: CODE_MIGRATION_GUIDE.md

- Integrate all refactored services and configurations.
- Build and test Docker containers locally.
- Validate using unit, integration, and contract tests.
- Deploy via CI/CD (e.g., GitHub Actions, Azure DevOps).

Output:
- End-to-end Azure-ready Java microservices application.

---

Final Checklist
---------------
[ ] AWS SDKs removed and replaced with Azure SDKs  
[ ] Lambda converted to Spring Boot REST controllers  
[ ] Dockerfiles created and images pushed to ACR  
[ ] Azure infrastructure provisioned via Terraform/Bicep  
[ ] Database migrated and validated  
[ ] Application integrated with App Config and Key Vault  
[ ] Application tested and validated in Azure environment
[ ] Deployment successful on Azure Container Apps  

