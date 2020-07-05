package com.hyperlocal.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.github.jasync.sql.db.mysql.MySQLQueryResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hyperlocal.server.Data.CatalogItem;
import com.hyperlocal.server.Data.Merchant;
import com.hyperlocal.server.Data.Shop;
import com.hyperlocal.server.Data.ShopDetails;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ShopController {

  private PubSubTemplate publisher;
  private static final String DATABASE_URL = "jdbc:mysql://10.124.32.3:3306/hyperlocal";
  private Connection connection;
  private Utilities util;
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

  @Inject
  public ShopController(PubSubTemplate pubSubTemplate) {
    this.publisher = pubSubTemplate;
    this.util = new Utilities();
    connection = MySQLConnectionBuilder.createConnectionPool(DATABASE_URL);
  }

  /*
   * To-do server-side checks: - Access control - Data validation - Remove Cross
   * Origin annotations
   */

  @CrossOrigin(origins = { "http://localhost:3000", "https://speedy-anthem-217710.an.r.appspot.com",
      "https://microapps.google.com" })
  @GetMapping("/api/query/elastic")
  public CompletableFuture<List<ShopDetails>> getDataFromElasticSearch(@RequestParam String query,
      @RequestParam String queryRadius, @RequestParam String latitude, @RequestParam String longitude) {

    MultiMatchQueryBuilder matchQuery = QueryBuilders
        .multiMatchQuery(query, "shopname", "typeofservice", "merchantname", "catalogitems").fuzziness("AUTO");
    GeoDistanceQueryBuilder filterOnDistance = QueryBuilders.geoDistanceQuery("pin.location")
        .point(Double.parseDouble(latitude), Double.parseDouble(longitude)).distance(queryRadius);

    BoolQueryBuilder boolMatchQueryWithDistanceFilter = QueryBuilders.boolQuery().must(matchQuery)
        .filter(filterOnDistance);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

    searchSourceBuilder.query(boolMatchQueryWithDistanceFilter);

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://10.128.0.13:9200/shops/_search?filter_path=hits.hits._id"))
        .method("GET", HttpRequest.BodyPublishers.ofString(searchSourceBuilder.toString()))
        .setHeader("Content-Type", "application/json").build();

    List<Long> shopIDList = new ArrayList<Long>();

    return client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body)
        .thenApply((responseString) -> {
          JsonObject obj = JsonParser.parseString(responseString).getAsJsonObject();
          JsonArray idListJson = obj.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
          for (JsonElement id : idListJson) {
            shopIDList.add(id.getAsJsonObject().get("_id").getAsLong());
          }
          return shopIDList;
        }).thenCompose((shopList) -> {
          return getShopsByShopIDBatch(shopList);
        });
  }

  @CrossOrigin(origins = { "http://localhost:3000", "https://speedy-anthem-217710.an.r.appspot.com",
      "https://microapps.google.com" })
  @GetMapping("/api/browse/elastic")
  public CompletableFuture<List<ShopDetails>> getDataFromElasticSearch(@RequestParam String queryRadius,
      @RequestParam String latitude, @RequestParam String longitude) {

    GeoDistanceQueryBuilder filterOnDistance = QueryBuilders.geoDistanceQuery("pin.location")
        .point(Double.parseDouble(latitude), Double.parseDouble(longitude)).distance(queryRadius);
    BoolQueryBuilder boolMatchQueryWithDistanceFilter = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
        .filter(filterOnDistance);
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(boolMatchQueryWithDistanceFilter);
    System.out.println(boolMatchQueryWithDistanceFilter.toString());

    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("http://10.128.0.13:9200/shops/_search?filter_path=hits.hits._id"))
        .method("GET", HttpRequest.BodyPublishers.ofString(searchSourceBuilder.toString()))
        .setHeader("Content-Type", "application/json").build();

    List<Long> shopIDList = new ArrayList<Long>();

    return client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body)
        .thenApply((responseString) -> {
          JsonObject obj = JsonParser.parseString(responseString).getAsJsonObject();
          JsonArray idListJson = obj.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
          for (JsonElement id : idListJson) {
            shopIDList.add(id.getAsJsonObject().get("_id").getAsLong());
          }
          return shopIDList;
        }).thenCompose((shopList) -> {
          return getShopsByShopIDBatch(shopList);
        });
  }

  public CompletableFuture<List<ShopDetails>> getShopsByShopIDBatch(List<Long> shopIDList) {

    HashMap<String, Merchant> mapMerchantIdToMerchant = new HashMap<String, Merchant>();
    HashMap<Long, ShopDetails> mapShopIdToShopDetails = new HashMap<Long, ShopDetails>();
    List<ShopDetails> shopsList = new ArrayList<ShopDetails>();

    String shopPreparedStatementPlaceholder = Utilities.getPlaceHolderString(shopIDList.size());

    // Fetch All Shops in ShopIDList and store their merchantIDs in a List
    return connection
        .sendPreparedStatement(String.format(SELECT_SHOPS_BATCH_QUERY, shopPreparedStatementPlaceholder), shopIDList)
        .thenApply((QueryResult result) -> {
          List<String> merchantIDList = new ArrayList<String>();
          ResultSet allShops = result.getRows();
          for (RowData shop : allShops) {
            ShopDetails shopDetails = new ShopDetails();
            Long ShopID = (Long) shop.get("ShopID");
            String MerchantID = (String) shop.get("MerchantID");
            shopDetails.setShop(new Shop(shop));
            merchantIDList.add(MerchantID);
            mapShopIdToShopDetails.put(ShopID, shopDetails);
          }
          return merchantIDList;

          // Select all Merchant Data for every merchantID in merchantIDList
        }).thenCompose((merchantIDList) -> {
          String merchantPreparedStatementPlaceholder = Utilities.getPlaceHolderString(merchantIDList.size());
          return connection.sendPreparedStatement(
              String.format(SELECT_MERCHANT_BATCH_QUERY, merchantPreparedStatementPlaceholder), merchantIDList);
        })

        // Map All merchantIDs to their Merchants and Update ShopDetails with merchant
        // Information
        .thenApply((result) -> {
          ResultSet allMerchants = result.getRows();
          for (RowData merchant : allMerchants) {
            mapMerchantIdToMerchant.put((String) merchant.get("MerchantID"), new Merchant(merchant));
          }
          for (Long shopID : shopIDList) {
            if (mapShopIdToShopDetails.containsKey(shopID)) {
              ShopDetails shopDetails = mapShopIdToShopDetails.get(shopID);
              String MerchantID = shopDetails.shop.merchantID;
              shopDetails.setMerchant(mapMerchantIdToMerchant.get(MerchantID));
              shopDetails.catalog = new ArrayList<CatalogItem>();
              mapShopIdToShopDetails.put(shopID, shopDetails);
            }
          }
          return allMerchants;

          // Get Catalog for all Shops
        }).thenCompose((allMerchants) -> {
          return connection.sendPreparedStatement(
              String.format(SELECT_CATALOG_BATCH_QUERY, shopPreparedStatementPlaceholder), shopIDList);

          // Update ShopDetails with CatalogItems
        }).thenApply((catalogQueryResult) -> {
          ResultSet catalogRecords = catalogQueryResult.getRows();
          for (RowData serviceRecord : catalogRecords) {
            Long ShopID = (Long) serviceRecord.get("ShopID");
            ShopDetails shopDetails = mapShopIdToShopDetails.get(ShopID);
            shopDetails.addCatalogItem(new CatalogItem(serviceRecord));
            mapShopIdToShopDetails.put(ShopID, shopDetails);
          }
          for (Long ShopId : shopIDList) {
            if (mapShopIdToShopDetails.containsKey(ShopId)) {
              shopsList.add(mapShopIdToShopDetails.get(ShopId));
            }
          }
          return shopsList;
        });
  }

  @CrossOrigin(origins = { "http://localhost:3000", "https://speedy-anthem-217710.an.r.appspot.com",
      "https://microapps.google.com" })
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
          ex.printStackTrace();
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        });

    return shopsPromise;
  }

  @CrossOrigin(origins = { "http://localhost:3000", "https://speedy-anthem-217710.an.r.appspot.com",
      "https://microapps.google.com" })
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

  @CrossOrigin(origins = { "http://localhost:3000", "https://speedy-anthem-217710.an.r.appspot.com",
      "https://microapps.google.com" })
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
    // publish promise not working -check that
    return statusPromise.thenCompose((QueryResult result) -> {
      // If successful, commit
      return connection.sendQuery("COMMIT");
    }).thenCompose((QueryResult result) -> {
      return publishMessage(Long.toString(shopID));
    }).thenApply((String publishPromise) -> {
      HashMap<String, Object> successMap = new HashMap<String, Object>();
      successMap.put("success", true);
      return successMap;
    }).exceptionally((ex) -> {
      // Else, auto-rollback when connection closes
      logger.error("Executed exceptionally: upsertCatalog()", ex);
      ex.printStackTrace();
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    });

  }

  /*
   * Route to handle shop upserts for a merchant Returns: Inserted Shop Instance
   */

  @CrossOrigin(origins = { "http://localhost:3000", "https://speedy-anthem-217710.an.r.appspot.com",
      "https://microapps.google.com" })
  @PostMapping("/api/insert/shop")
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
      e.printStackTrace();
      return "";
    }).thenApply((publishPromise) -> {
      return new Shop(shopDataAsJson);
    });
  }

  /*
   * Expects: All shop details (including the ShopID of the shop to be Updated)
   */

  @CrossOrigin(origins = { "http://localhost:3000", "https://speedy-anthem-217710.an.r.appspot.com",
      "https://microapps.google.com" })
  @PostMapping("/api/update/shop/")
  public CompletableFuture<Shop> updateShop(@RequestBody String shopDetailsString) {
    JsonObject shopDataAsJson = JsonParser.parseString(shopDetailsString).getAsJsonObject();

    return updateShopDetails(shopDataAsJson).thenApply((QueryResult queryResult) -> {
      return shopDataAsJson.get("shopID").getAsString();
    }).thenCompose((shopID) -> {
      return publishMessage(shopID);
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
