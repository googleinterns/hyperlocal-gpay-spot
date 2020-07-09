package com.hyperlocal.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.RowData;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hyperlocal.server.Data.CatalogItem;
import com.hyperlocal.server.Data.Merchant;
import com.hyperlocal.server.Data.Shop;
import com.hyperlocal.server.Data.ShopDetails;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gcp.pubsub.core.PubSubTemplate;
import org.springframework.scheduling.annotation.AsyncResult;

@SpringBootTest

public class ShopControllerTest {

  private final Shop shop = new Shop(3L, "4", "Test Shop", 43.424234, 43.4242444, "S-124", "Test");
  private final String SHOP_DATA_AS_STRING = new Gson().toJson(shop);
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
    when(connection.sendPreparedStatement(Constants.SELECT_SHOPS_BY_MERCHANT_STATEMENT, Arrays.asList(merchantID)))
        .thenReturn(queryResultPromise);
    List<Shop> expectedList = new ArrayList<Shop>();
    expectedList.add(new Shop(shopRecord));

    /* ACT */
    CompletableFuture<List<Shop>> actualListPromise = controller.getShopsByMerchantID(merchantID);

    /* ASSERT */
    assertEquals(expectedList, actualListPromise.get());
    verify(connection).sendPreparedStatement(Constants.SELECT_SHOPS_BY_MERCHANT_STATEMENT, Arrays.asList(merchantID));
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
      "ImageURL", "#"
    );
    ResultSet serviceRecords = new FakeResultSet(serviceRecord);
    QueryResult servicesQueryResult = new QueryResult(0L, "Success", serviceRecords);

    ShopDetails expectedShopDetails = new ShopDetails(new Shop(shopRecord), new Merchant(merchantRecord),
        new ArrayList<CatalogItem>(Arrays.asList(new CatalogItem(serviceRecord))));

    when(connection.sendPreparedStatement(Constants.SELECT_SHOP_STATEMENT, Arrays.asList(shopID)))
        .thenReturn(CompletableFuture.completedFuture(shopQueryResult));
    when(connection.sendPreparedStatement(Constants.SELECT_MERCHANT_STATEMENT, Arrays.asList(merchantID)))
        .thenReturn(CompletableFuture.completedFuture(merchantQueryResult));
    when(connection.sendPreparedStatement(Constants.SELECT_CATALOG_BY_SHOP_STATEMENT, Arrays.asList(shopID)))
        .thenReturn(CompletableFuture.completedFuture(servicesQueryResult));

    /* ACT */
    CompletableFuture<ShopDetails> actualShopDetailsPromise = controller.getShopDetails(shopID);

    /* ASSERT */
    assertEquals(expectedShopDetails, actualShopDetailsPromise.get());
    verify(connection).sendPreparedStatement(Constants.SELECT_SHOP_STATEMENT, Arrays.asList(shopID));
    verify(connection).sendPreparedStatement(Constants.SELECT_MERCHANT_STATEMENT, Arrays.asList(merchantID));
    verify(connection).sendPreparedStatement(Constants.SELECT_CATALOG_BY_SHOP_STATEMENT, Arrays.asList(shopID));
  }

  @Test
  public void shouldUpsertCatalog() throws Exception {

    /* ARRANGE */
    assertThat(controller).isNotNull();

    // Params: shopID, payload
    Long shopID = 1000000000000L;
    HashMap<String, Object> payload = new HashMap<String, Object>();

    HashMap<String, Object> createCommand = new HashMap<String, Object>();
    List<Object> createList = Arrays.asList(shopID, "Mango", "Lorem ipsum", "#");
    createCommand.put("serviceName", createList.get(1));
    createCommand.put("serviceDescription", createList.get(2));
    createCommand.put("imageURL", createList.get(3));
    payload.put("create", new HashMap[] { createCommand });
    HashMap<String, Object> updateCommand = new HashMap<String, Object>();
    List<Object> updateList = Arrays.asList("Apples", "Lorem ipsum", "#", 9000000000L);
    updateCommand.put("serviceName", updateList.get(0));
    updateCommand.put("serviceDescription", updateList.get(1));
    updateCommand.put("imageURL", updateList.get(2));
    updateCommand.put("serviceID", updateList.get(3));
    payload.put("update", new HashMap[] { updateCommand });
    HashMap<String, Object> deleteCommand = new HashMap<String, Object>();
    Long deleteServiceID = 9000000000L;
    deleteCommand.put("serviceID", deleteServiceID);
    payload.put("delete", new HashMap[] { deleteCommand });

    QueryResult emptyQueryResult = new QueryResult(1L, "Success", resultSet);

    HashMap<String, Object> expectedMap = new HashMap<String, Object>();
    expectedMap.put("success", true);

    when(connection.sendQuery("BEGIN"))
        .thenReturn(CompletableFuture.completedFuture(emptyQueryResult));
    when(connection.sendPreparedStatement(Constants.INSERT_CATALOG_STATEMENT, createList))
        .thenReturn(CompletableFuture.completedFuture(emptyQueryResult));
    when(connection.sendPreparedStatement(Constants.UPDATE_CATALOG_STATEMENT, updateList))
        .thenReturn(CompletableFuture.completedFuture(emptyQueryResult));
    when(connection.sendPreparedStatement(Constants.DELETE_CATALOG_STATEMENT, Arrays.asList(deleteServiceID)))
        .thenReturn(CompletableFuture.completedFuture(emptyQueryResult));
    when(connection.sendQuery("COMMIT")).thenReturn(CompletableFuture.completedFuture(emptyQueryResult));
    when(template.publish(Constants.PUBSUB_URL, "1000000000000"))
        .thenReturn(new AsyncResult<>("DONE"));

    /* ACT */
    CompletableFuture<HashMap<String, Object>> actualMapPromise = controller.upsertCatalog(shopID,
        new Gson().toJson(payload));

    /* ASSERT */
    assertEquals(expectedMap, actualMapPromise.get());
    verify(connection).sendQuery("BEGIN");
    verify(connection).sendPreparedStatement(Constants.INSERT_CATALOG_STATEMENT, createList);
    verify(connection).sendPreparedStatement(Constants.UPDATE_CATALOG_STATEMENT, updateList);
    verify(connection).sendPreparedStatement(Constants.DELETE_CATALOG_STATEMENT, Arrays.asList(deleteServiceID));
    verify(connection).sendQuery("COMMIT");
    verify(template).publish(Constants.PUBSUB_URL, "1000000000000");
  }

  @Test
  public void shouldReturnArrayOfShops() throws Exception {

    /* ARRANGE */
    assertThat(controller).isNotNull();

    ArrayList<Long> shopIdList = new ArrayList<Long>();
    shopIdList.add(1L);
    shopIdList.add(2L);
    shopIdList.add(3L);

    ArrayList<String> merchantIDList = new ArrayList<String>();
    merchantIDList.add("1");
    merchantIDList.add("2");
    merchantIDList.add("3");

    ArrayList<RowData> shopRecords = new ArrayList<RowData>();
    for (Long shopID : shopIdList) {
      RowData shopRecord = new FakeRowData(
        "ShopID", shopID, 
        "MerchantID", Long.toString(shopID), 
        "ShopName", "Arvind Shop", 
        "Latitude", new BigDecimal(23.33), 
        "Longitude", new BigDecimal(23.33), 
        "AddressLine1", "Mumbai", 
        "TypeOfService", "Groceries"
      );
      shopRecords.add(shopRecord);
    }
    ResultSet wrappedShopRecord = new FakeResultSet(shopRecords);
    QueryResult shopQueryResult = new QueryResult(0L, "Success", wrappedShopRecord);

    ArrayList<RowData> merchantRecords = new ArrayList<RowData>();
    for (String merchantID : merchantIDList) {
      RowData merchantRecord = new FakeRowData(
        "MerchantID", merchantID, 
        "MerchantName", "Arvind", 
        "MerchantPhone", "9876543210");
      merchantRecords.add(merchantRecord);
    }
    ResultSet wrappedMerchantRecord = new FakeResultSet(merchantRecords);
    QueryResult merchantQueryResult = new QueryResult(0L, "Success", wrappedMerchantRecord);

    ArrayList<RowData> CatalogItems = new ArrayList<RowData>();
    for (Long shopID : shopIdList) {
      RowData serviceRecord = new FakeRowData(
        "ServiceID", 101L, 
        "ShopID", shopID, 
        "ServiceName", "Apples",
        "ServiceDescription", "Fresh off the market!", 
        "ImageURL", "#")
      ;
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

    String shopPreparedStatementPlaceholder = Utilities.getPlaceHolderString(3);
    String merchantPreparedStatementPlaceholder = Utilities.getPlaceHolderString(3);

    when(connection.sendPreparedStatement(
      String.format(Constants.SELECT_SHOPS_BATCH_QUERY, shopPreparedStatementPlaceholder), shopIdList))
        .thenReturn(CompletableFuture.completedFuture(shopQueryResult));
    when(connection.sendPreparedStatement(
      String.format(Constants.SELECT_MERCHANT_BATCH_QUERY, merchantPreparedStatementPlaceholder), merchantIDList))
        .thenReturn(CompletableFuture.completedFuture(merchantQueryResult));
    when(connection.sendPreparedStatement(
      String.format(Constants.SELECT_CATALOG_BATCH_QUERY, shopPreparedStatementPlaceholder), shopIdList))
        .thenReturn(CompletableFuture.completedFuture(servicesQueryResult));

    /* ACT */

    CompletableFuture<List<ShopDetails>> actualShopDetailsPromise = controller.getShopsByShopIDBatch(shopIdList);

    /* ASSERT */

    assertEquals(expectedShopDetails, actualShopDetailsPromise.get());
    verify(connection).sendPreparedStatement(
      String.format(Constants.SELECT_CATALOG_BATCH_QUERY, shopPreparedStatementPlaceholder), shopIdList);
    verify(connection).sendPreparedStatement(
      String.format(Constants.SELECT_MERCHANT_BATCH_QUERY, merchantPreparedStatementPlaceholder), merchantIDList);
    verify(connection).sendPreparedStatement(
      String.format(Constants.SELECT_SHOPS_BATCH_QUERY, shopPreparedStatementPlaceholder), shopIdList);
  }

  @Test
  public void shouldInsertShop() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(shopJson).isNotNull();

    String InsertQueryParameters[] = new String[] { "Test Shop", "Test", "43.424234", "43.4242444", "S-124", "4" };
    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(connection.sendPreparedStatement(Constants.SHOP_INSERT_STATEMENT, Arrays.asList(InsertQueryParameters)))
        .thenReturn(queryResult);
    CompletableFuture<QueryResult> result = controller.insertNewShop(shopJson);
    assertEquals(queryResult.get(), result.get());
    verify(connection).sendPreparedStatement(Constants.SHOP_INSERT_STATEMENT, Arrays.asList(InsertQueryParameters));
  }

  @Test
  public void shouldUpdateShop() throws Exception {
    assertThat(controller).isNotNull();
    assertThat(shopJson).isNotNull();

    String updateQueryParameters[] = new String[] { "Test Shop", "Test", "43.424234", "43.4242444", "S-124", "3" };
    CompletableFuture<QueryResult> queryResult = CompletableFuture
        .completedFuture(new QueryResult(1, "SUCCESS", resultSet));

    when(connection.sendPreparedStatement(Constants.SHOP_UPDATE_STATEMENT, Arrays.asList(updateQueryParameters)))
        .thenReturn(queryResult);
    CompletableFuture<QueryResult> result = controller.updateShopDetails(shopJson);
    assertEquals(queryResult.get(), result.get());
    verify(connection).sendPreparedStatement(Constants.SHOP_UPDATE_STATEMENT, Arrays.asList(updateQueryParameters));
  }
}
