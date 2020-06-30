package com.hyperlocal.server;

import com.hyperlocal.server.Data.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

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

@RestController
public class ShopController {

  private final PubSubTemplate publisher;
  private static final String DATABASE_URL = "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal";;
  private Connection connection;
  private static final String SHOP_UPDATE_STATEMENT = "UPDATE `Shops` SET `ShopName` = ?, `TypeOfService`=?, `Latitude` = ?, `Longitude` = ?, `AddressLine1` = ? WHERE `ShopID`=?;";
  private static final String SHOP_INSERT_STATEMENT = "INSERT INTO `Shops` (`ShopName`, `TypeOfService`, `Latitude`, `Longitude`, `AddressLine1`, `MerchantID`) VALUES (?,?,?,?,?,?);";;
  private static final String SELECT_SHOP_STATEMENT = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `ShopID` = ?;";
  private static final String SELECT_SHOPS_BY_MERCHANT_STATEMENT = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `MerchantID` = ?;";
  private static final String SELECT_SHOPS_BATCH_QUERY = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `ShopID` IN (%s);";
  private static final String SELECT_MERCHANT_STATEMENT = "SELECT `MerchantID`, `MerchantName`, `MerchantPhone` from `Merchants` WHERE `MerchantID` = ?;";
  private static final String SELECT_MERCHANT_BATCH_QUERY = "SELECT `MerchantID`, `MerchantName`, `MerchantPhone` from `Merchants` WHERE `MerchantID` IN (%s);";
  private static final String SELECT_CATALOG_BY_SHOP_STATEMENT = "SELECT `ServiceID`, `ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL` from `Catalog` WHERE `ShopID` = ?;";
  private static final String SELECT_CATALOG_BATCH_QUERY = "SELECT `ServiceID`, `ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL` from `Catalog` WHERE `ShopID` IN (%s);";
  private static final String INSERT_CATALOG_STATEMENT = "INSERT INTO `Catalog` (`ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL`) VALUES (?, ?, ?, ?);";
  private static final String UPDATE_CATALOG_STATEMENT = "UPDATE `Catalog` SET `ServiceName` = ?, `ServiceDescription` = ?, `ImageURL` = ? WHERE `ServiceID` = ?;";
  private static final String DELETE_CATALOG_STATEMENT = "DELETE FROM `Catalog` WHERE `ServiceID` = ?;";
  private static final String PUBSUB_URL = "projects/speedy-anthem-217710/topics/testTopic";
  private static final Logger logger = LogManager.getLogger(ShopController.class);

  public ShopController(PubSubTemplate pubSubTemplate) {
    this.publisher = pubSubTemplate;
    connection = MySQLConnectionBuilder.createConnectionPool(DATABASE_URL);
  }

  /*
   * To-do server-side checks: - Access control - Data validation
   */

  // Fetch all shops by merchantID
  @GetMapping("/api/merchant/{merchantID}/shops")
  public CompletableFuture<List<Shop>> getShopsByMerchantID(@PathVariable String merchantID) {
    List<Shop> shopsList = new ArrayList<Shop>();

    CompletableFuture<List<Shop>> shopsPromise = connection
        // Get associated shops
        .sendPreparedStatement(SELECT_SHOPS_BY_MERCHANT_STATEMENT, Arrays.asList(merchantID))
        .thenApply((QueryResult shopQueryResult) -> {
          ResultSet shopRecords = shopQueryResult.getRows();
          for (RowData shopRecord : shopRecords)
            shopsList.add(new Shop(shopRecord));
          return shopsList;

        }).exceptionally(ex -> {
          logger.error("Executed exceptionally: getShopsByMerchantID()", ex);
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        });

    return shopsPromise;
  }

  @GetMapping("/api/shops/multiple")
  public CompletableFuture<List<ShopDetails>> getShopsByShopIDBatch() {
    List<ShopDetails> shopsList = new ArrayList<ShopDetails>();

    List<Long> shopIDList = new ArrayList<Long>();
    shopIDList.add(1L);
    shopIDList.add(2L);
    shopIDList.add(3L);

    HashMap<Long, Shop> mapShopIdtoShop = new HashMap<Long, Shop>();
    HashMap<String, Merchant> mapMerchantIdToMerchant = new HashMap<String, Merchant>();;
    HashMap<Long, ShopDetails> mapShopIdToShopDetails = new HashMap<Long, ShopDetails>();;

    // Convert ShopIDs to a String separated by Comma
    String shopIDListAsString = shopIDList.stream().map(Object::toString).collect(Collectors.joining(","));

    System.out.println(shopIDListAsString);
    return connection.sendPreparedStatement(String.format(SELECT_SHOPS_BATCH_QUERY, shopIDListAsString))
        .thenApply((QueryResult result) -> {
          List<String> merchantIDList = new ArrayList<String>();
          ResultSet allShops = result.getRows();
          for (RowData shop : allShops) {
            mapShopIdtoShop.put((Long) shop.get("ShopID"), new Shop(shop));
            mapShopIdToShopDetails.put((Long) shop.get("ShopID"), new ShopDetails());

            merchantIDList.add((String) shop.get("MerchantID"));
          }
          return merchantIDList;
     
     
        }).thenCompose((merchantIDList) -> {
          String MerchantIDListAsString = merchantIDList.stream().map(Object::toString).collect(Collectors.joining(","));
          return connection.sendPreparedStatement(String.format(SELECT_MERCHANT_BATCH_QUERY, MerchantIDListAsString));
        }).thenApply((result) -> {
          ResultSet allMerchants = result.getRows();
          for (RowData merchant : allMerchants) {
            mapMerchantIdToMerchant.put((String) merchant.get("MerchantID"), new Merchant(merchant));
          }
          return allMerchants;
     
     
        }).thenCompose((allMerchants) -> {
          return connection.sendPreparedStatement(String.format(SELECT_CATALOG_BATCH_QUERY, shopIDListAsString));
        }).thenApply((catalogQueryResult) -> {
          ResultSet catalogRecords = catalogQueryResult.getRows();
          for (RowData serviceRecord : catalogRecords) {
            Long ShopID = (Long) serviceRecord.get("ShopID");
            ShopDetails shopDetails = mapShopIdToShopDetails.get(ShopID);
            Shop shop = mapShopIdtoShop.get(ShopID);
            shopDetails.setShop(mapShopIdtoShop.get(ShopID));
            shopDetails.setMerchant(mapMerchantIdToMerchant.get(shop.merchantID));
            shopDetails.addCatalogItem(new CatalogItem(serviceRecord));
            mapShopIdToShopDetails.put(ShopID, shopDetails);
          }

          for (Long ShopId : shopIDList) {
            shopsList.add(mapShopIdToShopDetails.get(ShopId));
          }
          return shopsList;
        });
  }

  // Fetch catalog, shop & merchant details by shopID.
  @GetMapping("/api/shop/{shopID}")
  public CompletableFuture<ShopDetails> getShopDetails(@PathVariable Long shopID) {

    ShopDetails shopDetails = new ShopDetails();

    CompletableFuture<ShopDetails> shopDetailsPromise = connection
        // Get Shop details:
        .sendPreparedStatement(SELECT_SHOP_STATEMENT, Arrays.asList(shopID))
        .thenCompose((QueryResult shopQueryResult) -> {
          ResultSet wrappedShopRecord = shopQueryResult.getRows();
          if (wrappedShopRecord.size() == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested shop was not found.");
          RowData shopRecord = wrappedShopRecord.get(0);
          shopDetails.setShop(new Shop(shopRecord));

          // Get Merchant Details:
          return connection.sendPreparedStatement(SELECT_MERCHANT_STATEMENT,
              Arrays.asList(shopDetails.shop.merchantID));
        }).thenCompose((QueryResult merchantQueryResult) -> {
          ResultSet wrappedMerchantRecord = merchantQueryResult.getRows();
          if (wrappedMerchantRecord.size() == 0)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. No merchant found for the shop.");
          shopDetails.setMerchant(new Merchant(wrappedMerchantRecord.get(0)));

          // Get Catalog Details:
          return connection.sendPreparedStatement(SELECT_CATALOG_BY_SHOP_STATEMENT, Arrays.asList(shopID));
        }).thenApply((QueryResult catalogQueryResult) -> {
          ResultSet catalogRecords = catalogQueryResult.getRows();
          for (RowData serviceRecord : catalogRecords)
            shopDetails.addCatalogItem(new CatalogItem(serviceRecord));
          return shopDetails;

        });

    return shopDetailsPromise;
  }

  @PostMapping("/api/shop/{shopID}/catalog/update")
  public CompletableFuture<HashMap<String, Object>> upsertCatalog(@PathVariable Long shopID,
      @RequestBody String updatePayload) {
    JsonObject commands = JsonParser.parseString(updatePayload).getAsJsonObject();
    JsonArray addCommands = commands.getAsJsonArray("add");
    JsonArray editCommands = commands.getAsJsonArray("edit");
    JsonArray delCommands = commands.getAsJsonArray("delete");
    CompletableFuture<QueryResult> statusPromise = connection.sendQuery("BEGIN");

    // Process add commands
    for (JsonElement addCommandRaw : addCommands) {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        JsonObject addCommand = addCommandRaw.getAsJsonObject();
        String serviceName = addCommand.get("serviceName").getAsString();
        String serviceDescription = addCommand.get("serviceDescription").getAsString();
        String imageURL = addCommand.get("imageURL").getAsString();
        return connection.sendPreparedStatement(INSERT_CATALOG_STATEMENT,
            Arrays.asList(shopID, serviceName, serviceDescription, imageURL));
      });
    }

    // Process edit commands
    for (JsonElement editCommandRaw : editCommands) {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        JsonObject editCommand = editCommandRaw.getAsJsonObject();
        Long serviceID = editCommand.get("serviceID").getAsLong();
        String serviceName = editCommand.get("serviceName").getAsString();
        String serviceDescription = editCommand.get("serviceDescription").getAsString();
        String imageURL = editCommand.get("imageURL").getAsString();
        return connection.sendPreparedStatement(UPDATE_CATALOG_STATEMENT,
            Arrays.asList(serviceName, serviceDescription, imageURL, serviceID));
      });
    }

    // Process delete commands
    for (JsonElement delCommandRaw : delCommands) {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        Long serviceID = delCommandRaw.getAsJsonObject().get("serviceID").getAsLong();
        return connection.sendPreparedStatement(DELETE_CATALOG_STATEMENT, Arrays.asList(serviceID));
      });
    }

    return statusPromise.thenCompose((QueryResult result) -> {
      // If successful, commit
      return connection.sendQuery("COMMIT");
    }).thenApply((QueryResult result) -> {
      HashMap<String, Object> successMap = new HashMap<String, Object>();
      successMap.put("success", true);
      return successMap;
    }).exceptionally((ex) -> {
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
