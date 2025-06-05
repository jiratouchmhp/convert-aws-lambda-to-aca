// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import software.amazonaws.example.product.product.service.ProductService;
import software.amazonaws.example.product.product.entity.Product;
import software.amazonaws.example.product.product.entity.Products;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the Product Service.
 * Tests the complete flow from controller to service to DAO layer.
 * Works with both DynamoDB and Cosmos DB implementations via mocking.
 */
@WebMvcTest
@ActiveProfiles("test")
class ProductServiceIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService; // Mock the service instead of DAO

    @Test
    void testGetAllProducts_Success() throws Exception {
        // Given
        Product product1 = new Product("1", "Test Product 1", new BigDecimal("29.99"));
        Product product2 = new Product("2", "Test Product 2", new BigDecimal("39.99"));
        Products products = new Products(List.of(product1, product2));
        
        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products").isArray())
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].id").value("1"))
                .andExpect(jsonPath("$.products[0].name").value("Test Product 1"))
                .andExpect(jsonPath("$.products[0].price").value(29.99))
                .andExpect(jsonPath("$.products[1].id").value("2"))
                .andExpect(jsonPath("$.products[1].name").value("Test Product 2"))
                .andExpect(jsonPath("$.products[1].price").value(39.99));

        verify(productService).getAllProducts();
    }

    @Test
    void testGetProductById_Found() throws Exception {
        // Given
        String productId = "1";
        Product product = new Product(productId, "Test Product", new BigDecimal("29.99"));
        
        when(productService.getProductById(productId)).thenReturn(Optional.of(product));

        // When & Then
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(29.99));

        verify(productService).getProductById(productId);
    }

    @Test
    void testGetProductById_NotFound() throws Exception {
        // Given
        String productId = "nonexistent";
        
        when(productService.getProductById(productId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/products/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService).getProductById(productId);
    }

    @Test
    void testCreateProduct_Success() throws Exception {
        // Given
        String productId = "1";
        String productJson = """
            {
                "id": "1",
                "name": "Test Product",
                "price": 29.99
            }
            """;

        doNothing().when(productService).createOrUpdateProduct(any(Product.class));

        // When & Then
        mockMvc.perform(put("/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isCreated())
                .andExpect(content().string("Product with id = 1 created"));

        verify(productService).createOrUpdateProduct(any(Product.class));
    }

    @Test
    void testCreateProduct_IdMismatch() throws Exception {
        // Given
        String pathId = "1";
        String productJson = """
            {
                "id": "2",
                "name": "Test Product",
                "price": 29.99
            }
            """;

        // When & Then
        mockMvc.perform(put("/products/{id}", pathId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product ID in the body does not match path parameter"));

        verify(productService, never()).createOrUpdateProduct(any(Product.class));
    }

    @Test
    void testDeleteProduct_Success() throws Exception {
        // Given
        String productId = "1";
        
        when(productService.deleteProduct(productId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(content().string("Product with id = 1 deleted"));

        verify(productService).deleteProduct(productId);
    }

    @Test
    void testDeleteProduct_NotFound() throws Exception {
        // Given
        String productId = "nonexistent";
        
        when(productService.deleteProduct(productId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/products/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService).deleteProduct(productId);
    }
}