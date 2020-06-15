package com.hyperlocal.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import java.math.BigDecimal;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
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
  private static final String SELECT_SHOP_STATEMENT = "SELECT * from `Shops` WHERE `ShopID` = ?;";
  private static final String SELECT_MERCHANT_STATEMENT = "SELECT * from `Merchants` WHERE `MerchantID` = ?;";
  private static final String SELECT_CATALOG_BY_SHOP_STATEMENT = "SELECT * from `Catalog` WHERE `ShopID` = ?;";
  private static final String INSERT_CATALOG_STATEMENT = "INSERT INTO `Catalog` (`ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL`) VALUES (?, ?, ?, ?);";
  private static final String UPDATE_CATALOG_STATEMENT = "UPDATE `Catalog` SET `ServiceName` = ?, `ServiceDescription` = ?, `ImageURL` = ? WHERE `ServiceID` = ?;";
  private static final String DELETE_CATALOG_STATEMENT = "DELETE FROM `Catalog` WHERE `ServiceID` = ?;";
  private static final String SELECT_SHOPS_BY_MERCHANT_STATEMENT = "SELECT * from `Shops` WHERE `MerchantID` = ?;";
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
  public void shouldGetShopsByMerchantID() throws Exception {
    assertThat(controller).isNotNull();
    
    // Param
    Long merchantID = 1000000000000L;

    // Def: Mocked database output
    RowData shopRecord = new Record(1L, 200L, "Arvind Shop", new BigDecimal(23.33), new BigDecimal(23.33), "Mumbai", "Groceries");
    ResultSet shopRecords = new Records(shopRecord);
    QueryResult shopsQueryResult = new QueryResult(0L, "Success", shopRecords);

    CompletableFuture<QueryResult> queryResultPromise = CompletableFuture.completedFuture(shopsQueryResult);
    when(connection.sendPreparedStatement(SELECT_SHOPS_BY_MERCHANT_STATEMENT, Arrays.asList(merchantID)))
    .thenReturn(queryResultPromise);

    // Def: Expected output:
    HashMap<String, Object> expectedMap = new HashMap<String, Object>();
    expectedMap.put("count", 1);
    expectedMap.put("shops", new ArrayList<Shop>(Arrays.asList(new Shop(shopRecord))));

    
    // Verify: Expected vs actual output
    CompletableFuture<HashMap<String, Object>> actualMapPromise = controller.getShopsByMerchantID(merchantID);
    assertEquals(expectedMap, actualMapPromise.get());
    verify(connection).sendPreparedStatement(SELECT_SHOPS_BY_MERCHANT_STATEMENT, Arrays.asList(merchantID));
  }
  
  @Test
  public void shouldGetShopDetails() throws Exception {
    assertThat(controller).isNotNull();

    // Param
    Long shopID = 1000000000000L;

    /* BEGIN: Def database output */
    Long merchantID = 2000000000000L;

    RowData shopRecord = new Record(shopID, merchantID, "Arvind Shop", new BigDecimal(23.33), new BigDecimal(23.33), "Mumbai", "Groceries");
    ResultSet wrappedShopRecord = new Records(shopRecord);
    QueryResult shopQueryResult = new QueryResult(0L, "Success", wrappedShopRecord);

    RowData merchantRecord = new Record(merchantID, "Arvind", "9876543210");
    ResultSet wrappedMerchantRecord = new Records(merchantRecord);
    QueryResult merchantQueryResult = new QueryResult(0L, "Success", wrappedMerchantRecord);

    RowData serviceRecord = new Record(101L, shopID, "Apples", "Fresh off the market!", "#");
    ResultSet serviceRecords = new Records(serviceRecord);
    QueryResult servicesQueryResult = new QueryResult(0L, "Success", serviceRecords);
    /* END: Def database output */

    // Expected output
    HashMap<String, Object> expectedMap = new HashMap<String, Object>();
    expectedMap.put("shopDetails", new Shop(shopRecord));
    expectedMap.put("merchantDetails", new Merchant(merchantRecord));
    expectedMap.put("catalog", new ArrayList<Service>(Arrays.asList(new Service(serviceRecord))));
    

    // Mocking database calls
    when(connection.sendPreparedStatement(SELECT_SHOP_STATEMENT, Arrays.asList(shopID)))
    .thenReturn(CompletableFuture.completedFuture(shopQueryResult));

    when(connection.sendPreparedStatement(SELECT_MERCHANT_STATEMENT, Arrays.asList(merchantID)))
    .thenReturn(CompletableFuture.completedFuture(merchantQueryResult));
    
    when(connection.sendPreparedStatement(SELECT_CATALOG_BY_SHOP_STATEMENT, Arrays.asList(shopID)))
    .thenReturn(CompletableFuture.completedFuture(servicesQueryResult));
    

    // Verify: Expected vs actual output
    CompletableFuture<HashMap<String, Object>> actualMapPromise = controller.getShopDetails(shopID);
    assertEquals(expectedMap, actualMapPromise.get());
    verify(connection).sendPreparedStatement(SELECT_SHOP_STATEMENT, Arrays.asList(shopID));
    verify(connection).sendPreparedStatement(SELECT_MERCHANT_STATEMENT, Arrays.asList(merchantID));
    verify(connection).sendPreparedStatement(SELECT_CATALOG_BY_SHOP_STATEMENT, Arrays.asList(shopID));
  }



  @Test
  public void shouldUpsertCatalog() throws Exception {
    assertThat(controller).isNotNull();

    // Params: shopID, payload
    Long shopID = 1000000000000L;
    HashMap<String, Object> payload = new HashMap<String, Object>();
    
    // BEGIN: Populating payload
    // Add
    HashMap<String, Object> addCommand = new HashMap<String, Object>();
    List<Object> addList = Arrays.asList(shopID, "Mango", "Lorem ipsum", "#");
    addCommand.put("serviceName", addList.get(1));
    addCommand.put("serviceDescription", addList.get(2));
    addCommand.put("imageURL", addList.get(3));
    payload.put("add", new HashMap[]{addCommand});
    // Edit
    HashMap<String, Object> editCommand = new HashMap<String, Object>();
    List<Object> editList = Arrays.asList("Apples", "Lorem ipsum", "#", 9000000000L);
    editCommand.put("serviceName", editList.get(0));
    editCommand.put("serviceDescription", editList.get(1));
    editCommand.put("imageURL", editList.get(2));
    editCommand.put("serviceID", editList.get(3));
    payload.put("edit", new HashMap[]{editCommand});
    // Delete
    HashMap<String, Object> deleteCommand = new HashMap<String, Object>();
    Long deleteServiceID = 9000000000L;
    deleteCommand.put("serviceID", deleteServiceID);
    payload.put("delete", new HashMap[]{deleteCommand});
    // END: Populating payload

    // Def database output
    QueryResult emptyQueryResult = new QueryResult(1L, "Success", resultSet);

    // Expected output
    HashMap<String, Object> expectedMap = new HashMap<String, Object>();
    expectedMap.put("success", true);
    
    // Mocking database calls
    when(connection.sendQuery("BEGIN"))
    .thenReturn(CompletableFuture.completedFuture(emptyQueryResult));

    when(connection.sendPreparedStatement(INSERT_CATALOG_STATEMENT, addList))
    .thenReturn(CompletableFuture.completedFuture(emptyQueryResult));

    when(connection.sendPreparedStatement(UPDATE_CATALOG_STATEMENT, editList))
    .thenReturn(CompletableFuture.completedFuture(emptyQueryResult));
    
    when(connection.sendPreparedStatement(DELETE_CATALOG_STATEMENT, Arrays.asList(deleteServiceID)))
    .thenReturn(CompletableFuture.completedFuture(emptyQueryResult));

    when(connection.sendQuery("COMMIT"))
    .thenReturn(CompletableFuture.completedFuture(emptyQueryResult));

    // Verify: Expected vs actual output
    CompletableFuture<HashMap<String, Object>> actualMapPromise = controller.upsertCatalog(shopID, new Gson().toJson(payload));
    assertEquals(expectedMap, actualMapPromise.get());
    verify(connection).sendPreparedStatement(INSERT_CATALOG_STATEMENT, addList);
    verify(connection).sendPreparedStatement(UPDATE_CATALOG_STATEMENT, editList);
    verify(connection).sendPreparedStatement(DELETE_CATALOG_STATEMENT, Arrays.asList(deleteServiceID));
  }

  
  

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