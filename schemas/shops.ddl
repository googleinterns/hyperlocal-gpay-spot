CREATE TABLE Shops (
 ShopID INT64 NOT NULL,
 ShopName STRING(MAX) NOT NULL,
 Latitude FLOAT64 NOT NULL,
 Longitude FLOAT64 NOT NULL,
 AddressLine1 STRING(MAX) NOT NULL,
 IsGpayAccepted BOOL NOT NULL,
 IsCardAccepted BOOL NOT NULL,
 IsCashAccepted BOOL NOT NULL,
 ShopPhoneNumber STRING(10),
) PRIMARY KEY(ShopID);


// if we use spanner-event-exporter, we'll be needing a timestamp field too
