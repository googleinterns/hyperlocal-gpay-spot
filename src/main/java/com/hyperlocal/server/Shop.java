package com.hyperlocal.server;

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
        this.shopID = (Long)data.get(0);
        this.merchantID = (Long)data.get(1);
        this.shopName = (String)data.get(2);
        this.latitude = ((BigDecimal)data.get(3)).doubleValue();
        this.longitude = ((BigDecimal)data.get(4)).doubleValue();
        this.addressLine1 = (String)data.get(5);
        this.typeOfService = (String)data.get(6);
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
             Helper.doubleThresholdCompare(this.latitude, shopObj.latitude, 0.0000001) &&
             Helper.doubleThresholdCompare(this.longitude, shopObj.longitude, 0.0000001);
    }

}