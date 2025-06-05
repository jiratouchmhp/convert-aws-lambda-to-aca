// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import software.amazonaws.example.product.product.config.AzureConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AzureConfigurationProperties.class)
public class SpringBootSampleApplication {

  public static void main(String[] args) {
    SpringApplication.run(SpringBootSampleApplication.class, args);
  }
}
