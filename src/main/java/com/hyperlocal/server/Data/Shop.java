package com.hyperlocal.server.Data;

import java.io.Serializable;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasync.sql.db.RowData;
import com.google.auto.value.AutoValue;
import com.google.gson.JsonObject;

@AutoValue
public abstract class Shop implements Serializable{
  private static final long serialVersionUID = 1L;

  public abstract Long shopID();
    @JsonProperty public abstract String merchantID();
    @JsonProperty public abstract String shopName();
    @JsonProperty public abstract String addressLine1();
    @JsonProperty public abstract String typeOfService();
    @JsonProperty public abstract Double latitude();
    @JsonProperty public abstract Double longitude();
    
    public static Shop create() {
      return new AutoValue_Shop(
        0L,
        "1",
        "",
        "",
        "",
        0.00,
        0.00
      );
    }

    public static Shop create(Long shopID, String merchantID, String shopName, Double latitude, Double longitude, String addressLine1, String typeOfService)
    {
      return new AutoValue_Shop(
        shopID,
        merchantID,
        shopName,
        addressLine1,
        typeOfService,
        latitude,
        longitude
      );
    }

    public static Shop create(RowData data)
    {
      return new AutoValue_Shop(
        (Long)data.get("ShopID"),
        (String)data.get("MerchantID"),
        (String)data.get("ShopName"),        
        (String)data.get("AddressLine1"),
        (String)data.get("TypeOfService"),
        ((BigDecimal)data.get("Latitude")).doubleValue(),
        ((BigDecimal)data.get("Longitude")).doubleValue()
      );
    }

    public static Shop create(JsonObject data) {
      return new AutoValue_Shop(
        data.get("shopID").getAsLong(),
        data.get("merchantID").getAsString(),
        data.get("shopName").getAsString(),
        data.get("addressLine1").getAsString(),
        data.get("typeOfService").getAsString(),
        data.get("latitude").getAsDouble(),
        data.get("longitude").getAsDouble()
      );
    }
}