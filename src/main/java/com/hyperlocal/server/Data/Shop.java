package com.hyperlocal.server.Data;

import com.hyperlocal.server.Utilities;
import com.github.jasync.sql.db.RowData;
import com.google.gson.JsonObject;
import java.math.BigDecimal;

public class Shop {
    public Long shopID;
    public Long merchantID;
    public String shopName, addressLine1, typeOfService;
    public Double latitude, longitude;
    
    public Shop() {
      this.shopID = 1L;
      this.merchantID = 1L;
      this.shopName = "Test";
      this.addressLine1 = "TEST";
      this.typeOfService = "TEST";
      this.latitude = 32.32;
      this.longitude = 32.32;
    }

    public Shop(Long shopID, Long merchantID, String shopName, Double latitude, Double longitude, String addressLine1, String typeOfService)
    {
        this.shopID = shopID;
        this.merchantID = merchantID;
        this.shopName = shopName;
        this.addressLine1 = addressLine1;
        this.typeOfService = typeOfService;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Shop(RowData data)
    {
        this.shopID = (Long)data.get("ShopID");
        this.merchantID = (Long)data.get("MerchantID");
        this.shopName = (String)data.get("ShopName");
        this.latitude = ((BigDecimal)data.get("Latitude")).doubleValue();
        this.longitude = ((BigDecimal)data.get("Longitude")).doubleValue();
        this.addressLine1 = (String)data.get("AddressLine1");
        this.typeOfService = (String)data.get("TypeOfService");
    }

    public Shop(JsonObject data) {
      this.shopID = data.get("shopID").getAsLong();
      this.merchantID = data.get("merchantID").getAsLong();
      this.shopName = data.get("shopName").getAsString();
      this.addressLine1 = data.get("addressLine1").getAsString();
      this.typeOfService = data.get("typeOfService").getAsString();
      this.latitude = data.get("latitude").getAsDouble();
      this.longitude = data.get("longitude").getAsDouble();
    }

    public boolean equals(Object obj)
    {
      if(obj == null || !(obj instanceof Shop)) return false;
      Shop shopObj = (Shop) obj;
      return this.shopID.equals(shopObj.shopID) &&
             this.merchantID.equals(shopObj.merchantID) &&
             this.shopName.equals(shopObj.shopName) &&
             this.addressLine1.equals(shopObj.addressLine1) &&
             this.typeOfService.equals(shopObj.typeOfService) &&
             Utilities.doubleThresholdCompare(this.latitude, shopObj.latitude, 0.0000001) &&
             Utilities.doubleThresholdCompare(this.longitude, shopObj.longitude, 0.0000001);
    }

}