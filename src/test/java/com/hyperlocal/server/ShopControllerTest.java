package com.hyperlocal.server;

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
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;

@SpringBootTest

public class ShopControllerTest {

  private final Shop shop = new Shop(3L, 4L, "Test Shop", 43.424234, 43.4242444, "S-124", "Test");
  private final String SHOP_DATA_AS_STRING = new Gson().toJson(shop);
  private static final String SHOP_UPDATE_STATEMENT = "UPDATE `Shops` SET `ShopName` = ?, `TypeOfService`=?, `Latitude` = ?, `Longitude` = ?, `AddressLine1` = ? WHERE `ShopID`=?;";
  private static final String SHOP_INSERT_STATEMENT = "INSERT INTO `Shops` (`ShopName`, `TypeOfService`, `Latitude`, `Longitude`, `AddressLine1`, `MerchantID`) VALUES (?,?,?,?,?,?);";;
  private final JsonObject shopJson = JsonParser.parseString(SHOP_DATA_AS_STRING).getAsJsonObject();

  @Mock
  PubSubTemplate template;

  @Mock
  private static Connection connection;

  @InjectMocks
  ShopController controller = new ShopController(template);

  @Mock
  ResultSet resultSet;

  @Test
  public void shouldInsertShop() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(shopJson).isNotNull();

    String InsertQueryParameters[] = new String[] { "Test Shop", "Test", "43.424234", "43.4242444", "S-124", "4" };
    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(connection.sendPreparedStatement(SHOP_INSERT_STATEMENT, Arrays.asList(InsertQueryParameters)))
        .thenReturn(queryResult);
    CompletableFuture<QueryResult> result = controller.insertNewShop(shopJson);
    assertEquals(queryResult.get(), result.get());
    verify(connection).sendPreparedStatement(SHOP_INSERT_STATEMENT, Arrays.asList(InsertQueryParameters));
  }

  @Test
  public void shouldUpdateShop() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(shopJson).isNotNull();

    String updateQueryParameters[] = new String[] { "Test Shop", "Test", "43.424234", "43.4242444", "S-124", "3" };
    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(connection.sendPreparedStatement(SHOP_UPDATE_STATEMENT, Arrays.asList(updateQueryParameters)))
        .thenReturn(queryResult);
    CompletableFuture<QueryResult> result = controller.updateShopDetails(shopJson);
    assertEquals(queryResult.get(), result.get());
    verify(connection).sendPreparedStatement(SHOP_UPDATE_STATEMENT, Arrays.asList(updateQueryParameters));
  }
}