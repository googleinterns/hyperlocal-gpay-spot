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

  @Mock
  Connection connection;

  @InjectMocks
  MerchantController controller = new MerchantController();

  @Mock
  ResultSet resultSet;

  @Test
  public void shouldInsertMerchant() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(merchantJson).isNotNull();

    String InsertQueryParameters[] = new String[] { "4", "Test Merchant", "7867986767" };
    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(connection.sendPreparedStatement(Constants.MERCHANT_INSERT_STATEMENT, Arrays.asList(InsertQueryParameters)))
        .thenReturn(queryResult);
    CompletableFuture<QueryResult> result = controller.insertNewMerchant(merchantJson);
    assertEquals(queryResult.get(), result.get());
    verify(connection).sendPreparedStatement(Constants.MERCHANT_INSERT_STATEMENT, Arrays.asList(InsertQueryParameters));
  }

  @Test
  public void shouldUpdateMerchant() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(merchantJson).isNotNull();

    String updateQueryParameters[] = new String[] { "Test Merchant", "7867986767", "4" };
    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(connection.sendPreparedStatement(Constants.MERCHANT_UPDATE_STATEMENT, Arrays.asList(updateQueryParameters)))
        .thenReturn(queryResult);
    CompletableFuture<QueryResult> result = controller.updateMerchantDetails(merchantJson);
    assertEquals(queryResult.get(), result.get());
    verify(connection).sendPreparedStatement(Constants.MERCHANT_UPDATE_STATEMENT, Arrays.asList(updateQueryParameters));
  }
}