package com.hyperlocal.server;

import com.hyperlocal.server.Data.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.util.concurrent.ListenableFuture;

@SpringBootTest

public class ShopControllerTest {

  private final Shop shop = new Shop(3L, "4", "Test Shop", 43.424234, 43.4242444, "S-124", "Test");
  private final String SHOP_DATA_AS_STRING = new Gson().toJson(shop);
  private static final String SHOP_UPDATE_STATEMENT = "UPDATE `Shops` SET `ShopName` = ?, `TypeOfService`=?, `Latitude` = ?, `Longitude` = ?, `AddressLine1` = ? WHERE `ShopID`=?;";
  private static final String SHOP_INSERT_STATEMENT = "INSERT INTO `Shops` (`ShopName`, `TypeOfService`, `Latitude`, `Longitude`, `AddressLine1`, `MerchantID`) VALUES (?,?,?,?,?,?);";;
  private static final String SELECT_SHOP_STATEMENT = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `ShopID` = ?;";
  private static final String SELECT_SHOPS_BY_MERCHANT_STATEMENT = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `MerchantID` = ?;";
  private static final String SELECT_SHOPS_BATCH_QUERY = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `ShopID` IN (?,?,?);";
  private static final String SELECT_MERCHANT_BATCH_QUERY = "SELECT `MerchantID`, `MerchantName`, `MerchantPhone` from `Merchants` WHERE `MerchantID` IN (?,?,?);";
  private static final String SELECT_CATALOG_BATCH_QUERY = "SELECT `ServiceID`, `ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL` from `Catalog` WHERE `ShopID` IN (?,?,?);";
  private static final String SELECT_MERCHANT_STATEMENT = "SELECT `MerchantID`, `MerchantName`, `MerchantPhone` from `Merchants` WHERE `MerchantID` = ?;";
  private static final String SELECT_CATALOG_BY_SHOP_STATEMENT = "SELECT `ServiceID`, `ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL` from `Catalog` WHERE `ShopID` = ?;";
  private static final String INSERT_CATALOG_STATEMENT = "INSERT INTO `Catalog` (`ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL`) VALUES (?, ?, ?, ?);";
  private static final String UPDATE_CATALOG_STATEMENT = "UPDATE `Catalog` SET `ServiceName` = ?, `ServiceDescription` = ?, `ImageURL` = ? WHERE `ServiceID` = ?;";
  private static final String DELETE_CATALOG_STATEMENT = "DELETE FROM `Catalog` WHERE `ServiceID` = ?;";
  private final JsonObject shopJson = JsonParser.parseString(SHOP_DATA_AS_STRING).getAsJsonObject();

  @Mock
  private static PubSubTemplate template;

  @Mock
  private static Connection connection;

  @InjectMocks
  ShopController controller = new ShopController(template);

  @Mock
  ResultSet resultSet;

  @Test
  public void shouldGetShopsByMerchantID() throws Exception {

    /* ARRANGE */
    assertThat(controller).isNotNull();
    String merchantID = "1000000000000";

    RowData shopRecord = new FakeRowData(
      "ShopID", 1L, 
      "MerchantID", "1000000000000", 
      "ShopName", "Arvind Shop", 
      "Latitude", new BigDecimal(23.33), 
      "Longitude", new BigDecimal(23.33), 
      "AddressLine1", "Mumbai", 
      "TypeOfService", "Groceries"
      );
    ResultSet shopRecords = new FakeResultSet(shopRecord);
    QueryResult shopsQueryResult = new QueryResult(0L, "Success", shopRecords);
    CompletableFuture<QueryResult> queryResultPromise = CompletableFuture.completedFuture(shopsQueryResult);
    when(connection.sendPreparedStatement(SELECT_SHOPS_BY_MERCHANT_STATEMENT, Arrays.asList(merchantID)))
    .thenReturn(queryResultPromise);
    List<Shop> expectedList = new ArrayList<Shop>();
    expectedList.add(new Shop(shopRecord));

    /* ACT */
    CompletableFuture<List<Shop>> actualListPromise = controller.getShopsByMerchantID(merchantID);

    /* ASSERT */
    assertEquals(expectedList, actualListPromise.get());
    verify(connection).sendPreparedStatement(SELECT_SHOPS_BY_MERCHANT_STATEMENT, Arrays.asList(merchantID));
  }
  
  @Test
  public void shouldGetShopDetails() throws Exception {

    /* ARRANGE */
    assertThat(controller).isNotNull();

    Long shopID = 1000000000000L;

    String merchantID = "2000000000000";
    RowData shopRecord = new FakeRowData(
      "ShopID", shopID, 
      "MerchantID", merchantID, 
      "ShopName", "Arvind Shop", 
      "Latitude", new BigDecimal(23.33), 
      "Longitude", new BigDecimal(23.33), 
      "AddressLine1", "Mumbai", 
      "TypeOfService", "Groceries"
      );
    ResultSet wrappedShopRecord = new FakeResultSet(shopRecord);
    QueryResult shopQueryResult = new QueryResult(0L, "Success", wrappedShopRecord);
    RowData merchantRecord = new FakeRowData(
      "MerchantID", merchantID, 
      "MerchantName", "Arvind", 
      "MerchantPhone", "9876543210"
      );
    ResultSet wrappedMerchantRecord = new FakeResultSet(merchantRecord);
    QueryResult merchantQueryResult = new QueryResult(0L, "Success", wrappedMerchantRecord);

    RowData serviceRecord = new FakeRowData(
      "ServiceID", 101L, 
      "ShopID", shopID, 
      "ServiceName", "Apples", 
      "ServiceDescription", "Fresh off the market!", 
      "ImageURL", "#");
    ResultSet serviceRecords = new FakeResultSet(serviceRecord);
    QueryResult servicesQueryResult = new QueryResult(0L, "Success", serviceRecords);

    ShopDetails expectedShopDetails = new ShopDetails(new Shop(shopRecord), new Merchant(merchantRecord), new ArrayList<CatalogItem>(Arrays.asList(new CatalogItem(serviceRecord))));

    when(connection.sendPreparedStatement(SELECT_SHOP_STATEMENT, Arrays.asList(shopID)))
    .thenReturn(CompletableFuture.completedFuture(shopQueryResult));
    when(connection.sendPreparedStatement(SELECT_MERCHANT_STATEMENT, Arrays.asList(merchantID)))
    .thenReturn(CompletableFuture.completedFuture(merchantQueryResult));
    when(connection.sendPreparedStatement(SELECT_CATALOG_BY_SHOP_STATEMENT, Arrays.asList(shopID)))
    .thenReturn(CompletableFuture.completedFuture(servicesQueryResult));

    /* ACT */
    CompletableFuture<ShopDetails> actualShopDetailsPromise = controller.getShopDetails(shopID);
    
    /* ASSERT */
    assertEquals(expectedShopDetails, actualShopDetailsPromise.get());
    verify(connection).sendPreparedStatement(SELECT_SHOP_STATEMENT, Arrays.asList(shopID));
    verify(connection).sendPreparedStatement(SELECT_MERCHANT_STATEMENT, Arrays.asList(merchantID));
    verify(connection).sendPreparedStatement(SELECT_CATALOG_BY_SHOP_STATEMENT, Arrays.asList(shopID));
  }



  @Test
  public void shouldUpsertCatalog() throws Exception {
    
    /* ARRANGE */
    assertThat(controller).isNotNull();

    // Params: shopID, payload
    Long shopID = 1000000000000L;
    HashMap<String, Object> payload = new HashMap<String, Object>();
    
    HashMap<String, Object> addCommand = new HashMap<String, Object>();
    List<Object> addList = Arrays.asList(shopID, "Mango", "Lorem ipsum", "#");
    addCommand.put("serviceName", addList.get(1));
    addCommand.put("serviceDescription", addList.get(2));
    addCommand.put("imageURL", addList.get(3));
    payload.put("add", new HashMap[]{addCommand});
    HashMap<String, Object> editCommand = new HashMap<String, Object>();
    List<Object> editList = Arrays.asList("Apples", "Lorem ipsum", "#", 9000000000L);
    editCommand.put("serviceName", editList.get(0));
    editCommand.put("serviceDescription", editList.get(1));
    editCommand.put("imageURL", editList.get(2));
    editCommand.put("serviceID", editList.get(3));
    payload.put("edit", new HashMap[]{editCommand});
    HashMap<String, Object> deleteCommand = new HashMap<String, Object>();
    Long deleteServiceID = 9000000000L;
    deleteCommand.put("serviceID", deleteServiceID);
    payload.put("delete", new HashMap[]{deleteCommand});

    QueryResult emptyQueryResult = new QueryResult(1L, "Success", resultSet);

    HashMap<String, Object> expectedMap = new HashMap<String, Object>();
    expectedMap.put("success", true);
        assertThat(controller).isNotNull();

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
    when(template.publish("projects/speedy-anthem-217710/topics/testTopic", "1000000000000"))
    .thenReturn(new AsyncResult<>("DONE"));

    /* ACT */
    CompletableFuture<HashMap<String, Object>> actualMapPromise = controller.upsertCatalog(shopID, new Gson().toJson(payload));

   verify(template).publish("projects/speedy-anthem-217710/topics/testTopic", "1000000000000");

    /* ASSERT */
    assertEquals(expectedMap, actualMapPromise.get());
    verify(connection).sendQuery("BEGIN");
    verify(connection).sendPreparedStatement(INSERT_CATALOG_STATEMENT, addList);
    verify(connection).sendPreparedStatement(UPDATE_CATALOG_STATEMENT, editList);
    verify(connection).sendPreparedStatement(DELETE_CATALOG_STATEMENT, Arrays.asList(deleteServiceID));
    verify(connection).sendQuery("COMMIT");
  }
  
  @Test
  public void shouldReturnArrayOfShops() throws Exception {

    /* ARRANGE */
    assertThat(controller).isNotNull();

    HashMap<String, ArrayList<Long>> payLoad = new HashMap<String, ArrayList<Long>>();
    ArrayList<Long> shopIdList = new ArrayList<Long>();
    shopIdList.add(1L);
    shopIdList.add(2L);
    shopIdList.add(3L);

    payLoad.put("id", shopIdList);

    ArrayList<String> merchantIDList = new ArrayList<String>();
    merchantIDList.add("1");
    merchantIDList.add("2");
    merchantIDList.add("3");

    ArrayList<RowData> shopRecords = new ArrayList<RowData>();
    for (Long shopID : shopIdList) {
      RowData shopRecord = new FakeRowData("ShopID", shopID, "MerchantID", Long.toString(shopID), "ShopName",
          "Arvind Shop", "Latitude", new BigDecimal(23.33), "Longitude", new BigDecimal(23.33), "AddressLine1",
          "Mumbai", "TypeOfService", "Groceries");
      shopRecords.add(shopRecord);
    }
    ResultSet wrappedShopRecord = new FakeResultSet(shopRecords);
    QueryResult shopQueryResult = new QueryResult(0L, "Success", wrappedShopRecord);

    ArrayList<RowData> merchantRecords = new ArrayList<RowData>();
    for (String merchantID : merchantIDList) {
      RowData merchantRecord = new FakeRowData("MerchantID", merchantID, "MerchantName", "Arvind", "MerchantPhone",
          "9876543210");
      merchantRecords.add(merchantRecord);
    }
    ResultSet wrappedMerchantRecord = new FakeResultSet(merchantRecords);
    QueryResult merchantQueryResult = new QueryResult(0L, "Success", wrappedMerchantRecord);

    ArrayList<RowData> CatalogItems = new ArrayList<RowData>();
    for (Long shopID : shopIdList) {
      RowData serviceRecord = new FakeRowData("ServiceID", 101L, "ShopID", shopID, "ServiceName", "Apples",
          "ServiceDescription", "Fresh off the market!", "ImageURL", "#");
      CatalogItems.add(serviceRecord);
    }
    ResultSet serviceRecords = new FakeResultSet(CatalogItems);
    QueryResult servicesQueryResult = new QueryResult(0L, "Success", serviceRecords);

    List<ShopDetails> expectedShopDetails = new ArrayList<ShopDetails>();

    for (Integer i = 0; i < 3; i++) {
      Shop shop = new Shop(shopRecords.get(i));
      Merchant merchant = new Merchant(merchantRecords.get(i));
      List<CatalogItem> catalogItems = Arrays.asList(new CatalogItem(CatalogItems.get(i)));
      ShopDetails shopDetails = new ShopDetails(shop, merchant, catalogItems);
      expectedShopDetails.add(shopDetails);
    }

    when(connection.sendPreparedStatement(SELECT_SHOPS_BATCH_QUERY, shopIdList))
        .thenReturn(CompletableFuture.completedFuture(shopQueryResult));
    when(connection.sendPreparedStatement(SELECT_MERCHANT_BATCH_QUERY, merchantIDList))
        .thenReturn(CompletableFuture.completedFuture(merchantQueryResult));
    when(connection.sendPreparedStatement(SELECT_CATALOG_BATCH_QUERY, shopIdList))
        .thenReturn(CompletableFuture.completedFuture(servicesQueryResult));

    /* ACT */

    CompletableFuture<List<ShopDetails>> actualShopDetailsPromise = controller
        .getShopsByShopIDBatch(new Gson().toJson(payLoad));

    /* ASSERT */

    assertEquals(expectedShopDetails, actualShopDetailsPromise.get());
    verify(connection).sendPreparedStatement(SELECT_CATALOG_BATCH_QUERY, shopIdList);
    verify(connection).sendPreparedStatement(SELECT_MERCHANT_BATCH_QUERY, merchantIDList);
    verify(connection).sendPreparedStatement(SELECT_SHOPS_BATCH_QUERY, shopIdList);
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