package com.hyperlocal.server;

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
import com.hyperlocal.server.Data.SearchSnippet;
import com.hyperlocal.server.Data.Shop;
import com.hyperlocal.server.Data.ShopDetails;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.GeoDistanceSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
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

  private Utilities util = new Utilities();;

  private static final Logger logger = LogManager.getLogger(ShopController.class);

  public ShopController(PubSubTemplate pubSubTemplate) {
    this.publisher = pubSubTemplate;
    connection = MySQLConnectionBuilder.createConnectionPool(Constants.DATABASE_URL);
  }

  /**
   * Search Endpoint which returns list of Search Snippets
   * @param query The search query string
   * @param queryRadius The geographical radius to search in with default 3km
   * @param latitude The latitude to measure queryRadius from
   * @param longitude The longitude to measure queryRadius from
   * @return CompletableFuture of List of Search Snippets matching the search criteria
   */
  @GetMapping("/v1/shops")
  public CompletableFuture<List<SearchSnippet>> getDataFromSearchIndex(
      @RequestParam(value = "query", required = false, defaultValue = "") String query,
      @RequestParam(value = "queryRadius", required = false, defaultValue = "3km") String queryRadius,
      @RequestParam String latitude, 
      @RequestParam String longitude) {

    List<Long> shopIDList = new ArrayList<Long>();
    HashMap<Long, List<String>> mapShopIDtoHighlight = new HashMap<Long, List<String>>();
    List<SearchSnippet> searchSnippets = new ArrayList<SearchSnippet>();

    List<String> fieldsToSearch = new ArrayList<String>();
    fieldsToSearch.add("shopname");
    fieldsToSearch.add("typeofservice");
    fieldsToSearch.add("merchantname");
    fieldsToSearch.add("catalogitems");

    // GeoDistance query for filtering everything in a radius
    GeoDistanceQueryBuilder filterOnDistance = QueryBuilders.geoDistanceQuery("pin.location")
        .point(Double.parseDouble(latitude), Double.parseDouble(longitude)).distance(queryRadius);

    // Boolean query to hold conditions of both Matchquery and Geodistance query
    BoolQueryBuilder boolMatchQueryWithDistanceFilter;

    // Build Highlight query to get matched phrases
    HighlightBuilder highLightBuilder = new HighlightBuilder().requireFieldMatch(false);

    // We are spliting the tokens as unigram, bigram, trigram and also indexing their prefixes to ensure both infix and prefix match
    for (String fieldName : fieldsToSearch) {
      highLightBuilder = highLightBuilder
                          .field(fieldName)
                          .field(String.format("%s._2gram", fieldName))
                          .field(String.format("%s._3gram", fieldName))
                          .field(String.format("%s._index_prefix", fieldName));
    }

    // Create a match query
    // default: If no input string provided, browse query (i.e match all)
    // else match on input text
    if (query.equals("")) {
      boolMatchQueryWithDistanceFilter = QueryBuilders.boolQuery().must(QueryBuilders.matchAllQuery())
          .filter(filterOnDistance);
    } else {
      boolMatchQueryWithDistanceFilter = QueryBuilders.boolQuery()
          .must(QueryBuilders.disMaxQuery()
              .add(QueryBuilders.multiMatchQuery(query, "shopname", "typeofservice", "merchantname", "catalogitems")
                  .fuzziness("AUTO"))
              .add(QueryBuilders.multiMatchQuery(query, "shopname", "typeofservice", "merchantname", "catalogitems")
                  .type("bool_prefix")))
          .filter(filterOnDistance);
    }

    // Add bool query, geodistance filter, highlighter and sorting to search Request
    SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
    searchSourceBuilder.query(boolMatchQueryWithDistanceFilter);
    searchSourceBuilder.highlighter(highLightBuilder);
    searchSourceBuilder.fetchSource(false);
    searchSourceBuilder
      .sort(new ScoreSortBuilder())
      .sort(new GeoDistanceSortBuilder("pin.location", Double.parseDouble(latitude), Double.parseDouble(longitude)));

    return util.getResponseBody(Constants.SEARCH_INDEX_URL, searchSourceBuilder.toString())
        .thenCompose((responseString) -> {
          // Empty {} is returned by search Index if nothing matches
          if (!responseString.equals("{}")) {
            JsonObject obj = JsonParser.parseString(responseString).getAsJsonObject();
            JsonArray documentListJson = obj.get("hits").getAsJsonObject().get("hits").getAsJsonArray();
            for (JsonElement document : documentListJson) {
              Long shopID = document.getAsJsonObject().get("_id").getAsLong();
              shopIDList.add(shopID);
              List<String> matchedPhrases = new ArrayList<String>();

              // Highlights are only present if the query string wasn't empty, empty matchedphrases otherwise
              if (document.getAsJsonObject().has("highlight")) {
                JsonObject highlightObject = document.getAsJsonObject().get("highlight").getAsJsonObject();
                for (String highlightKey : highlightObject.keySet()) {
                  JsonArray matchedPhrasesList = highlightObject.get(highlightKey).getAsJsonArray();
                  for (JsonElement phrase : matchedPhrasesList) {
                    matchedPhrases.add(phrase.getAsString());
                  }
                }
              }
              mapShopIDtoHighlight.put(shopID, matchedPhrases);
            }
          }
 
          return getShopDetailsListByShopIDBatch(shopIDList);
        }).thenApply((shopDetailsList) -> {

          // With shopdetails and matched phrases available, create search snippets
          for (ShopDetails shopDetails : shopDetailsList) {
            Long shopID = shopDetails.shop.shopID();
            SearchSnippet searchSnippet = SearchSnippet.create(shopDetails, mapShopIDtoHighlight.get(shopID));
            searchSnippets.add(searchSnippet);
          }
          return searchSnippets;
        });
  }

  /**
   * Return a list of {@link ShopDetails} objects  
   * @param shopIDList List of shopIDs to get ShopDetails of
   * @return CompletableFuture of list of ShopDetails
   */
  public CompletableFuture<List<ShopDetails>> getShopDetailsListByShopIDBatch(List<Long> shopIDList) {

    HashMap<String, Merchant> mapMerchantIdToMerchant = new HashMap<String, Merchant>();
    HashMap<Long, ShopDetails> mapShopIdToShopDetails = new HashMap<Long, ShopDetails>();
    List<ShopDetails> shopsList = new ArrayList<ShopDetails>();

    if (shopIDList.isEmpty()) {
      return CompletableFuture.completedFuture(shopsList);
    }

    String shopPreparedStatementPlaceholder = Utilities.getPlaceHolderString(shopIDList.size());

    // Fetch All Shops in ShopIDList and store their merchantIDs in a List
    return connection
      .sendPreparedStatement(
        String.format(Constants.SELECT_SHOPS_BATCH_QUERY, shopPreparedStatementPlaceholder),shopIDList

      ).thenCompose((QueryResult result) -> {
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
        String merchantPreparedStatementPlaceholder = Utilities.getPlaceHolderString(merchantIDList.size());
        return connection
          .sendPreparedStatement(
            String.format(Constants.SELECT_MERCHANT_BATCH_QUERY, merchantPreparedStatementPlaceholder),
            merchantIDList
          );

      // Map All merchantIDs to their Merchants and Update ShopDetails with merchant
      // Information
      }).thenCompose((result) -> {
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
            String.format(Constants.SELECT_CATALOG_BATCH_QUERY, shopPreparedStatementPlaceholder), shopIDList
          );
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

  /**
   * Return the list of Shops owned by a Merchant
   * @param merchantID Unique ID of the merchant (SUB)
   * @return CompletableFuture of List of Shops owned by the Merchant
   */
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

  /** 
   * Fetch catalog, shop & merchant details by shopID 
   * @param shopID The Unique ID of the shop 
   * @return CompletableFuture of ShopDetails of the Shop
  */
  @GetMapping("/v1/shops/{shopID}")
  public CompletableFuture<ShopDetails> getShopDetails(@PathVariable Long shopID) {

    CompletableFuture<ShopDetails> shopDetailsPromise = connection
        .sendPreparedStatement(Constants.SELECT_SHOP_DETAILS_STATEMENT, Arrays.asList(shopID))
        .thenApply((QueryResult shopDetailsQueryResult) -> {
          ResultSet shopDetailsRecords = shopDetailsQueryResult.getRows();
          if(shopDetailsRecords.size() == 0)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Requested shop was not found.");
          RowData shopDetailsRecord = shopDetailsRecords.get(0);
          ShopDetails shopDetails = new ShopDetails();
          shopDetails.setShop(Shop.create(shopDetailsRecord));
          shopDetails.setMerchant(Merchant.create(shopDetailsRecord));
          if(shopDetailsRecord.get("ServiceName") == null) return shopDetails;
          for(RowData catalogItem : shopDetailsRecords) {
            shopDetails.addCatalogItem(CatalogItem.create(catalogItem));
          }
          return shopDetails;  
        });

    return shopDetailsPromise;
  }

  /**
   * Update the catalog for a shop
   * @param merchantID The unique ID of the Merchant
   * @param shopID The unique ID of the Shop
   * @param updatePayload JSON payload containing Data to update
   * @return {@code HashMap<"success",true>} if upsert is successful throws exception otherwise
   * @throws ResponseStatusException
   */
  @PostMapping("/v1/merchants/{merchantID}/shops/{shopID}/catalog:batchUpdate")
  public CompletableFuture<HashMap<String, Object>> upsertCatalog(@PathVariable String merchantID, @PathVariable Long shopID, @RequestBody String updatePayload) {
    JsonObject commands = JsonParser.parseString(updatePayload).getAsJsonObject();
    JsonArray createCommands = commands.getAsJsonArray("create");
    JsonArray updateCommands = commands.getAsJsonArray("update");
    JsonArray deleteCommands = commands.getAsJsonArray("delete");
    CompletableFuture<QueryResult> statusPromise = connection.sendQuery("BEGIN");
    
    if(createCommands.size() > 0) {
      String[] insertPlaceholdersArray = new String[createCommands.size()];
      Arrays.fill(insertPlaceholdersArray, Constants.INSERT_CATALOG_PLACEHOLDER);
      String insertPlaceholders = String.join(", ", insertPlaceholdersArray);
      String insertQuery = String.format(Constants.INSERT_CATALOG_STATEMENT, insertPlaceholders);
      List<Object> insertQueryParameters = new ArrayList<Object>();
      for(JsonElement createCommandRaw : createCommands)
      {
          JsonObject createCommand = createCommandRaw.getAsJsonObject();
          insertQueryParameters.add(shopID);
          insertQueryParameters.add(createCommand.get("serviceName").getAsString());
          insertQueryParameters.add(createCommand.get("serviceDescription").getAsString());
          insertQueryParameters.add(createCommand.get("imageURL").getAsString());
      }
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        return connection.sendPreparedStatement(insertQuery, insertQueryParameters);
      });
    }
    

    for(JsonElement updateCommandRaw : updateCommands) {
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        JsonObject updateCommand = updateCommandRaw.getAsJsonObject();
        Long serviceID = updateCommand.get("serviceID").getAsLong();
        String serviceName = updateCommand.get("serviceName").getAsString();
        String serviceDescription = updateCommand.get("serviceDescription").getAsString();
        String imageURL = updateCommand.get("imageURL").getAsString();
        return connection.sendPreparedStatement(Constants.UPDATE_CATALOG_STATEMENT, Arrays.asList(serviceName, serviceDescription, imageURL, serviceID));
      });
    }

    if(deleteCommands.size() > 0) {
      String deletePlaceholders = Utilities.getPlaceHolderString(deleteCommands.size());
      String deleteQuery = String.format(Constants.DELETE_CATALOG_STATEMENT, deletePlaceholders);
      List<Object> deleteQueryParameters = new ArrayList<Object>();
      for(JsonElement deleteCommandRaw : deleteCommands) {
        deleteQueryParameters.add(deleteCommandRaw.getAsJsonObject().get("serviceID").getAsLong());
      }
      statusPromise = statusPromise.thenCompose((QueryResult result) -> {
        return connection.sendPreparedStatement(deleteQuery, deleteQueryParameters);
      });
    }
 
    return statusPromise
      .thenCompose((QueryResult result) -> {
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

  /**
   * Upsert Shops for a merchant
   * @param merchantID Unique ID of the merchant
   * @param shopDetailsString JSON string containtaing {@link Shop} information
   * @return CompletableFuture of the upserted Shop
   * @throws InterruptedException
   * @throws ExecutionException
   */

  @PostMapping("/v1/merchants/{merchantID}/shops")
  public @ResponseBody CompletableFuture<Shop> insertShop(@PathVariable String merchantID,
      @RequestBody String shopDetailsString) throws InterruptedException, ExecutionException {
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
      return "";
    }).thenApply((publishPromise) -> {
      newShopDetails.addProperty("merchantID", merchantID);
      return Shop.create(newShopDetails);
    });
  }


  /** 
   * Update a shop's details
   * @param merchantID The Unique ID of the merchant
   * @param shopID The Unique ID of the Shop
   * @param shopDetailsString JSON serialized {@link Shop} object of updated Shop
   * @return Updated shop as CompletableFuture
   */
  @PutMapping("/v1/merchants/{merchantID}/shops/{shopID}")
  public CompletableFuture<Shop> updateShop(@PathVariable String merchantID, @PathVariable Long shopID,
      @RequestBody String shopDetailsString) {
    JsonObject newShopDetails = JsonParser.parseString(shopDetailsString).getAsJsonObject();
    List<Object> queryParams = Arrays.asList(
      newShopDetails.get("shopName").getAsString(),
      newShopDetails.get("typeOfService").getAsString(), 
      newShopDetails.get("latitude").getAsString(),
      newShopDetails.get("longitude").getAsString(),
      newShopDetails.get("addressLine1").getAsString(),
      shopID,
      merchantID
    );
    return connection
    .sendPreparedStatement(Constants.SHOP_UPDATE_STATEMENT, queryParams)
    .thenCompose((QueryResult queryResult) -> {
      return publishMessage(Long.toString(shopID));
    }).exceptionally(e -> {
      logger.error(String.format("ShopID %s: Could not update or publish to PubSub. Exited exceptionally!",
          Long.toString(shopID)));
      return "";
    }).thenApply((publishPromise) -> {
      newShopDetails.addProperty("shopID", shopID);
      newShopDetails.addProperty("merchantID", merchantID);
      return Shop.create(newShopDetails);
    });
  }

  /** 
   * Publish a message to Pub Sub
   * @param message The message to be pushed to Pub Sub
   * @return The CompletableFuture of the call
   */
  public CompletableFuture<String> publishMessage(String message) {
    return this.publisher.publish(Constants.PUBSUB_URL, message).completable();
  }
}
