CREATE TABLE Catalog (
 ServiceID INT64 NOT NULL,
 ShopID INT64 NOT NULL,
 CanServiceAtHome BOOL NOT NULL,
 TypeOfService STRING(MAX) NOT NULL,
 FOREIGN KEY(ShopID) REFERENCES Shops(ShopID),
) PRIMARY KEY(ShopID, ServiceID),
 INTERLEAVE IN PARENT Shops ON DELETE CASCADE


 //we'll need a timestamp field here if we use spanner-event-exporter

