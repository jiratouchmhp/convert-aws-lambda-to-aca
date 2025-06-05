// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.dao;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazonaws.example.product.product.entity.Product;

import java.math.BigDecimal;

/**
 * Document model for Azure Cosmos DB persistence.
 * Maps between Product entity and Cosmos DB document format.
 */
public class CosmosProductDocument {
    
    @JsonProperty("id")
    private String id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("price")
    private BigDecimal price;
    
    @JsonProperty("_partitionKey")
    private String partitionKey;

    // Default constructor for Jackson
    public CosmosProductDocument() {
    }

    public CosmosProductDocument(String id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.partitionKey = id; // Using id as partition key for simplicity
    }

    public static CosmosProductDocument fromProduct(Product product) {
        return new CosmosProductDocument(
            product.id(),
            product.name(),
            product.price()
        );
    }

    public Product toProduct() {
        return new Product(this.id, this.name, this.price);
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        this.partitionKey = id; // Keep partition key in sync
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getPartitionKey() {
        return partitionKey;
    }

    public void setPartitionKey(String partitionKey) {
        this.partitionKey = partitionKey;
    }

    @Override
    public String toString() {
        return "CosmosProductDocument{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", partitionKey='" + partitionKey + '\'' +
                '}';
    }
}