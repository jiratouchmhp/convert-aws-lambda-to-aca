// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import software.amazonaws.example.product.product.entity.Product;
import software.amazonaws.example.product.product.entity.Products;
import software.amazonaws.example.product.product.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration test for ProductController.
 * Tests the REST endpoints migrated from AWS Lambda functions.
 */
@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @Test
    public void testGetAllProducts() throws Exception {
        // Given
        Product product = new Product("1", "Test Product", new BigDecimal("29.99"));
        Products products = new Products(List.of(product));
        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.products[0].id").value("1"))
                .andExpect(jsonPath("$.products[0].name").value("Test Product"))
                .andExpect(jsonPath("$.products[0].price").value(29.99));

        verify(productService).getAllProducts();
    }

    @Test
    public void testGetProductById_Found() throws Exception {
        // Given
        Product product = new Product("1", "Test Product", new BigDecimal("29.99"));
        when(productService.getProductById("1")).thenReturn(Optional.of(product));

        // When & Then
        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.price").value(29.99));

        verify(productService).getProductById("1");
    }

    @Test
    public void testGetProductById_NotFound() throws Exception {
        // Given
        when(productService.getProductById("999")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/products/999"))
                .andExpect(status().isNotFound());

        verify(productService).getProductById("999");
    }

    @Test
    public void testCreateProduct() throws Exception {
        // Given
        String productJson = "{\"id\":\"1\",\"name\":\"Test Product\",\"price\":29.99}";

        // When & Then
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isCreated())
                .andExpect(content().string("Product with id = 1 created"));

        verify(productService).createOrUpdateProduct(any(Product.class));
    }

    @Test
    public void testCreateProduct_IdMismatch() throws Exception {
        // Given
        String productJson = "{\"id\":\"2\",\"name\":\"Test Product\",\"price\":29.99}";

        // When & Then
        mockMvc.perform(put("/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Product ID in the body does not match path parameter"));

        verify(productService, never()).createOrUpdateProduct(any(Product.class));
    }

    @Test
    public void testDeleteProduct_Found() throws Exception {
        // Given
        when(productService.deleteProduct("1")).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Product with id = 1 deleted"));

        verify(productService).deleteProduct("1");
    }

    @Test
    public void testDeleteProduct_NotFound() throws Exception {
        // Given
        when(productService.deleteProduct("999")).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/products/999"))
                .andExpect(status().isNotFound());

        verify(productService).deleteProduct("999");
    }
}