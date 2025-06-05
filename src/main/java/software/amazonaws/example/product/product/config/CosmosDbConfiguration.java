// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.config;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.identity.DefaultAzureCredentialBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Azure Cosmos DB configuration.
 * Configures Cosmos DB client for data access.
 */
@Configuration
@ConditionalOnProperty(name = "azure.cosmos.enabled", havingValue = "true", matchIfMissing = false)
public class CosmosDbConfiguration {
    
    private static final Logger logger = LoggerFactory.getLogger(CosmosDbConfiguration.class);

    private final AzureConfigurationProperties azureConfig;

    public CosmosDbConfiguration(AzureConfigurationProperties azureConfig) {
        this.azureConfig = azureConfig;
    }

    @Bean
    public CosmosClient cosmosClient() {
        logger.info("Initializing Cosmos DB client with endpoint: {}", 
            azureConfig.getCosmos().getEndpoint());

        CosmosClientBuilder clientBuilder = new CosmosClientBuilder()
            .endpoint(azureConfig.getCosmos().getEndpoint());

        // Use key authentication if provided, otherwise use Azure identity
        if (azureConfig.getCosmos().getKey() != null && 
            !azureConfig.getCosmos().getKey().isEmpty()) {
            logger.info("Using key authentication for Cosmos DB");
            clientBuilder = clientBuilder.key(azureConfig.getCosmos().getKey());
        } else {
            logger.info("Using Azure identity authentication for Cosmos DB");
            clientBuilder = clientBuilder.credential(new DefaultAzureCredentialBuilder().build());
        }

        // Configure connection options for optimal performance
        CosmosClient client = clientBuilder
            .directMode(DirectConnectionConfig.getDefaultConfig())
            .gatewayMode(GatewayConnectionConfig.getDefaultConfig())
            .buildClient();

        logger.info("Cosmos DB client initialized successfully");
        return client;
    }
}