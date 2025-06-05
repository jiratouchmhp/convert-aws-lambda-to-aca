// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.dao;

import org.junit.jupiter.api.Test;
import software.amazonaws.example.product.product.entity.Product;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CosmosProductDao.
 * Tests the Azure Cosmos DB data access layer.
 * Note: For comprehensive testing, integration tests with Cosmos DB emulator are recommended.
 */
class CosmosProductDaoTest {

    @Test
    void testCosmosProductDocumentMapping() {
        // Given
        Product product = new Product("1", "Test Product", new BigDecimal("29.99"));

        // When
        CosmosProductDocument document = CosmosProductDocument.fromProduct(product);
        Product mappedProduct = document.toProduct();

        // Then
        assertEquals(product.id(), mappedProduct.id());
        assertEquals(product.name(), mappedProduct.name());
        assertEquals(product.price(), mappedProduct.price());
    }

    @Test
    void testCosmosProductDocumentPartitionKey() {
        // Given
        String productId = "test-product-id";
        Product product = new Product(productId, "Test Product", new BigDecimal("29.99"));

        // When
        CosmosProductDocument document = CosmosProductDocument.fromProduct(product);

        // Then
        assertEquals(productId, document.getPartitionKey());
        assertEquals(productId, document.getId());
    }
}