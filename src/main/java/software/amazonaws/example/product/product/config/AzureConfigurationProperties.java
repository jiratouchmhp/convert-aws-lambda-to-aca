// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package software.amazonaws.example.product.product.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Azure configuration properties for the Product Service.
 * Replaces AWS environment variables with Azure-specific configuration.
 */
@Configuration
@ConfigurationProperties(prefix = "azure")
public class AzureConfigurationProperties {

    private final Cosmos cosmos = new Cosmos();
    private final ApplicationInsights applicationInsights = new ApplicationInsights();
    private final Identity identity = new Identity();

    public Cosmos getCosmos() {
        return cosmos;
    }

    public ApplicationInsights getApplicationInsights() {
        return applicationInsights;
    }

    public Identity getIdentity() {
        return identity;
    }

    public static class Cosmos {
        private String endpoint;
        private String key;
        private String databaseName;
        private String containerName;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getDatabaseName() {
            return databaseName;
        }

        public void setDatabaseName(String databaseName) {
            this.databaseName = databaseName;
        }

        public String getContainerName() {
            return containerName;
        }

        public void setContainerName(String containerName) {
            this.containerName = containerName;
        }
    }

    public static class ApplicationInsights {
        private String instrumentationKey;
        private String connectionString;

        public String getInstrumentationKey() {
            return instrumentationKey;
        }

        public void setInstrumentationKey(String instrumentationKey) {
            this.instrumentationKey = instrumentationKey;
        }

        public String getConnectionString() {
            return connectionString;
        }

        public void setConnectionString(String connectionString) {
            this.connectionString = connectionString;
        }
    }

    public static class Identity {
        private String clientId;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }
    }
}