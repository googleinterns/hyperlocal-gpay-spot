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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ShopController {

  private PubSubTemplate publisher;

  private Connection connection;

  private static final Logger logger = LogManager.getLogger(ShopController.class);

  public ShopController(PubSubTemplate pubSubTemplate) {
    this.publisher = pubSubTemplate;
    connection = MySQLConnectionBuilder.createConnectionPool(Constants.DATABASE_URL);
  }
  
  // API for performing search and browse queries
  @GetMapping("/v1/shops")
  public CompletableFuture<List<ShopDetails>> getDataFromSearchIndex(
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "queryRadius", required = false, defaultValue = "3km") String queryRadius,
      @RequestParam String latitude, 
      @RequestParam String longitude) {

    List<Long> shopIDList = new ArrayList<Long>();

    // GeoDistance query for filtering everything in a radius
    GeoDistanceQueryBuilder filterOnDistance = QueryBuilders.geoDistanceQuery("pin.location")
        .point(Double.parseDouble(latitude), Double.parseDouble(longitude)).distance(queryRadius);

    // Boolean query to hold conditions of both Matchquery and Geodistance query
    BoolQueryBuilder boolMatchQueryWithDistanceFilter;

    // Create a match query

    // default: If no input string provided, browse query (i.e match all)
    // else match on input text
    if (query.equals("")) {
      boolMatchQueryWithDistanceFilter = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
          .filter(filterOnDistance);
    } else {
      boolMatchQueryWithDistanceFilter = QueryBuilders
          .boolQuery().must(QueryBuilders
              .multiMatchQuery(query, "shopname", "typeofservice", "merchantname", "catalogitems").fuzziness("AUTO"))
          .filter(filterOnDistance);
    }

    // Create a search request with the Boolean query
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(boolMatchQueryWithDistanceFilter);

    // Create the HTTP Request to send
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(Constants.SEARCH_INDEX_URL))
        .method("GET", HttpRequest.BodyPublishers.ofString(searchSourceBuilder.toString()))
        .setHeader("Content-Type", "application/json").build();

    // Send request to search Index asynchronously and parse response to get ShopIDs
    return client.sendAsync(request, BodyHandlers.ofString()).thenApply(HttpResponse::body)
        .thenCompose((responseString) -> {
          // Empty {} is returned by search Index if nothing matches
          if (!responseString.equals("{}")) {
            JsonObject obj = JsonParser.parseString(responseString).getAsJsonObject();
            JsonArray idListJson = obj.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
            for (JsonElement id : idListJson) {
              shopIDList.add(id.getAsJsonObject().get("_id").getAsLong());
            }
          }
          // Perform BatchQuery on shopIDs and get List of ShopDetails corresponding to
          // the shopIDs
          // If nothing matches then shopIDList is empty
          return getShopsByShopIDBatch(shopIDList);
        });
  }

  public CompletableFuture<List<ShopDetails>> getShopsByShopIDBatch(List<Long> shopIDList) {

    HashMap<String, Merchant> mapMerchantIdToMerchant = new HashMap<String, Merchant>();
    HashMap<Long, ShopDetails> mapShopIdToShopDetails = new HashMap<Long, ShopDetails>();
    List<ShopDetails> shopsList = new ArrayList<ShopDetails>();

    // if empty shopIDList then return empty shopsList
    if (shopIDList.isEmpty()) {
      return CompletableFuture.completedFuture(shopsList);
    }

    String shopPreparedStatementPlaceholder = Utilities.getPlaceHolderString(shopIDList.size());

    // Fetch All Shops in ShopIDList and store their merchantIDs in a List
    return connection
        .sendPreparedStatement(String.format(Constants.SELECT_SHOPS_BATCH_QUERY, shopPreparedStatementPlaceholder),
            shopIDList)
        .thenCompose((QueryResult result) -> {
          List<String> merchantIDList = new ArrayList<String>();
          ResultSet allShops = result.getRows();
          for (RowData shop : allShops) {
            ShopDetails shopDetails = new ShopDetails();
            Long ShopID = (Long) shop.get("ShopID");
            String MerchantID = (String) shop.get("MerchantID");
            shopDetails.setShop(Shop.create(shop));
            merchantIDList.add(MerchantID);
            mapShopIdToShopDetails.put(ShopID, shopDetails);
          }

          // Select all Merchant Data for every merchantID in merchantIDList
          String merchantPreparedStatementPlaceholder = Utilities.getPlaceHolderString(merchantIDList.size());
          return connection.sendPreparedStatement(
              String.format(Constants.SELECT_MERCHANT_BATCH_QUERY, merchantPreparedStatementPlaceholder),
              merchantIDList);
        })

        // Map All merchantIDs to their Merchants and Update ShopDetails with merchant
        // Information
        .thenCompose((result) -> {
          ResultSet allMerchants = result.getRows();
          for (RowData merchant : allMerchants) {
            mapMerchantIdToMerchant.put((String) merchant.get("MerchantID"), Merchant.create(merchant));
          }
          for (Long shopID : shopIDList) {
            if (mapShopIdToShopDetails.containsKey(shopID)) {
              ShopDetails shopDetails = mapShopIdToShopDetails.get(shopID);
              String MerchantID = shopDetails.shop.merchantID();
              shopDetails.setMerchant(mapMerchantIdToMerchant.get(MerchantID));
              shopDetails.catalog = new ArrayList<CatalogItem>();
              mapShopIdToShopDetails.put(shopID, shopDetails);
            }
          }
          // Get Catalog for all Shops
          return connection.sendPreparedStatement(
              String.format(Constants.SELECT_CATALOG_BATCH_QUERY, shopPreparedStatementPlaceholder), shopIDList);

          // Update ShopDetails with CatalogItems
        }).thenApply((catalogQueryResult) -> {
          ResultSet catalogRecords = catalogQueryResult.getRows();
          for (RowData serviceRecord : catalogRecords) {
            Long ShopID = (Long) serviceRecord.get("ShopID");
            ShopDetails shopDetails = mapShopIdToShopDetails.get(ShopID);
            shopDetails.addCatalogItem(CatalogItem.create(serviceRecord));
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

  // Fetch all shops by merchantID
  @GetMapping("/v1/merchants/{merchantID}/shops")
  public CompletableFuture<List<Shop>> getShopsByMerchantID(@PathVariable String merchantID) {
    List<Shop> shopsList = new ArrayList<Shop>();

    CompletableFuture<List<Shop>> shopsPromise = connection
        // Get associated shops
        .sendPreparedStatement(Constants.SELECT_SHOPS_BY_MERCHANT_STATEMENT, Arrays.asList(merchantID))
        .thenApply((QueryResult shopQueryResult) -> {
          ResultSet shopRecords = shopQueryResult.getRows();
          for (RowData shopRecord : shopRecords)
            shopsList.add(Shop.create(shopRecord));
          return shopsList;

        }).exceptionally(ex -> {
          logger.error("Executed exceptionally: getShopsByMerchantID()", ex);
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
        });

    return shopsPromise;
  }

  // Fetch catalog, shop & merchant details by shopID.
  @GetMapping("/v1/shops/{shopID}")
  public CompletableFuture<ShopDetails> getShopDetails(@PathVariable Long shopID) {

    ShopDetails shopDetails = new ShopDetails();

    CompletableFuture<ShopDetails> shopDetailsPromise = connection
        // Get Shop details:
        .sendPreparedStatement(Constants.SELECT_SHOP_STATEMENT, Arrays.asList(shopID))
        .thenCompose((QueryResult shopQueryResult) -> {
          ResultSet wrappedShopRecord = shopQueryResult.getRows();
          if (wrappedShopRecord.size() == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested shop was not found.");
          RowData shopRecord = wrappedShopRecord.get(0);
          shopDetails.setShop(Shop.create(shopRecord));

          // Get Merchant Details:
          return connection.sendPreparedStatement(Constants.SELECT_MERCHANT_STATEMENT,
              Arrays.asList(shopDetails.shop.merchantID()));
        }).thenCompose((QueryResult merchantQueryResult) -> {
          ResultSet wrappedMerchantRecord = merchantQueryResult.getRows();
          if (wrappedMerchantRecord.size() == 0)
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                "Something went wrong. No merchant found for the shop.");
          shopDetails.setMerchant(Merchant.create(wrappedMerchantRecord.get(0)));

          // Get Catalog Details:
          return connection.sendPreparedStatement(Constants.SELECT_CATALOG_BY_SHOP_STATEMENT, Arrays.asList(shopID));
        }).thenApply((QueryResult catalogQueryResult) -> {
          ResultSet catalogRecords = catalogQueryResult.getRows();
          for (RowData serviceRecord : catalogRecords)
            shopDetails.addCatalogItem(CatalogItem.create(serviceRecord));
          return shopDetails;
        });

    return shopDetailsPromise;
  }

  @PostMapping("/v1/merchants/{merchantID}/shops/{shopID}/catalog:batchUpdate")
  public CompletableFuture<HashMap<String, Object>> upsertCatalog(@PathVariable Long shopID,
      @RequestBody String updatePayload) {
    JsonObject commands = JsonParser.parseString(updatePayload).getAsJsonObject();
    JsonArray createCommands = commands.getAsJsonArray("create");
    JsonArray updateCommands = commands.getAsJsonArray("update");
    JsonArray deleteCommands = commands.getAsJsonArray("delete");
    CompletableFuture<QueryResult> statusPromise = connection.sendQuery("BEGIN");

    // Process create commands
    for (JsonElement createCommandRaw : createCommands) {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        JsonObject createCommand = createCommandRaw.getAsJsonObject();
        String serviceName = createCommand.get("serviceName").getAsString();
        String serviceDescription = createCommand.get("serviceDescription").getAsString();
        String imageURL = createCommand.get("imageURL").getAsString();
        return connection.sendPreparedStatement(Constants.INSERT_CATALOG_STATEMENT,
            Arrays.asList(shopID, serviceName, serviceDescription, imageURL));
      });
    }

    // Process update commands
    for (JsonElement updateCommandRaw : updateCommands) {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        JsonObject updateCommand = updateCommandRaw.getAsJsonObject();
        Long serviceID = updateCommand.get("serviceID").getAsLong();
        String serviceName = updateCommand.get("serviceName").getAsString();
        String serviceDescription = updateCommand.get("serviceDescription").getAsString();
        String imageURL = updateCommand.get("imageURL").getAsString();
        return connection.sendPreparedStatement(Constants.UPDATE_CATALOG_STATEMENT,
            Arrays.asList(serviceName, serviceDescription, imageURL, serviceID));
      });
    }

    // Process delete commands
    for (JsonElement deleteCommandRaw : deleteCommands) {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        Long serviceID = deleteCommandRaw.getAsJsonObject().get("serviceID").getAsLong();
        return connection.sendPreparedStatement(Constants.DELETE_CATALOG_STATEMENT, Arrays.asList(serviceID));
      });
    }

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
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    });

  }

  /*
   * Route to handle shop upserts for a merchant Returns: Inserted Shop Instance
   */

  @PostMapping("/v1/merchants/{merchantID}/shops")
  public @ResponseBody CompletableFuture<Shop> insertShop(@PathVariable String merchantID, @RequestBody String shopDetailsString)
      throws InterruptedException, ExecutionException {
    JsonObject newShopDetails = JsonParser.parseString(shopDetailsString).getAsJsonObject();
    List<Object> queryParams = Arrays.asList(
      newShopDetails.get("shopName").getAsString(),
      newShopDetails.get("typeOfService").getAsString(),
      newShopDetails.get("latitude").getAsString(),
      newShopDetails.get("longitude").getAsString(),
      newShopDetails.get("addressLine1").getAsString(),
      merchantID
    );
    return connection
    .sendPreparedStatement(Constants.SHOP_INSERT_STATEMENT, queryParams)
    .thenCompose((queryResult) -> {
      long shopID = ((MySQLQueryResult) queryResult).getLastInsertId();
      newShopDetails.addProperty("shopID", shopID);
      return publishMessage(Long.toString(shopID));
    }).exceptionally(e -> {
      // TODO: Handle errors
      return "";
    }).thenApply((publishPromise) -> {
      newShopDetails.addProperty("merchantID", merchantID);
      return Shop.create(newShopDetails);
    });
  }

  /*
   * Expects: All shop details (including the ShopID of the shop to be Updated)
   */

  @PutMapping("/v1/merchants/{merchantID}/shops/{shopID}")
  public CompletableFuture<Shop> updateShop(@PathVariable String merchantID, @PathVariable Long shopID, @RequestBody String shopDetailsString) {
    JsonObject newShopDetails = JsonParser.parseString(shopDetailsString).getAsJsonObject();
    List<Object> queryParams = Arrays.asList(
      newShopDetails.get("shopName").getAsString(),
      newShopDetails.get("typeOfService").getAsString(),
      newShopDetails.get("latitude").getAsString(),
      newShopDetails.get("longitude").getAsString(),
      newShopDetails.get("addressLine1").getAsString(),
      shopID
    );
    return connection
    .sendPreparedStatement(Constants.SHOP_UPDATE_STATEMENT, queryParams)
    .thenCompose((QueryResult queryResult) -> {
      return publishMessage(Long.toString(shopID));
    }).exceptionally(e -> {
      e.printStackTrace();
      logger.error(String.format("ShopID %s: Could not update or publish to PubSub. Exited exceptionally!",
      Long.toString(shopID)));
      // TODO: Handle errors
      return "";
    }).thenApply((publishPromise) -> {
      newShopDetails.addProperty("shopID", shopID);
      newShopDetails.addProperty("merchantID", merchantID);
      return Shop.create(newShopDetails);
    });
  }

  public CompletableFuture<String> publishMessage(String message) {
    return this.publisher.publish(Constants.PUBSUB_URL, message).completable();
  }

}
