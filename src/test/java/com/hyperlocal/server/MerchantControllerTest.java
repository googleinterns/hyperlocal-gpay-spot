package com.hyperlocal.server;

import com.hyperlocal.server.Data.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MerchantControllerTest {

  Merchant merchant = new Merchant("4", "Test Merchant", "7867986767");
  private final String MERCHANT_DATA_AS_STRING = new Gson().toJson(merchant);
  private JsonObject merchantJson = JsonParser.parseString(MERCHANT_DATA_AS_STRING).getAsJsonObject();
  private static final String MERCHANT_UPDATE_STATEMENT = "UPDATE `Merchants` SET `MerchantName` = ?, `MerchantPhone` = ? WHERE `MerchantID`= ?;";
  private static final String MERCHANT_INSERT_STATEMENT = "INSERT into `Merchants` (`MerchantID`, `MerchantName`, `MerchantPhone`) values (?,?,?);";

  @Mock
  Connection connection;

  @Mock
  Utilities util;

  @InjectMocks
  MerchantController controller = new MerchantController();

  @Mock
  ResultSet resultSet;

  @Test
  public void shouldInsertMerchant() throws Exception {
    // ARRANGE
    assertThat(controller).isNotNull();
    String merchantID = "10254198765423652158965412365";
    String merchantName = "John Smith";
    String merchantPhone = "+91 9999999999";
    String phoneJWT = "FAKE_PHONE_JWT";
    
    JsonObject inputJsonObject = new JsonObject();
    inputJsonObject.addProperty("merchantID", merchantID);
    inputJsonObject.addProperty("merchantName", merchantName);
    inputJsonObject.addProperty("phoneJWT", phoneJWT);

    Merchant expectedOutput = new Merchant(merchantID, merchantName, merchantPhone);

    JsonObject decodedPhoneToken = new JsonObject();
    decodedPhoneToken.addProperty("phone_number", merchantPhone);
    decodedPhoneToken.addProperty("phone_number_verified", true);

    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(util.verifyAndDecodePhoneJWT(phoneJWT))
        .thenReturn(CompletableFuture.completedFuture(decodedPhoneToken.toString()));
    when(connection.sendPreparedStatement(MERCHANT_INSERT_STATEMENT, Arrays.asList(merchantID, merchantName, merchantPhone)))
        .thenReturn(queryResult);
    
    // ACT
    Merchant actualOutput = controller.insertMerchant(inputJsonObject.toString()).get();
    
    // ASSERT
    assertEquals(expectedOutput, actualOutput);
    verify(util).verifyAndDecodePhoneJWT(phoneJWT);
    verify(connection).sendPreparedStatement(MERCHANT_INSERT_STATEMENT, Arrays.asList(merchantID, merchantName, merchantPhone));
  }

  @Test
  public void shouldUpdateMerchant() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(merchantJson).isNotNull();

    String updateQueryParameters[] = new String[] { "Test Merchant", "7867986767", "4" };
    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(connection.sendPreparedStatement(MERCHANT_UPDATE_STATEMENT, Arrays.asList(updateQueryParameters)))
        .thenReturn(queryResult);
    CompletableFuture<QueryResult> result = controller.updateMerchantDetails(merchantJson);
    assertEquals(queryResult.get(), result.get());
    verify(connection).sendPreparedStatement(MERCHANT_UPDATE_STATEMENT, Arrays.asList(updateQueryParameters));
  }
}