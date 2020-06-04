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
import org.springframework.util.concurrent.ListenableFuture;
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
    
    // Container obj for shop details
    HashMap<String, Object> shopDetailsMap = new HashMap<String, Object>();

    // Promise: returns JSON string
    CompletableFuture<String> shopDetailsPromise = connection
        // Get Shop details  
      .sendPreparedStatement("SELECT * from `Shops` WHERE `ShopID` = ?;", Arrays.asList(shopID))
      .thenCompose((QueryResult shopQueryResult) -> {
        ResultSet shopRecords = shopQueryResult.getRows();
        if(shopRecords.size() == 0) throw new NullPointerException("Shop not found."); // No shop with supplied ShopID found
        RowData shopData = shopRecords.get(0);
        Shop shop = new Shop(shopData);
        shopDetailsMap.put("ShopDetails", shop);

        // Get Merchant Details
        return connection.sendPreparedStatement("SELECT * from `Merchants` WHERE `MerchantID` = ?;", Arrays.asList(shop.MerchantID));
      }).thenCompose((QueryResult merchantQueryResult) -> {
        RowData merchantData = merchantQueryResult.getRows().get(0);
        Merchant merchant = new Merchant((Long)merchantData.get(0), (String)merchantData.get(1), (String)merchantData.get(2));
        shopDetailsMap.put("MerchantDetails", merchant);
        
        // Get Catalog Details
        return connection.sendPreparedStatement("SELECT * from `Catalog` WHERE `ShopID` = ?;", Arrays.asList(shopID));
      }).thenApply((QueryResult catalogQueryResult) -> {
        ResultSet catalogResultSet = catalogQueryResult.getRows();
        ArrayList<Service> serviceList = new ArrayList<Service>();
        for(RowData serviceRow : catalogResultSet) serviceList.add(new Service(serviceRow));
        shopDetailsMap.put("Catalog", serviceList);
        
        // Container -> JSON String
        return new Gson().toJson(shopDetailsMap);
      }).exceptionally(ex -> {
        return new Gson().toJson(Collections.singletonMap("error", ex.getMessage()));
      });

    return shopDetailsPromise;
  }

  @PostMapping("/merchants/") 
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
      ListenableFuture<String> publishPromise = publishMessage(postInputString); // what to do with this?
      return newMerchant;
    });

    return insertedMerchant;
  }  

  public ListenableFuture<String> publishMessage(String message) {
    return this.publisher.publish("projects/speedy-anthem-217710/topics/testTopic",message);
  }

}