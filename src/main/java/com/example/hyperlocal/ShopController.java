package com.example.hyperlocal;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class ShopController {

  private final PubSubTemplate publisher;
  private final Connection connection;

  public ShopController(PubSubTemplate pubSubTemplate) {
    this.publisher = pubSubTemplate;
    connection = MySQLConnectionBuilder.createConnectionPool(
        "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
  }

  /*
   * Route to handle shop upserts for a merchant
   * returns: upserted Shop Instance
  */

  @PostMapping("/upsert/shop")
  public CompletableFuture<String> upsertShop(@RequestBody String postInputString) {
    JsonObject shopDataAsJson = JsonParser.parseString(postInputString).getAsJsonObject();

    return connection.sendPreparedStatement("SELECT * from Shops where MerchantID = ?;",
      Arrays.asList(shopDataAsJson.get("MerchantID").getAsLong()))
    .thenCompose(queryResult -> {
      if (queryResult.getRows().size() != 0) {
        return updateShopDetails(shopDataAsJson);
      } else {
        return insertNewShop(shopDataAsJson);
      }
    })    
    .thenCompose((queryResult) -> {
      return connection.sendPreparedStatement(
        "Select * from Shops where MerchantID = ?;", 
        Arrays.asList(shopDataAsJson.get("MerchantID").toString())
      );
    })
    .thenCompose((QueryResult queryResult) -> {
      return CompletableFuture.completedFuture(new Shop(queryResult.getRows().get(0)));
    })
    .thenCompose((upsertedShop) -> {
      try {
        publishMessage(Long.toString(upsertedShop.ShopID));
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
      return CompletableFuture.completedFuture(new Gson().toJson(upsertedShop));
    });
  }  

  public CompletableFuture<String> publishMessage(String message) throws InterruptedException, ExecutionException {
    return this.publisher.publish("projects/speedy-anthem-217710/topics/testTopic",message).completable();
  }

  public CompletableFuture<QueryResult> updateShopDetails(JsonObject shopDataAsJsonObject) {
    return connection.sendPreparedStatement(
      "UPDATE `Shops` SET "
      .concat(String.format("`ShopName` = '%s',", shopDataAsJsonObject.get("ShopName").getAsString()))
      .concat(String.format("`TypeOfService` = '%s',", shopDataAsJsonObject.get("TypeOfService").getAsString()))
      .concat(String.format("`Latitude` = '%s',", shopDataAsJsonObject.get("Latitude").getAsDouble()))
      .concat(String.format("`Longitude` = '%s',", shopDataAsJsonObject.get("Longitude").getAsDouble()))
      .concat(String.format("`AddressLine1` = '%s' ", shopDataAsJsonObject.get("AddressLine1").getAsString()))
      .concat(String.format("WHERE `MerchantID`= %s;", shopDataAsJsonObject.get("MerchantID").getAsLong()))
    );
  }

  public CompletableFuture<QueryResult> insertNewShop(JsonObject shopDatJsonObject) {
    return connection.sendPreparedStatement(
      "INSERT INTO `Shops` (ShopName, TypeOfService, Latitude, Longitude, AddressLine1, MerchantID, ShopID) VALUES ("
      .concat(String.format("'%s' ,", shopDatJsonObject.get("ShopName").getAsString()))
      .concat(String.format("'%s' ,", shopDatJsonObject.get("TypeOfService").getAsString()))
      .concat(String.format("'%s' ,", shopDatJsonObject.get("Latitude").getAsString()))
      .concat(String.format("'%s' ,", shopDatJsonObject.get("Longitude").getAsString()))
      .concat(String.format("'%s' ,", shopDatJsonObject.get("AddressLine1").getAsString()))
      .concat(String.format("'%s' ,", shopDatJsonObject.get("MerchantID").getAsString()))
      .concat(String.format("'%s') ;", shopDatJsonObject.get("ShopID").getAsString()))
    );
  }
}