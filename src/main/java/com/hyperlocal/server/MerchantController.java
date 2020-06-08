package com.hyperlocal.server;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Rest Controller for Merchants

@RestController
public class MerchantController {

  private static String DATABASE_URL; 
  private static Connection connection;
  private static String MERCHANT_UPDATE_PREPARED_STATEMENT;
  private static String MERCHANT_INSERT_PREPARED_STATEMENT;

  public MerchantController() {
    DATABASE_URL = "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal";
    connection = MySQLConnectionBuilder.createConnectionPool(DATABASE_URL);
    MERCHANT_INSERT_PREPARED_STATEMENT = "INSERT into `Merchants` (`MerchantID`, `MerchantName`, `MerchantPhone`) values (?,?,?);";
    MERCHANT_UPDATE_PREPARED_STATEMENT = "UPDATE `Merchants` SET `MerchantName` = ?, `MerchantPhone` = ? WHERE `MerchantID`= ?;";
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

  @PostMapping("/update/merchant/")
  public CompletableFuture<String> upsertMerchant(@RequestBody String postInputString) {
    JsonObject jsonObject = JsonParser.parseString(postInputString).getAsJsonObject();

    Merchant newMerchant = new Merchant(jsonObject);

    CompletableFuture<String> upsertedMerchantDetails = updateMerchantDetails(newMerchant).thenApply((result) -> {
      return new Gson().toJson(newMerchant);
    });
    return upsertedMerchantDetails;
  }

  @PostMapping("/insert/merchant")
  public CompletableFuture<String> insertMerchant(@RequestBody String postInputString) {
    JsonObject jsonObject = JsonParser.parseString(postInputString).getAsJsonObject();

    Merchant newMerchant = new Merchant(jsonObject);

    CompletableFuture<String> insertedMerchantDetails = insertNewMerchant(newMerchant).thenApply((result) -> {
      return new Gson().toJson(newMerchant);
    });
    return insertedMerchantDetails;
  }

  /* Helper function to call database and update it */
  public CompletableFuture<QueryResult> updateMerchantDetails(Merchant merchantDetails) {
    String UpdateQueryParameters[] = new String[] { merchantDetails.getMerchantName(),
        merchantDetails.getMerchantPhone() };
    return connection.sendPreparedStatement(MERCHANT_UPDATE_PREPARED_STATEMENT, Arrays.asList(UpdateQueryParameters));
  }

  /* Calls database and inserts a new Merchant record */
  public CompletableFuture<QueryResult> insertNewMerchant(Merchant merchantDetails) {
    String InsertQueryParameters[] = new String[] { Long.toString(merchantDetails.getMerchantID()),
        merchantDetails.getMerchantName(), merchantDetails.getMerchantPhone() };
    return connection.sendPreparedStatement(MERCHANT_INSERT_PREPARED_STATEMENT, Arrays.asList(InsertQueryParameters));
  }
}