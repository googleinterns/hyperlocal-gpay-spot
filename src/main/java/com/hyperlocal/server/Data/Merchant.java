package com.hyperlocal.server.Data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasync.sql.db.RowData;
import com.google.gson.JsonObject;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Merchant implements Serializable {
  @JsonProperty public abstract String merchantID();
  @JsonProperty public abstract String merchantName();
  @JsonProperty public abstract String merchantPhone();

  public static Merchant create() {
    return new AutoValue_Merchant(null, null, null);
  }

  public static Merchant create(String merchantID, String merchantName, String merchantPhone) {
    return new AutoValue_Merchant(merchantID, merchantName, merchantPhone);
  }

  public static Merchant create(RowData data) {
    return new AutoValue_Merchant(
      (String) data.get("MerchantID"),
      (String) data.get("MerchantName"),
      (String) data.get("MerchantPhone")
    );
  }

  public static Merchant create(JsonObject data) {
    return new AutoValue_Merchant(
      data.get("merchantID").getAsString(),
      data.get("merchantName").getAsString(),
      data.get("merchantPhone").getAsString()
    );
  }
}