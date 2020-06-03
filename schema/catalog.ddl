CREATE TABLE `Catalog` (
  `ServiceID` BIGINT NOT NULL,
  `ShopID` BIGINT NOT NULL,
  `ServiceName` VARCHAR(255) NOT NULL,
  `ServiceDescription` TEXT NOT NULL,
  `ImageURL` VARCHAR(1023),
  PRIMARY KEY(`ServiceID`),
  FOREIGN KEY(`ShopID`) REFERENCES `Shops`(`ShopID`)
);