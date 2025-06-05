// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazonaws.example.product.product.entity.Product;
import software.amazonaws.example.product.product.entity.Products;
import software.amazonaws.example.product.product.service.ProductService;

import java.util.Optional;

/**
 * REST Controller for Product operations.
 * Migrated from AWS Lambda functions to Spring Boot REST endpoints.
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products.
     * Migrated from GetAllProductsFunction.
     *
     * @return ResponseEntity containing all products
     */
    @GetMapping
    public ResponseEntity<Products> getAllProducts() {
        try {
            Products products = productService.getAllProducts();
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            // Log the error (Application Insights will capture this)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a product by ID.
     * Migrated from GetProductByIdFunction.
     *
     * @param id the product ID
     * @return ResponseEntity containing the product if found, 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable String id) {
        try {
            Optional<Product> product = productService.getProductById(id);
            if (product.isPresent()) {
                return ResponseEntity.ok(product.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // Log the error (Application Insights will capture this)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create or update a product.
     * Migrated from CreateProductFunction.
     *
     * @param id the product ID from path
     * @param product the product data from request body
     * @return ResponseEntity with creation status
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> createOrUpdateProduct(@PathVariable String id, @RequestBody Product product) {
        try {
            // Validate that the ID in the path matches the ID in the body
            if (!product.id().equals(id)) {
                return ResponseEntity.badRequest()
                    .body("Product ID in the body does not match path parameter");
            }

            productService.createOrUpdateProduct(product);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body("Product with id = " + id + " created");
        } catch (Exception e) {
            // Log the error (Application Insights will capture this)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server Error :: " + e.getMessage());
        }
    }

    /**
     * Delete a product by ID.
     * Migrated from DeleteProductFunction.
     *
     * @param id the product ID to delete
     * @return ResponseEntity with deletion status
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable String id) {
        try {
            boolean deleted = productService.deleteProduct(id);
            if (deleted) {
                return ResponseEntity.ok("Product with id = " + id + " deleted");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            // Log the error (Application Insights will capture this)
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server Error :: " + e.getMessage());
        }
    }
}