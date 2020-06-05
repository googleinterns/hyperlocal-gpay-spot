CREATE TABLE `Shops` (
  `ShopID` BIGINT NOT NULL,
  `MerchantID` BIGINT NOT NULL,
  `ShopName` VARCHAR(255) NOT NULL,
  `Latitude` DECIMAL(10, 8) NOT NULL,
  `Longitude` DECIMAL(11, 8) NOT NULL,
  `AddressLine1` VARCHAR(1023) NOT NULL,
  `TypeOfService` VARCHAR(255) NOT NULL,
  PRIMARY KEY(`ShopID`),
  FOREIGN KEY(`MerchantID`) REFERENCES `Merchants`(`MerchantID`)
);