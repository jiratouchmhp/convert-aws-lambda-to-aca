// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.dao;

import org.junit.jupiter.api.Test;
import software.amazonaws.example.product.product.entity.Product;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CosmosProductDocument.
 * Tests the document model for Azure Cosmos DB.
 */
class CosmosProductDocumentTest {

    @Test
    void testFromProduct() {
        // Given
        Product product = new Product("1", "Test Product", new BigDecimal("29.99"));

        // When
        CosmosProductDocument document = CosmosProductDocument.fromProduct(product);

        // Then
        assertEquals(product.id(), document.getId());
        assertEquals(product.name(), document.getName());
        assertEquals(product.price(), document.getPrice());
        assertEquals(product.id(), document.getPartitionKey());
    }

    @Test
    void testToProduct() {
        // Given
        CosmosProductDocument document = new CosmosProductDocument("1", "Test Product", new BigDecimal("29.99"));

        // When
        Product product = document.toProduct();

        // Then
        assertEquals(document.getId(), product.id());
        assertEquals(document.getName(), product.name());
        assertEquals(document.getPrice(), product.price());
    }

    @Test
    void testSetId_UpdatesPartitionKey() {
        // Given
        CosmosProductDocument document = new CosmosProductDocument();

        // When
        document.setId("test-id");

        // Then
        assertEquals("test-id", document.getId());
        assertEquals("test-id", document.getPartitionKey());
    }

    @Test
    void testDefaultConstructor() {
        // When
        CosmosProductDocument document = new CosmosProductDocument();

        // Then
        assertNull(document.getId());
        assertNull(document.getName());
        assertNull(document.getPrice());
        assertNull(document.getPartitionKey());
    }

    @Test
    void testToString() {
        // Given
        CosmosProductDocument document = new CosmosProductDocument("1", "Test Product", new BigDecimal("29.99"));

        // When
        String result = document.toString();

        // Then
        assertTrue(result.contains("id='1'"));
        assertTrue(result.contains("name='Test Product'"));
        assertTrue(result.contains("price=29.99"));
        assertTrue(result.contains("partitionKey='1'"));
    }
}