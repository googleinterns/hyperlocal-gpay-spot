package com.hyperlocal.server;

import com.hyperlocal.server.Data.*;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

// Rest Controller for Merchants

@RestController
public class MerchantController {

  private static final String DATABASE_URL = "jdbc:mysql://10.124.32.3:3306/hyperlocal";
  private Connection connection;
  private static final String MERCHANT_UPDATE_STATEMENT = "UPDATE `Merchants` SET `MerchantName` = ?, `MerchantPhone` = ? WHERE `MerchantID`= ?;";
  private static final String MERCHANT_INSERT_STATEMENT = "INSERT into `Merchants` (`MerchantID`, `MerchantName`, `MerchantPhone`) values (?,?,?);";

  public MerchantController() {
    connection = MySQLConnectionBuilder.createConnectionPool(DATABASE_URL);
  }

  /*
   * Update details of a merchant
   * 
   * Expects: A Json String from client as Input with the following format:
   * 
   * { "MerchantID": <Merchant sub id here>, "MerchantName": <Updated Name of the
   * merchant here>, "MechantPhone": <Updated PhoneNumber of the merchant here> }
   * 
   * Returns: Merchant Object with updated Details if exists, else return inserted
   * Merchant object
   */

  @CrossOrigin(origins = {"http://localhost:3000", "https://speedy-anthem-217710.an.r.appspot.com", "https://microapps.google.com"})
  @PostMapping("/api/update/merchant/")
  public CompletableFuture<Merchant> updateMerchant(@RequestBody String postInputString) {
    JsonObject newMerchant = JsonParser.parseString(postInputString).getAsJsonObject();
    return updateMerchantDetails(newMerchant).thenApply((result) -> {
      return new Merchant(newMerchant);
    });
  }

  @CrossOrigin(origins = {"http://localhost:3000", "https://speedy-anthem-217710.an.r.appspot.com", "https://microapps.google.com"})
  @PostMapping("/api/insert/merchant")
  public CompletableFuture<Merchant> insertMerchant(@RequestBody String postInputString) {
    JsonObject newMerchant = JsonParser.parseString(postInputString).getAsJsonObject();
    return insertNewMerchant(newMerchant).thenApply((result) -> {
      return new Merchant(newMerchant);
    }).exceptionally((ex) -> {
        ex.printStackTrace();
        return null;
    });
  }

  /* Helper function to call database and update it */
  public CompletableFuture<QueryResult> updateMerchantDetails(JsonObject merchantDetails) {
    String UpdateQueryParameters[] = new String[] { merchantDetails.get("merchantName").getAsString(),
        merchantDetails.get("merchantPhone").getAsString(), merchantDetails.get("merchantID").getAsString() };
    return connection.sendPreparedStatement(MERCHANT_UPDATE_STATEMENT, Arrays.asList(UpdateQueryParameters));
  }

  /* Calls database and inserts a new Merchant record */
  public CompletableFuture<QueryResult> insertNewMerchant(JsonObject merchantDetails) {
    String InsertQueryParameters[] = new String[] { merchantDetails.get("merchantID").getAsString(),
        merchantDetails.get("merchantName").getAsString(), merchantDetails.get("merchantPhone").getAsString() };
    return connection.sendPreparedStatement(MERCHANT_INSERT_STATEMENT, Arrays.asList(InsertQueryParameters));
  }
}