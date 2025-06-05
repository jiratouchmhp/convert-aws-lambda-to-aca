// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.dao;

import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazonaws.example.product.product.config.AzureConfigurationProperties;
import software.amazonaws.example.product.product.entity.Product;
import software.amazonaws.example.product.product.entity.Products;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Azure Cosmos DB implementation of ProductDao.
 * Replaces DynamoDB with Azure Cosmos DB for data persistence.
 */
@Component
@ConditionalOnProperty(name = "azure.cosmos.enabled", havingValue = "true", matchIfMissing = false)
public class CosmosProductDao implements ProductDao {
    private static final Logger logger = LoggerFactory.getLogger(CosmosProductDao.class);
    
    private final CosmosContainer container;
    private final AzureConfigurationProperties azureConfig;

    public CosmosProductDao(CosmosClient cosmosClient, AzureConfigurationProperties azureConfig) {
        this.azureConfig = azureConfig;
        
        // Get database and container
        CosmosDatabase database = cosmosClient.getDatabase(azureConfig.getCosmos().getDatabaseName());
        this.container = database.getContainer(azureConfig.getCosmos().getContainerName());
        
        logger.info("CosmosProductDao initialized with database: {} and container: {}", 
            azureConfig.getCosmos().getDatabaseName(), 
            azureConfig.getCosmos().getContainerName());
    }

    @Override
    public Optional<Product> getProduct(String id) {
        try {
            logger.debug("Getting product with id: {}", id);
            
            CosmosItemResponse<CosmosProductDocument> response = container.readItem(
                id, 
                new PartitionKey(id), 
                CosmosProductDocument.class
            );
            
            if (response.getItem() != null) {
                Product product = response.getItem().toProduct();
                logger.debug("Found product: {}", product);
                return Optional.of(product);
            }
            
            logger.debug("Product not found with id: {}", id);
            return Optional.empty();
            
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                logger.debug("Product not found with id: {}", id);
                return Optional.empty();
            }
            logger.error("Error getting product with id: {}", id, e);
            throw new RuntimeException("Failed to get product", e);
        }
    }

    @Override
    public void putProduct(Product product) {
        try {
            logger.debug("Putting product: {}", product);
            
            CosmosProductDocument document = CosmosProductDocument.fromProduct(product);
            CosmosItemResponse<CosmosProductDocument> response = container.upsertItem(
                document,
                new PartitionKey(product.id()),
                new CosmosItemRequestOptions()
            );
            
            logger.debug("Product saved successfully with id: {}, Request charge: {} RUs", 
                product.id(), response.getRequestCharge());
                
        } catch (CosmosException e) {
            logger.error("Error saving product: {}", product, e);
            throw new RuntimeException("Failed to save product", e);
        }
    }

    @Override
    public void deleteProduct(String id) {
        try {
            logger.debug("Deleting product with id: {}", id);
            
            CosmosItemResponse<?> response = container.deleteItem(
                id,
                new PartitionKey(id),
                new CosmosItemRequestOptions()
            );
            
            logger.debug("Product deleted successfully with id: {}, Request charge: {} RUs", 
                id, response.getRequestCharge());
                
        } catch (CosmosException e) {
            if (e.getStatusCode() == 404) {
                logger.debug("Product not found for deletion with id: {}", id);
                return; // Consider this as successful deletion
            }
            logger.error("Error deleting product with id: {}", id, e);
            throw new RuntimeException("Failed to delete product", e);
        }
    }

    @Override
    public Products getAllProduct() {
        try {
            logger.debug("Getting all products");
            
            CosmosPagedIterable<CosmosProductDocument> items = container.queryItems(
                "SELECT * FROM c",
                new CosmosQueryRequestOptions(),
                CosmosProductDocument.class
            );
            
            List<Product> productList = new ArrayList<>();
            for (CosmosProductDocument document : items) {
                productList.add(document.toProduct());
            }
            
            logger.debug("Retrieved {} products", productList.size());
            return new Products(productList);
            
        } catch (CosmosException e) {
            logger.error("Error getting all products", e);
            throw new RuntimeException("Failed to get all products", e);
        }
    }
}