package com.example.hyperlocal;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.github.jasync.sql.db.mysql.MySQLQueryResult;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ShopController {

  private final PubSubTemplate publisher;

  public ShopController(PubSubTemplate pubSubTemplate) {
    this.publisher = pubSubTemplate;
  }

    /* TESTING ONLY: list of all shops */

    @GetMapping("/shops/")
    public CompletableFuture<List> getAllShops() {
      Connection connection = MySQLConnectionBuilder.createConnectionPool(
          "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
      CompletableFuture<List> future = CompletableFuture.supplyAsync(() -> {
        CompletableFuture<QueryResult> queryResult = connection
            .sendPreparedStatement("Select * from Shops;");
        return queryResult.join().getRows();
      });
      return future;
    }

  /* Route to handle shop upserts for a merchant
    
    Expects: 
    {
      "ShopID":       // should it be autoincrement? if not, store in database?
      "MerchantID": ---,
      "ShopName": ---,
      "TypeOfService": ---,
      "Latitude": ---,
      "Longitude": ---
      "AddressLine1": ---
    }

    returns: upserted Shop Instance
  */

  @PostMapping("/upsert/shop")
  public CompletableFuture<Shop> upsertShop(@RequestBody String postInputString) {
    JsonObject jsonObject = JsonParser.parseString(postInputString).getAsJsonObject();

    Connection connection = MySQLConnectionBuilder.createConnectionPool(
        "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");

    CompletableFuture<Shop> upsertedShopDetails = connection
      .sendPreparedStatement("SELECT COUNT(1) from Shops where ShopID = ?;",
          Arrays.asList(jsonObject.get("ShopID").getAsLong()))
      .thenCompose(queryResult -> {
        if (queryResult.getRows().size() != 0) {
          return connection.sendPreparedStatement(
            "UPDATE `Shops` SET "
            .concat(String.format("`ShopName` = '%s',", jsonObject.get("ShopName").getAsString()))
            .concat(String.format("`TypeOfService` = '%s',", jsonObject.get("TypeOfService").getAsString()))
            .concat(String.format("`Latitude` = '%s',", jsonObject.get("Latitude").getAsDouble()))
            .concat(String.format("`Longitude` = '%s',", jsonObject.get("Longitude").getAsDouble()))
            .concat(String.format("`AddressLine1` = '%s' ", jsonObject.get("AddressLine1").getAsString()))
            .concat(String.format("WHERE `ShopID`= %s;", jsonObject.get("ShopID").getAsLong()))
          );
        }
        else{
          return connection.sendPreparedStatement(
            "INSERT INTO `Shops` (ShopName, TypeOfService, ShopID, Latitude, Longitude, AddressLine1, MerchantID) VALUES"
            .concat(String.format("'%s' ,", jsonObject.get("ShopName").getAsString()))
            .concat(String.format("'%s' ,", jsonObject.get("TypeOfService").getAsString()))
            .concat(String.format("'%s' ,", jsonObject.get("ShopID").getAsLong()))
            .concat(String.format("'%s' ,", jsonObject.get("Latitude").getAsDouble()))
            .concat(String.format("'%s' ,", jsonObject.get("Longitude").getAsDouble()))
            .concat(String.format("'%s' ,", jsonObject.get("AddressLine1").getAsString()))
            .concat(String.format("'%s' ;", jsonObject.get("MerchantID").getAsLong()))
          );
        }
      })
      .thenApply((result) -> {
        Shop shopDetailsObject = null;
        try {
          String publishPromise = publishMessage(postInputString).get();
          shopDetailsObject = new Shop(
            JsonParser.parseString(postInputString).getAsJsonObject()
          );
        } catch (Exception e) {
          System.out.println("Couldn't upsert to pubsub Queue");
        }
        return shopDetailsObject;
      }); 
    return upsertedShopDetails;
  }  

  public CompletableFuture<String> publishMessage(String message) throws InterruptedException, ExecutionException {
    return this.publisher.publish("projects/speedy-anthem-217710/topics/testTopic",message).completable();
  }
}