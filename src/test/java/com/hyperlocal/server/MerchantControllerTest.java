package com.hyperlocal.server;

import javax.servlet.http.HttpServletRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hyperlocal.server.Data.Merchant;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MerchantControllerTest {

  String merchantID = "4";
  String merchantName = "Test Merchant";
  String merchantPhone = "+91 7867986767";
  private final Merchant merchant = Merchant.create(merchantID, merchantName, merchantPhone);

  @Mock
  Connection connection;

  @Mock
  Utilities util;

  @Mock
  HttpServletRequest request;

  @InjectMocks
  MerchantController controller = new MerchantController();

  @Mock
  ResultSet resultSet;

  @Test
  public void shouldInsertMerchant() throws Exception {
    // ARRANGE
    assertThat(controller).isNotNull();
    String merchantID = "10254198765423652158965412365";
    String merchantName = "John";
    String merchantPhone = "+91 9999999999";
    String phoneJWT = "FAKE_PHONE_JWT";
    String idJWT = "FAKE_ID_JWT";

    JsonObject requestBody = new JsonObject();
    requestBody.addProperty("phoneJWT", phoneJWT);

    JsonObject decodedId = new JsonObject();
    decodedId.addProperty("sub", merchantID);
    decodedId.addProperty("given_name", merchantName);

    JsonObject decodedPhoneToken = new JsonObject();
    decodedPhoneToken.addProperty("phone_number", merchantPhone);
    decodedPhoneToken.addProperty("phone_number_verified", true);

    List<Object> queryParams = Arrays.asList(merchantID, merchantName, merchantPhone);
    CompletableFuture<QueryResult> queryResult = CompletableFuture.completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(util.verifyAndDecodePhoneJwt(phoneJWT)).thenReturn(decodedPhoneToken.toString());
    when(util.verifyAndDecodeIdJwt(idJWT)).thenReturn(decodedId.toString());
    when(request.getHeader("X-Authorization")).thenReturn(idJWT);
    when(connection.sendPreparedStatement(Constants.MERCHANT_INSERT_STATEMENT, queryParams)).thenReturn(queryResult);
    Merchant expectedOutput = Merchant.create(merchantID, merchantName, merchantPhone);

    // ACT
    Merchant actualOutput = controller.insertMerchant(request, requestBody.toString()).get();
    
    // ASSERT
    assertEquals(expectedOutput, actualOutput);
    verify(util).verifyAndDecodePhoneJwt(phoneJWT);
    verify(util).verifyAndDecodeIdJwt(idJWT);
    verify(request).getHeader("X-Authorization");
    verify(connection).sendPreparedStatement(Constants.MERCHANT_INSERT_STATEMENT, queryParams);
  }

}