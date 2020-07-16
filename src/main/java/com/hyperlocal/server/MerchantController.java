package com.hyperlocal.server;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hyperlocal.server.Data.Merchant;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MerchantController {

  private Connection connection;

  public MerchantController() {
    connection = MySQLConnectionBuilder.createConnectionPool(Constants.DATABASE_URL);
  }

  /**
   * Updates details of a merchant
   * @param merchantID The unique ID of the merchant
   * @param postInputString JSON serialized {@link Merchant} details
   * @return CompletableFuture of the updated Merchant
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
      return Merchant.create(merchantID, merchantName, merchantPhone);
    });
  }

  /**
   * Insert a new merchant
   * @param postInputString JSON serialized {@link Merchant} details
   * @return CompletableFuture of the newly inserted Merchant
   */
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
      return Merchant.create(merchantJson);
    });
  }

}