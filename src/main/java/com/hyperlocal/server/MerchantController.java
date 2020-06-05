package com.hyperlocal.server;

import java.util.List;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;



// Rest Controller for Merchants

@RestController
public class MerchantController {
  
  /*
    Upserts details of a merchant
      - MerchantID
      - MerchantName
      - MerchantPhone

    Returns: Merchant Object with updated Details if exists, else return inserted Merchant object
  */

  @PostMapping("/upsert/merchants")
  public CompletableFuture<String> upsertMerchant(@RequestBody String postInputString) {
    JsonObject jsonObject = JsonParser.parseString(postInputString).getAsJsonObject();

    Merchant newMerchant = new Merchant(jsonObject);

    Connection connection = MySQLConnectionBuilder.createConnectionPool(
      "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");

    CompletableFuture<String> upsertedMerchantDetails = connection
      .sendPreparedStatement("SELECT * from Merchants where MerchantID = ?;",
          Arrays.asList(newMerchant.getMerchantID()))
      .thenCompose(queryResult -> {
        if (queryResult.getRows().size() != 0) {         
          return updateMerchantDetails(newMerchant);
        }
        else{
          return insertNewMerchant(newMerchant);
        }
      })
      .thenCompose((result) -> {
        return CompletableFuture.completedFuture(new Gson().toJson(newMerchant));
      });
      return upsertedMerchantDetails;
  }  

  /* Helper function to call database and update it */
  public CompletableFuture<QueryResult> updateMerchantDetails(Merchant merchantDetails) {
    Connection connection = MySQLConnectionBuilder.createConnectionPool(
      "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
    return connection.sendPreparedStatement(
      "UPDATE `Merchants` SET "
      .concat(String.format("`MerchantName` = '%s',", merchantDetails.getMerchantName()))
      .concat(String.format("`MerchantPhone` = '%s' ", merchantDetails.getMerchantPhone()))
      .concat(String.format("WHERE `MerchantID`= %s;", merchantDetails.getMerchantID())));
  }

  /* Calls database and inserts a new Merchant record */
  public CompletableFuture<QueryResult> insertNewMerchant(Merchant merchantDetails) {
    Connection connection = MySQLConnectionBuilder.createConnectionPool(
      "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
    String InsertQueryParameters[] = new String[]{Long.toString(merchantDetails.getMerchantID()), merchantDetails.getMerchantName(), merchantDetails.getMerchantPhone()};
      return connection.sendPreparedStatement(
        "Insert into `Merchants` (MerchantID, MerchantName, MerchantPhone) values (?,?,?);", Arrays.asList(InsertQueryParameters));
  }
}