package com.hyperlocal.server;

import com.github.jasync.sql.db.RowData;
import com.google.gson.JsonObject;

import java.math.BigDecimal;

public class Shop {
    public Long ShopID;
    public transient Long MerchantID;
    public String ShopName, AddressLine1, TypeOfService;
    public Double Latitude, Longitude;
    public Shop(Long ShopID, Long MerchantID, String ShopName, Double Latitude, Double Longitude, String AddressLine1, String TypeOfService)
    {
        this.ShopID = ShopID;
        this.MerchantID = MerchantID;
        this.ShopName = ShopName;
        this.AddressLine1 = AddressLine1;
        this.TypeOfService = TypeOfService;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
    }

    public Shop(RowData data)
    {
        this.ShopID = (Long)data.get(0);
        this.MerchantID = (Long)data.get(1);
        this.ShopName = (String)data.get(2);
        this.Latitude = ((BigDecimal)data.get(3)).doubleValue();
        this.Longitude = ((BigDecimal)data.get(4)).doubleValue();
        this.AddressLine1 = (String)data.get(5);
        this.TypeOfService = (String)data.get(6);
    }

    public Shop(JsonObject data) {
      this.ShopID = data.get("ShopID").getAsLong();
      this.MerchantID = data.get("MerchantID").getAsLong();
      this.ShopName = data.get("ShopName").getAsString();
      this.AddressLine1 = data.get("AddressLine1").getAsString();
      this.TypeOfService = data.get("TypeOfService").getAsString();
      this.Latitude = data.get("Latitude").getAsDouble();
      this.Longitude = data.get("Longitude").getAsDouble();
    }

}