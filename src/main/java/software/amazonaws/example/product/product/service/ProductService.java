// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.service;

import org.springframework.stereotype.Service;
import software.amazonaws.example.product.product.dao.ProductDao;
import software.amazonaws.example.product.product.entity.Product;
import software.amazonaws.example.product.product.entity.Products;

import java.util.Optional;

/**
 * Service layer for Product operations.
 * Separates business logic from HTTP controller concerns.
 */
@Service
public class ProductService {

    private final ProductDao productDao;

    public ProductService(ProductDao productDao) {
        this.productDao = productDao;
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param id the product ID
     * @return Optional containing the product if found, empty otherwise
     */
    public Optional<Product> getProductById(String id) {
        return productDao.getProduct(id);
    }

    /**
     * Retrieves all products.
     *
     * @return Products containing list of all products
     */
    public Products getAllProducts() {
        return productDao.getAllProduct();
    }

    /**
     * Creates or updates a product.
     *
     * @param product the product to create/update
     */
    public void createOrUpdateProduct(Product product) {
        productDao.putProduct(product);
    }

    /**
     * Deletes a product by its ID.
     *
     * @param id the product ID to delete
     * @return true if the product existed and was deleted, false if it didn't exist
     */
    public boolean deleteProduct(String id) {
        Optional<Product> existingProduct = productDao.getProduct(id);
        if (existingProduct.isPresent()) {
            productDao.deleteProduct(id);
            return true;
        }
        return false;
    }

    /**
     * Checks if a product exists by its ID.
     *
     * @param id the product ID
     * @return true if the product exists, false otherwise
     */
    public boolean productExists(String id) {
        return productDao.getProduct(id).isPresent();
    }
}