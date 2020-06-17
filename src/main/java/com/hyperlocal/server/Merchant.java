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
    this.merchantID = (Long) data.get("MerchantID");
    this.merchantName = (String) data.get("MerchantName");
    this.merchantPhone = (String) data.get("MerchantPhone");
  }

  public Merchant(JsonObject data) {
    this.merchantID = data.get("merchantID").getAsLong();
    this.merchantName = data.get("merchantName").getAsString();
    this.merchantPhone = data.get("merchantPhone").getAsString();
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

  public boolean equals(Object obj)
  {
    if(obj == null || !(obj instanceof Merchant)) return false;
    Merchant merchantObj = (Merchant) obj;
    return this.merchantID.equals(merchantObj.merchantID) &&
           this.merchantName.equals(merchantObj.merchantName) &&
           this.merchantPhone.equals(merchantObj.merchantPhone);
  }

}