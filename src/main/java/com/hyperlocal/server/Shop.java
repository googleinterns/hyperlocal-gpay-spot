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
      this.shopID = data.get("ShopID").getAsLong();
      this.merchantID = data.get("MerchantID").getAsLong();
      this.shopName = data.get("ShopName").getAsString();
      this.addressLine1 = data.get("AddressLine1").getAsString();
      this.typeOfService = data.get("TypeOfService").getAsString();
      this.latitude = data.get("Latitude").getAsDouble();
      this.longitude = data.get("Longitude").getAsDouble();
    }

}