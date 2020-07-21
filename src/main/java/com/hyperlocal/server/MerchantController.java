package com.hyperlocal.server;

import javax.servlet.http.HttpServletRequest;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
public class MerchantController {

  private Connection connection;
  private Utilities util;
  public MerchantController() {
    connection = MySQLConnectionBuilder.createConnectionPool(Constants.DATABASE_URL);
    util = new Utilities();
  }

  /**
   * Updates details of a merchant
   * @param merchantID The unique ID of the merchant
   * @param postInputString JSON serialized {@link Merchant} details
   * @return CompletableFuture of the updated Merchant
   */
  // @PutMapping("/v1/merchants/{merchantID}")
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
   * @param postInputString Serialized JSON with 'phoneJWT' key mapping to Base-64 JWT token issued by Spot Phone Number API
   * @return CompletableFuture of the newly inserted Merchant
   */
  @PostMapping("/v1/merchants")
  public CompletableFuture<Merchant> insertMerchant(HttpServletRequest request, @RequestBody String postInputString) {
    String encodedIdToken = request.getHeader("X-Authorization");
    String encodedPhoneToken = JsonParser.parseString(postInputString).getAsJsonObject().get("phoneJWT").getAsString();
    String idJson = util.verifyAndDecodeIdJwt(encodedIdToken);
    String phoneJson = util.verifyAndDecodePhoneJwt(encodedPhoneToken);
    if(idJson == null || phoneJson == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The provided ID token or/and phone token are invalid.");
    }

    JsonObject idJsonObj = JsonParser.parseString(idJson).getAsJsonObject();
    String merchantID = idJsonObj.get("sub").getAsString();
    String merchantName = idJsonObj.get("given_name").getAsString();

    JsonObject phoneJsonObj = JsonParser.parseString(phoneJson).getAsJsonObject();
    boolean isPhoneVerified = phoneJsonObj.get("phone_number_verified").getAsBoolean();
    if(!isPhoneVerified) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Phone number should be verified.");
    }
    String merchantPhone = phoneJsonObj.get("phone_number").getAsString();

    List<Object> queryParams = Arrays.asList(merchantID, merchantName, merchantPhone);

    return connection
    .sendPreparedStatement(Constants.MERCHANT_INSERT_STATEMENT, queryParams)
    .thenApply((resp) -> {
      return Merchant.create(merchantID, merchantName, merchantPhone);
    });
  }

}