package com.hyperlocal.server;

import com.hyperlocal.server.Data.*;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

// Rest Controller for Merchants

@RestController
public class MerchantController {

  private static final String DATABASE_URL = "jdbc:mysql://10.124.32.3:3306/hyperlocal";
  private Connection connection;
  private Utilities util;
  private static final String MERCHANT_UPDATE_STATEMENT = "UPDATE `Merchants` SET `MerchantName` = ?, `MerchantPhone` = ? WHERE `MerchantID`= ?;";
  private static final String MERCHANT_INSERT_STATEMENT = "INSERT into `Merchants` (`MerchantID`, `MerchantName`, `MerchantPhone`) values (?,?,?);";

  public MerchantController() {
    this.util = new Utilities();
    connection = MySQLConnectionBuilder.createConnectionPool(DATABASE_URL);
  }

  @GetMapping("/api/verify/phone")
  public CompletableFuture<String> verifyPhone(@RequestBody String postInputString) {
    String token = JsonParser.parseString(postInputString).getAsJsonObject().get("jwt").getAsString();
    return util.verifyAndDecodePhoneJWT(token)
    .thenApply((json) -> {
      if(json == null) return "Invalid token";
      return json;
    });
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
    JsonObject input = JsonParser.parseString(postInputString).getAsJsonObject();
    String encodedPhoneToken = input.get("phoneJWT").getAsString();
    return util.verifyAndDecodePhoneJWT(encodedPhoneToken)
    .thenCompose((json) -> {
      if(json == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Phone Token provided.");;
      JsonObject phoneJsonObject = JsonParser.parseString(json).getAsJsonObject();
      boolean isPhoneVerified = phoneJsonObject.get("phone_number_verified").getAsBoolean();
      if(!isPhoneVerified) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Phone number should be verified.");
      String merchantPhone = phoneJsonObject.get("phone_number").getAsString();
      String merchantID = input.get("merchantID").getAsString();
      String merchantName = input.get("merchantName").getAsString();
      input.addProperty("merchantPhone", merchantPhone);
      return connection.sendPreparedStatement(MERCHANT_INSERT_STATEMENT, Arrays.asList(merchantID, merchantName, merchantPhone));
    })
    .thenApply((resp) -> {
      return new Merchant(input);
    });
  }

  /* Helper function to call database and update it */
  public CompletableFuture<QueryResult> updateMerchantDetails(JsonObject merchantDetails) {
    String UpdateQueryParameters[] = new String[] { merchantDetails.get("merchantName").getAsString(),
        merchantDetails.get("merchantPhone").getAsString(), merchantDetails.get("merchantID").getAsString() };
    return connection.sendPreparedStatement(MERCHANT_UPDATE_STATEMENT, Arrays.asList(UpdateQueryParameters));
  }

}