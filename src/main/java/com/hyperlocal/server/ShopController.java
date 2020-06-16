package com.hyperlocal.server;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.github.jasync.sql.db.mysql.MySQLQueryResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
public class ShopController {

  private final PubSubTemplate publisher;
  private static final String DATABASE_URL = "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal";;
  private Connection connection;
  private static final String SHOP_UPDATE_STATEMENT = "UPDATE `Shops` SET `ShopName` = ?, `TypeOfService`=?, `Latitude` = ?, `Longitude` = ?, `AddressLine1` = ? WHERE `ShopID`=?;";
  private static final String SHOP_INSERT_STATEMENT = "INSERT INTO `Shops` (`ShopName`, `TypeOfService`, `Latitude`, `Longitude`, `AddressLine1`, `MerchantID`) VALUES (?,?,?,?,?,?);";;
  private static final String SELECT_SHOP_STATEMENT = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `ShopID` = ?;";
  private static final String SELECT_SHOPS_BY_MERCHANT_STATEMENT = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `MerchantID` = ?;";
  private static final String SELECT_MERCHANT_STATEMENT = "SELECT `MerchantID`, `MerchantName`, `MerchantPhone` from `Merchants` WHERE `MerchantID` = ?;";
  private static final String SELECT_CATALOG_BY_SHOP_STATEMENT = "SELECT `ServiceID`, `ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL` from `Catalog` WHERE `ShopID` = ?;";
  private static final String INSERT_CATALOG_STATEMENT = "INSERT INTO `Catalog` (`ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL`) VALUES (?, ?, ?, ?);";
  private static final String UPDATE_CATALOG_STATEMENT = "UPDATE `Catalog` SET `ServiceName` = ?, `ServiceDescription` = ?, `ImageURL` = ? WHERE `ServiceID` = ?;";
  private static final String DELETE_CATALOG_STATEMENT = "DELETE FROM `Catalog` WHERE `ServiceID` = ?;";
  private static final String PUBSUB_URL = "projects/speedy-anthem-217710/topics/testTopic";
  private static final Logger logger = LogManager.getLogger(ShopController.class);

  public ShopController(PubSubTemplate pubSubTemplate) {
    this.publisher = pubSubTemplate;
    connection = MySQLConnectionBuilder.createConnectionPool(DATABASE_URL);
  }

  /* To-do server-side checks: 
  - Access control
  - Data validation */


  // Fetch all shops by merchantID
  @GetMapping("/api/merchant/{merchantID}/shops")
  public CompletableFuture<List<Shop>> getShopsByMerchantID(@PathVariable Long merchantID) {
    // Container obj for merchant's shops
    List<Shop> shopsList = new ArrayList<Shop>();

    // Promise: returns merchant's shops
    CompletableFuture<List<Shop>> shopsPromise = connection
        // Get associated shops
      .sendPreparedStatement(SELECT_SHOPS_BY_MERCHANT_STATEMENT, Arrays.asList(merchantID))
      .thenApply((QueryResult shopQueryResult) -> {
        ResultSet shopRecords = shopQueryResult.getRows();
        for(RowData shopRecord : shopRecords) shopsList.add(new Shop(shopRecord));
        return shopsList;
        
        // If something goes wrong:
      }).exceptionally(ex -> {
        logger.error("Executed exceptionally: getShopsByMerchantID()", ex);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
      });

    return shopsPromise;
  }


  // Fetch catalog, shop & merchant details by shopID.
  @GetMapping("/api/shop/{shopID}")
  public CompletableFuture<HashMap<String, Object>> getShopDetails(@PathVariable Long shopID) {

    // Container obj for shop details
    HashMap<String, Object> shopDetailsMap = new HashMap<String, Object>();

    // Promise: returns shop details
    CompletableFuture<HashMap<String, Object>> shopDetailsPromise = connection
        // Get Shop details:
      .sendPreparedStatement(SELECT_SHOP_STATEMENT, Arrays.asList(shopID))
      .thenCompose((QueryResult shopQueryResult) -> {
        ResultSet wrappedShopRecord = shopQueryResult.getRows();
        if(wrappedShopRecord.size() == 0) throw new RuntimeException("Not found"); // No shop with supplied ShopID found
        RowData shopRecord = wrappedShopRecord.get(0);
        Shop shop = new Shop(shopRecord);
        shopDetailsMap.put("shopDetails", shop);

        // Get Merchant Details:
        return connection.sendPreparedStatement(SELECT_MERCHANT_STATEMENT, Arrays.asList(shop.merchantID));
      }).thenCompose((QueryResult merchantQueryResult) -> {
        RowData merchantRecord = merchantQueryResult.getRows().get(0);
        Merchant merchant = new Merchant((Long)merchantRecord.get(0), (String)merchantRecord.get(1), (String)merchantRecord.get(2));
        shopDetailsMap.put("merchantDetails", merchant);
        
        // Get Catalog Details:
        return connection.sendPreparedStatement(SELECT_CATALOG_BY_SHOP_STATEMENT, Arrays.asList(shopID));
      }).thenApply((QueryResult catalogQueryResult) -> {
        ResultSet catalogRecords = catalogQueryResult.getRows();
        ArrayList<CatalogItem> servicesList = new ArrayList<CatalogItem>();
        for(RowData serviceRecord : catalogRecords) servicesList.add(new CatalogItem(serviceRecord));
        shopDetailsMap.put("catalog", servicesList);
        return shopDetailsMap;
        
        // If something goes wrong:
      }).exceptionally(ex -> {
        logger.error("Executed exceptionally: getShopDetails()", ex);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
      });

    return shopDetailsPromise;
  }


  @PostMapping("/api/shop/{shopID}/catalog/update")
  public CompletableFuture<HashMap<String, Object>> upsertCatalog(@PathVariable Long shopID, @RequestBody String updatePayload) {
    // Container obj for upsert status
    JsonObject commands = JsonParser.parseString(updatePayload).getAsJsonObject();
    JsonArray addCommands = commands.getAsJsonArray("add");
    JsonArray editCommands = commands.getAsJsonArray("edit");
    JsonArray delCommands = commands.getAsJsonArray("delete");
    CompletableFuture<QueryResult> statusPromise = connection.sendQuery("BEGIN");
    
    // Process add commands
    for(JsonElement addCommandRaw : addCommands)
    {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        JsonObject addCommand = addCommandRaw.getAsJsonObject();
        String serviceName = addCommand.get("serviceName").getAsString();
        String serviceDescription = addCommand.get("serviceDescription").getAsString();
        String imageURL = addCommand.get("imageURL").getAsString();
        return connection.sendPreparedStatement(INSERT_CATALOG_STATEMENT, Arrays.asList(shopID, serviceName, serviceDescription, imageURL));
      });
    }

    // Process edit commands
    for(JsonElement editCommandRaw : editCommands)
    {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        JsonObject editCommand = editCommandRaw.getAsJsonObject();
        Long serviceID = editCommand.get("serviceID").getAsLong();
        String serviceName = editCommand.get("serviceName").getAsString();
        String serviceDescription = editCommand.get("serviceDescription").getAsString();
        String imageURL = editCommand.get("imageURL").getAsString();
        return connection.sendPreparedStatement(UPDATE_CATALOG_STATEMENT, Arrays.asList(serviceName, serviceDescription, imageURL, serviceID));
      });
    }

    // Process delete commands
    for(JsonElement delCommandRaw : delCommands)
    {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        Long serviceID = delCommandRaw.getAsJsonObject().get("serviceID").getAsLong();
        return connection.sendPreparedStatement(DELETE_CATALOG_STATEMENT, Arrays.asList(serviceID));
      });
    }
    
    return statusPromise
      .thenCompose((QueryResult result) -> {
        // If successful, commit
        return connection.sendQuery("COMMIT");
      }).thenApply((QueryResult result) -> {
        HashMap<String, Object> successMap = new HashMap<String, Object>();
        successMap.put("success", true);
        return successMap;
      })
      .exceptionally((ex) -> {
        // Else, auto-rollback when connection closes
        logger.error("Executed exceptionally: upsertCatalog()", ex);
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
      });

  }

  /*
   * Route to handle shop upserts for a merchant Returns: Inserted Shop Instance
   */

  @PostMapping("/insert/shop")
  public @ResponseBody CompletableFuture<Shop> insertShop(@RequestBody String shopDetailsString)
      throws InterruptedException, ExecutionException {
    JsonObject shopDataAsJson = JsonParser.parseString(shopDetailsString).getAsJsonObject();

    return insertNewShop(shopDataAsJson).thenApply((queryResult) -> {
      return ((MySQLQueryResult) queryResult).getLastInsertId();
    }).thenApply((shopID) -> {
      shopDataAsJson.addProperty("shopID", shopID);
      return shopID;
    }).thenCompose((shopID) -> {
      return publishMessage(Long.toString(shopID));
    }).exceptionally(e -> {
      logger.error(String.format("ShopID %s: Could not publish to PubSub. Exited exceptionally!",
          shopDataAsJson.get("shopID").getAsString()));
      return "";
    }).thenApply((publishPromise) -> {
      return new Shop(shopDataAsJson);
    });
  }

  /*
   * Expects: All shop details (including the ShopID of the shop to be Updated)
   */

  @PostMapping("/update/shop/")
  public CompletableFuture<Shop> updateShop(@RequestBody String shopDetailsString) {
    JsonObject shopDataAsJson = JsonParser.parseString(shopDetailsString).getAsJsonObject();

    return updateShopDetails(shopDataAsJson).thenApply((QueryResult queryResult) -> {
      return ((MySQLQueryResult) queryResult).getLastInsertId();
    }).thenCompose((shopID) -> {
      return publishMessage(Long.toString(shopID));
    }).exceptionally(e -> {
      logger.error(String.format("ShopID %s: Could not publish to PubSub. Exited exceptionally!",
          shopDataAsJson.get("shopID").getAsString()));
      return "";
    }).thenApply((publishPromise) -> {
      return new Shop(shopDataAsJson);
    });
  }

  public CompletableFuture<String> publishMessage(String message) {
    return this.publisher.publish(PUBSUB_URL, message).completable();
  }

  public CompletableFuture<QueryResult> updateShopDetails(JsonObject shopDataJsonObject) {
    String UpdateQueryParameters[] = new String[] { shopDataJsonObject.get("shopName").getAsString(),
        shopDataJsonObject.get("typeOfService").getAsString(), shopDataJsonObject.get("latitude").getAsString(),
        shopDataJsonObject.get("longitude").getAsString(), shopDataJsonObject.get("addressLine1").getAsString(),
        shopDataJsonObject.get("shopID").getAsString() };

    return connection.sendPreparedStatement(SHOP_UPDATE_STATEMENT, Arrays.asList(UpdateQueryParameters));
  }

  public CompletableFuture<QueryResult> insertNewShop(JsonObject shopDataJsonObject) {
    String InsertQueryParameters[] = new String[] { shopDataJsonObject.get("shopName").getAsString(),
        shopDataJsonObject.get("typeOfService").getAsString(), shopDataJsonObject.get("latitude").getAsString(),
        shopDataJsonObject.get("longitude").getAsString(), shopDataJsonObject.get("addressLine1").getAsString(),
        shopDataJsonObject.get("merchantID").getAsString() };

    return connection.sendPreparedStatement(SHOP_INSERT_STATEMENT, Arrays.asList(InsertQueryParameters));
  }
}
