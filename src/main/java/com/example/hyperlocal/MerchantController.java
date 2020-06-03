package com.example.hyperlocal;

import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Rest Controller for Merchants
@RestController
public class MerchantController {

  private final PubSubTemplate publisher;

  public MerchantController(PubSubTemplate pubSubTemplate) {
    this.publisher = pubSubTemplate;
  }

  @GetMapping("/get/shop/{shopID}")
  public CompletableFuture<String> getShopDetails(@PathVariable Integer shopID) {
    // DB Connection
    Connection connection = MySQLConnectionBuilder.createConnectionPool("jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
    
    // Promise: returns JSON string
    CompletableFuture<String> shopPromise = connection
      .sendPreparedStatement("SELECT * from `Shops` WHERE `ShopID` = ?;", Arrays.asList(shopID))
      .thenApply((QueryResult queryResult) -> {
        // Get Shop details
        ResultSet rows = queryResult.getRows();
        if(rows.size() == 0) return null;
        else
        {
          RowData shopData = rows.get(0);
          return new Shop(shopData);
        }
      }).thenApply((Shop shop) -> {
        // Shop exists?
        if(shop == null) return new Gson().toJson(Collections.singletonMap("error", "Shop not found."));
        else
        {
          // Get Merchant Details
          CompletableFuture<Merchant> merchantPromise = connection
            .sendPreparedStatement("SELECT * from `Merchants` WHERE `MerchantID` = ?;", Arrays.asList(shop.MerchantID))
            .thenApply((QueryResult merchantQuery) -> {
              RowData merchantData = merchantQuery.getRows().get(0);
              return new Merchant((Long)merchantData.get(0), (String)merchantData.get(1), (String)merchantData.get(2));
            });
          
          // Get Catalog Details
          CompletableFuture<ArrayList<Service>> catalogPromise = connection
            .sendPreparedStatement("SELECT * from `Catalog` WHERE `ShopID` = ?;", Arrays.asList(shop.ShopID))
            .thenApply((QueryResult catalogQuery) -> {
              ResultSet catalogSet = catalogQuery.getRows();
              ArrayList<Service> serviceList = new ArrayList<Service>();
              for(RowData serviceRow : catalogSet) serviceList.add(new Service(serviceRow));
              return serviceList;
            });
          
          // Put everything into HashMap
          HashMap<String, Object> shopMap = new HashMap<String, Object>();
          shopMap.put("ShopDetails", shop);
          try
          {
            shopMap.put("MerchantDetails", merchantPromise.get());
            shopMap.put("Catalog", catalogPromise.get());
          }
          catch(Exception e)
          {
            return new Gson().toJson(Collections.singletonMap("error", "Internal Server Error: Could not fetch details"));
          }

          // Convert HashMap to JSON string
          return new Gson().toJson(shopMap);
        }
      });

    return shopPromise;
  }


  /* Route handlers for the Merchant API */

  @GetMapping("/merchants/")
  public CompletableFuture<List> getCatalog(/*@PathVariable Integer id*/) {
    Connection connection = MySQLConnectionBuilder.createConnectionPool(
        "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
    CompletableFuture<List> future = CompletableFuture.supplyAsync(() -> {
      CompletableFuture<QueryResult> queryResult = connection
          .sendPreparedStatement("Select * from Merchants;");//String.format("Select * from Merchants where MerchantID = %s", id));
      return queryResult.join().getRows();
    });
    return future;
  }


  @PostMapping("/merchants/new") 
  public CompletableFuture<Merchants> addMerchant(@RequestBody String postInputString) {
    JsonObject jsonObject = JsonParser.parseString(postInputString).getAsJsonObject(); 
      
    Merchants newMerchant = new Merchants(
    jsonObject.get("MerchantID").getAsInt(),
    jsonObject.get("MerchantName").getAsString(),
    jsonObject.get("MerchantPhone").getAsString()
    );

    Connection connection = MySQLConnectionBuilder.createConnectionPool(
      "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");   
    
    CompletableFuture<Merchants> insertedMerchant = connection
        .sendPreparedStatement(
      String.format("INSERT INTO Merchants"
      .concat("(`MerchantID`, `MerchantName`, `MerchantPhone`)") 
      .concat("VALUES (")
      .concat(String.format("'%s',", newMerchant.getMerchantID()))
      .concat(String.format("'%s',", newMerchant.getMerchantName()))
      .concat(String.format("'%s');", newMerchant.getMerchantPhone())))
    ).thenApply((result) -> {
      publishMessage(postInputString);
      return newMerchant;
    });

    return insertedMerchant;
  }  


  @PostMapping("/merchants/update/{id}")
  public void updateCatalogItem(@RequestBody String postInputString,
       @RequestParam Integer MerchantID) {
    Connection connection = MySQLConnectionBuilder.createConnectionPool(
      "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
      connection.sendPreparedStatement("UPDATE Merchants SET MerchantName =' a new name' where MerchantID=1");    
  } 

  public void publishMessage(String message) {
    this.publisher.publish("projects/speedy-anthem-217710/topics/testTopic",message);
  }

}