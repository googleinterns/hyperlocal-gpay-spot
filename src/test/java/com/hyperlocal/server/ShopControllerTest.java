package com.hyperlocal.server;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.ResultSetKt;
import com.github.jasync.sql.db.RowData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.mockito.runners.MockitoJUnitRunner;

import org.mockito.Spy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;

import io.grpc.netty.shaded.io.netty.util.concurrent.CompleteFuture;

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

    CompletableFuture<QueryResult> queryResult = CompletableFuture.completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(connection.sendPreparedStatement(SHOP_INSERT_STATEMENT,Arrays.asList(InsertQueryParameters))).thenReturn(queryResult);
    CompletableFuture<QueryResult> result = controller.insertNewShop(shopJson);
    assertEquals(queryResult.get(), result.get());
    verify(connection).sendPreparedStatement(SHOP_INSERT_STATEMENT, Arrays.asList(InsertQueryParameters));
  }

  @Test
  public void shouldUpdateShop() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(SHOP_DATA_AS_STRING).isNotNull();
    controller.updateShop(SHOP_DATA_AS_STRING);
    verify(controller).updateShopDetails(JsonParser.parseString(SHOP_DATA_AS_STRING).getAsJsonObject());
  }
}