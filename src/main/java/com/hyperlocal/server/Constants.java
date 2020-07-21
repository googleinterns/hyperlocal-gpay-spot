package com.hyperlocal.server;

/**
 * @author: Diksha, Onish
 * @version: 1.0
 * @since: 1.0
 */
public final class Constants {

  /* The IP Addresses here is the Internal IP addresses of CloudSQL instance 
   * Obtaining an internal IP for CloudSQL: https://cloud.google.com/sql/docs/mysql/configure-private-ip 
   */
  static final String DATABASE_URL = "jdbc:mysql://10.124.33.5:3306/hyperlocal";

  /* The IP Address here is the internal IP Address of the Compute Engine running Elasticsearch server
   * Obtaining a static Internal IP for Compute Engine VM: https://cloud.google.com/compute/docs/ip-addresses/reserve-static-internal-ip-address
   */
  static final String SEARCH_INDEX_URL = "http://10.128.0.13:9200/shops/_search?filter_path=hits.hits";

  public static final String SHOP_UPDATE_STATEMENT = "UPDATE `Shops` SET `ShopName` = ?, `TypeOfService`=?, `Latitude` = ?, `Longitude` = ?, `AddressLine1` = ? WHERE `ShopID`= ? AND `MerchantID` = ?;";
  public static final String SHOP_INSERT_STATEMENT = "INSERT INTO `Shops` (`ShopName`, `TypeOfService`, `Latitude`, `Longitude`, `AddressLine1`, `MerchantID`) VALUES (?,?,?,?,?,?);";;
  public static final String SELECT_SHOP_STATEMENT = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `ShopID` = ?;";
  public static final String SELECT_SHOP_DETAILS_STATEMENT = "SELECT `Shops`.`ShopID`, `Shops`.`MerchantID`, `Shops`.`ShopName`, `Shops`.`Latitude`, `Shops`.`Longitude`, `Shops`.`AddressLine1`, `Shops`.`TypeOfService`, `Merchants`.`MerchantName`, `Merchants`.`MerchantPhone`, `Catalog`.`ServiceID`, `Catalog`.`ServiceName`, `Catalog`.`ServiceDescription`, `Catalog`.`ImageURL` FROM `Shops` INNER JOIN `Merchants` ON `Shops`.`MerchantID` = `Merchants`.`MerchantID` LEFT JOIN `Catalog` ON `Shops`.`ShopID` = `Catalog`.`ShopID` WHERE `Shops`.`ShopID` = ?";
  public static final String SELECT_SHOPS_BY_MERCHANT_STATEMENT = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `MerchantID` = ?;";
  public static final String SELECT_SHOPS_BATCH_QUERY = "SELECT `ShopID`, `MerchantID`, `ShopName`, `Latitude`, `Longitude`, `AddressLine1`, `TypeOfService` from `Shops` WHERE `ShopID` IN (%s);";
  public static final String SELECT_MERCHANT_STATEMENT = "SELECT `MerchantID`, `MerchantName`, `MerchantPhone` from `Merchants` WHERE `MerchantID` = ?;";
  public static final String SELECT_MERCHANT_BATCH_QUERY = "SELECT `MerchantID`, `MerchantName`, `MerchantPhone` from `Merchants` WHERE `MerchantID` IN (%s);";
  public static final String SELECT_CATALOG_BY_SHOP_STATEMENT = "SELECT `ServiceID`, `ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL` from `Catalog` WHERE `ShopID` = ?;";
  public static final String SELECT_CATALOG_BATCH_QUERY = "SELECT `ServiceID`, `ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL` from `Catalog` WHERE `ShopID` IN (%s);";
  public static final String INSERT_CATALOG_STATEMENT = "INSERT INTO `Catalog` (`ShopID`, `ServiceName`, `ServiceDescription`, `ImageURL`) VALUES %s;";
  public static final String INSERT_CATALOG_PLACEHOLDER = "(?, ?, ?, ?)";
  public static final String UPDATE_CATALOG_STATEMENT = "UPDATE `Catalog` SET `ServiceName` = ?, `ServiceDescription` = ?, `ImageURL` = ? WHERE `ServiceID` = ?;";
  public static final String DELETE_CATALOG_STATEMENT = "DELETE FROM `Catalog` WHERE `ServiceID` IN (%s);";
  public static final String MERCHANT_UPDATE_STATEMENT = "UPDATE `Merchants` SET `MerchantName` = ?, `MerchantPhone` = ? WHERE `MerchantID`= ?;";
  public static final String MERCHANT_INSERT_STATEMENT = "INSERT INTO `Merchants` (`MerchantID`, `MerchantName`, `MerchantPhone`) VALUE (?, ?, ?) ON DUPLICATE KEY UPDATE `MerchantID` = `MerchantID`;";
  public static final String PUBSUB_URL = "projects/speedy-anthem-217710/topics/testTopic";
}