package com.example.hyperlocal;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.google.api.client.json.JsonString;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Rest Controller for Merchants
@RestController
public class MerchantController {

  @GetMapping("/merchants/{id}")
  public CompletableFuture<List> getCatalog(@PathVariable Integer id) {

    Connection connection = MySQLConnectionBuilder.createConnectionPool(
        "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
    CompletableFuture<List> future = CompletableFuture.supplyAsync(() -> {
      CompletableFuture<QueryResult> queryResult = connection
          .sendPreparedStatement(String.format("Select * from Merchants where MerchantID = %s", id));
      return queryResult.join().getRows().get(0);
    });
    return future;
  }

  @PostMapping("/merchants") 
  public void addCatalogItem(@RequestBody String postInputString) {
    JsonObject jsonObject = JsonParser.parseString(postInputString).getAsJsonObject();
    
    Merchants newMerchant = new Merchants(
    jsonObject.get("ShopName").getAsString(),
     jsonObject.get("Latitude").getAsDouble(),
     jsonObject.get("Longitude").getAsDouble(),
     jsonObject.get("TypeOfService").getAsString(),
     jsonObject.get("AddressLine1").getAsString()
    );

    Connection connection = MySQLConnectionBuilder.createConnectionPool(
      "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
      connection.sendPreparedStatement( 
          String.format("INSERT INTO Merchants"
          .concat("(`ShopName`, `Latitude`, `Longitude`, `TypeOfService`, `AddressLine1`)") 
          .concat("VALUES (")
          .concat(String.format("'%s',", newMerchant.getShopName()))
          .concat(String.format("%s,", newMerchant.getLatitude()))
          .concat(String.format("%s,", newMerchant.getLongitude()))
          .concat(String.format("'%s',", newMerchant.getTypeOfService()))
          .concat(String.format("'%s');", newMerchant.getAddressLine1())))
      );    
  }  

  @PostMapping("merchants/update")
  public void updateCatalogItem() {
    Connection connection = MySQLConnectionBuilder.createConnectionPool(
      "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
      connection.sendPreparedStatement("UPDATE Merchants SET ShopName =' a new name' where MerchantID=13");    
  } 
}