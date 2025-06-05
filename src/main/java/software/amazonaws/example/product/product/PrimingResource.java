/**
 * This code was used to prime the resources in SnapStart example. 
 * Since we're migrating to Azure Container Apps, this file is disabled.
 * To learn more about SnapStart and Priming, refer to https://aws.amazon.com/blogs/compute/reducing-java-cold-starts-on-aws-lambda-functions-with-snapstart/
 **/

package software.amazonaws.example.product.product;

import org.springframework.context.annotation.Configuration;
import software.amazonaws.example.product.product.dao.DynamoProductDao;

@Configuration
public class PrimingResource {

  private final DynamoProductDao productDao;

  public PrimingResource(DynamoProductDao productDao) {
    this.productDao = productDao;
    // Core.getGlobalContext().register(this); // Disabled for Azure Container Apps
  }

  // @Override // Disabled for Azure Container Apps
  public void beforeCheckpoint(Object context) throws Exception {
    System.out.println("beforeCheckpoint hook - disabled for Azure Container Apps");
    //Below line would initialize the AWS SDK DynamoDBClient class. This technique is called "Priming".
    // productDao.describeTable(); // Will be migrated to Cosmos DB
  }

  // @Override // Disabled for Azure Container Apps
  public void afterRestore(Object context) throws Exception {
    System.out.println("afterRestore hook - disabled for Azure Container Apps");
  }
}
