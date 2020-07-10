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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

// Rest Controller for Merchants

@RestController
public class MerchantController {

  private Connection connection;

  public MerchantController() {
    connection = MySQLConnectionBuilder.createConnectionPool(Constants.DATABASE_URL);
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

  @PutMapping("/v1/merchants/{merchantID}")
  public CompletableFuture<Merchant> updateMerchant(@PathVariable String merchantID, @RequestBody String postInputString) {
    // TODO: Rewrite method with ID JWT & phone JWT verification
    JsonObject merchantJson = JsonParser.parseString(postInputString).getAsJsonObject();
    String merchantName = merchantJson.get("merchantName").getAsString();
    String merchantPhone = merchantJson.get("merchantPhone").getAsString();
    return connection
    .sendPreparedStatement(Constants.MERCHANT_UPDATE_STATEMENT, Arrays.asList(merchantName, merchantPhone, merchantID))
    .thenApply((resp) -> {
      return new Merchant(merchantID, merchantName, merchantPhone);
    });
  }

  @PostMapping("/v1/merchants")
  public CompletableFuture<Merchant> insertMerchant(@RequestBody String postInputString) {
    // TODO: Rewrite method with ID JWT & phone JWT verification
    JsonObject merchantJson = JsonParser.parseString(postInputString).getAsJsonObject();
    List<Object> queryParams = Arrays.asList(
      merchantJson.get("merchantID").getAsString(), 
      merchantJson.get("merchantName").getAsString(), 
      merchantJson.get("merchantPhone").getAsString()
    );
    return connection
    .sendPreparedStatement(Constants.MERCHANT_INSERT_STATEMENT, queryParams)
    .thenApply((resp) -> {
      return new Merchant(merchantJson);
    });
  }

}