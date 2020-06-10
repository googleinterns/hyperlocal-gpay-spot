package com.hyperlocal.server;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.github.jasync.sql.db.mysql.MySQLQueryResult;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShopController {

  private final PubSubTemplate publisher;
  private static final String DATABASE_URL = "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal";;
  private static Connection connection;
  private static final String SHOP_UPDATE_PREPARED_STATEMENT = "UPDATE `Shops` SET `ShopName` = ?, `TypeOfService`=?, `Latitude` = ?, `Longitude` = ?, `AddressLine1` = ? WHERE `ShopID`=?;";
  private static final String SHOP_INSERT_PREPARED_STATEMENT = "INSERT INTO `Shops` (`ShopName`, `TypeOfService`, `Latitude`, `Longitude`, `AddressLine1`, `MerchantID`) VALUES (?,?,?,?,?,?);";;
  private static final String PUBSUB_URL = "projects/speedy-anthem-217710/topics/testTopic";
  private static final Logger logger = LogManager.getLogger(ShopController.class);

  public ShopController(PubSubTemplate pubSubTemplate) {
    this.publisher = pubSubTemplate;
    connection = MySQLConnectionBuilder.createConnectionPool(DATABASE_URL);
  }

  /*
   * Route to handle shop upserts for a merchant Returns: Inserted Shop Instance
   */
  @PostMapping("/insert/shop")
  public @ResponseBody CompletableFuture<String> insertShop(@RequestBody String shopDetailsString)
      throws InterruptedException, ExecutionException {
    JsonObject shopDataAsJson = JsonParser.parseString(shopDetailsString).getAsJsonObject();

    return insertNewShop(shopDataAsJson).thenCompose((QueryResult queryResult) -> {
      CompletableFuture<Long> shopID = CompletableFuture
          .completedFuture(((MySQLQueryResult) queryResult).getLastInsertId());
      return shopID;
    }).thenCompose((shopID) -> {
      shopDataAsJson.addProperty("ShopID", shopID);
      return CompletableFuture.completedFuture(shopID);
    }).thenCompose((shopID) -> {
      return publishMessage(Long.toString(shopID));
    }).exceptionally(e -> {
      logger.error(String.format("ShopID %s: Could not publish to PubSub. Exited exceptionally!",
          shopDataAsJson.get("ShopID").getAsString()));
      return "";
    }).thenApply((publishPromise) -> {
      return new Gson().toJson(shopDataAsJson);
    });
  }

  /*
   * Expects: All shop details (including the ShopID of the shop to be Updated)
   */

  @PostMapping("/update/shop/")
  public CompletableFuture<String> updateShop(@RequestBody String shopDetailsString) {
    JsonObject shopDataAsJson = JsonParser.parseString(shopDetailsString).getAsJsonObject();

    return updateShopDetails(shopDataAsJson).thenCompose((QueryResult queryResult) -> {
      CompletableFuture<Long> shopID = CompletableFuture
          .completedFuture(((MySQLQueryResult) queryResult).getLastInsertId());
      return shopID;
    }).thenCompose((shopID) -> {
      return publishMessage(Long.toString(shopID));
    }).exceptionally(e -> {
      logger.error(String.format("ShopID %s: Could not publish to PubSub. Exited exceptionally!",
          shopDataAsJson.get("ShopID").getAsString()));
      return "";
    }).thenApply((publishPromise) -> {
      return new Gson().toJson(shopDataAsJson);
    });
  }

  public CompletableFuture<String> publishMessage(String message) {
    return this.publisher.publish(PUBSUB_URL, message).completable();
  }

  public CompletableFuture<QueryResult> updateShopDetails(JsonObject shopDataJsonObject) {
    String UpdateQueryParameters[] = new String[] { shopDataJsonObject.get("ShopName").getAsString(),
        shopDataJsonObject.get("TypeOfService").getAsString(), shopDataJsonObject.get("Latitude").getAsString(),
        shopDataJsonObject.get("Longitude").getAsString(), shopDataJsonObject.get("AddressLine1").getAsString(),
        shopDataJsonObject.get("ShopID").getAsString() };

    return connection.sendPreparedStatement(SHOP_UPDATE_PREPARED_STATEMENT, Arrays.asList(UpdateQueryParameters));
  }

  public CompletableFuture<QueryResult> insertNewShop(JsonObject shopDataJsonObject) {
    String InsertQueryParameters[] = new String[] { shopDataJsonObject.get("ShopName").getAsString(),
        shopDataJsonObject.get("TypeOfService").getAsString(), shopDataJsonObject.get("Latitude").getAsString(),
        shopDataJsonObject.get("Longitude").getAsString(), shopDataJsonObject.get("AddressLine1").getAsString(),
        shopDataJsonObject.get("MerchantID").getAsString() };

    return connection.sendPreparedStatement(SHOP_INSERT_PREPARED_STATEMENT, Arrays.asList(InsertQueryParameters));
  }
}
