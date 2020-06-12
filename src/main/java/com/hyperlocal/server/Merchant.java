package com.hyperlocal.server;

import com.github.jasync.sql.db.RowData;
import com.google.gson.JsonObject;

public class Merchant {
  public Long merchantID;
  public String merchantName;
  public String merchantPhone;

  public Merchant() {
    this.merchantID = null;
    this.merchantName = null;
    this.merchantPhone = null;
  }

  public Merchant(Long merchantID, String merchantName, String merchantPhone) {
    this.merchantID = merchantID;
    this.merchantName = merchantName;
    this.merchantPhone = merchantPhone;
  }

  public Merchant(RowData data) {
    this.merchantID = (Long) data.get(0);
    this.merchantName = (String) data.get(1);
    this.merchantPhone = (String) data.get(2);
  }

  public Merchant(JsonObject data) {
    this.merchantID = data.get("MerchantID").getAsLong();
    this.merchantName = data.get("MerchantName").getAsString();
    this.merchantPhone = data.get("MerchantPhone").getAsString();
  }

  public Long getMerchantID() {
    return merchantID;
  }

  public String getMerchantName() {
    return merchantName;
  }

  public String getMerchantPhone() {
    return merchantPhone;
  }
}