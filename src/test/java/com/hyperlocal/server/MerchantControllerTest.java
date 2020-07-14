package com.hyperlocal.server;

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

  @InjectMocks
  MerchantController controller = new MerchantController();

  @Mock
  ResultSet resultSet;

  @Test
  public void shouldInsertMerchant() throws Exception {
    // ARRANGE
    assertThat(controller).isNotNull();
    List<Object> queryParams = Arrays.asList(merchantID, merchantName, merchantPhone);
    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));
    when(connection.sendPreparedStatement(Constants.MERCHANT_INSERT_STATEMENT, queryParams))
        .thenReturn(queryResult);
    Merchant expectedOutput = merchant;

    // ACT
    Merchant actualOutput = controller.insertMerchant(new Gson().toJson(merchant)).get();
    
    // ASSERT
    assertEquals(expectedOutput, actualOutput);
    verify(connection).sendPreparedStatement(Constants.MERCHANT_INSERT_STATEMENT, queryParams);
  }

  @Test
  public void shouldUpdateMerchant() throws Exception {
    // ARRANGE
    assertThat(controller).isNotNull();
    List<Object> queryParams = Arrays.asList(merchantName, merchantPhone, merchantID);
    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));
    when(connection.sendPreparedStatement(Constants.MERCHANT_UPDATE_STATEMENT, queryParams))
        .thenReturn(queryResult);
    JsonObject newMerchantDetails = new JsonObject();
    newMerchantDetails.addProperty("merchantName", merchantName);
    newMerchantDetails.addProperty("merchantPhone", merchantPhone);
    Merchant expectedOutput = merchant;
    
    // ACT
    Merchant actualOutput = controller.updateMerchant(merchantID, new Gson().toJson(newMerchantDetails)).get();
    
    // ASSERT
    assertEquals(expectedOutput, actualOutput);
    verify(connection).sendPreparedStatement(Constants.MERCHANT_UPDATE_STATEMENT, queryParams);
  }
}