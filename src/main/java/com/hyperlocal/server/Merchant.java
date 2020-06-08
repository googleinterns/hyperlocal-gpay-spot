package com.hyperlocal.server;

import com.github.jasync.sql.db.RowData;
import com.google.gson.JsonObject;

public class Merchant {
  public Long MerchantID; 
  public String MerchantName;
  public String MerchantPhone;

  public Merchant() {
    this.MerchantID = null;
    this.MerchantName = null;
    this.MerchantPhone = null;
  }

  public Merchant(Long MerchantID, String MerchantName, String MerchantPhone) {
    this.MerchantID = MerchantID;
    this.MerchantName = MerchantName;
    this.MerchantPhone = MerchantPhone;
  }

  public Merchant(RowData data)
  {
    this.MerchantID = (Long)data.get(0);
    this.MerchantName = (String)data.get(1);
    this.MerchantPhone = (String)data.get(2);
  }

  public Merchant(JsonObject data) {
    this.MerchantID = data.get("MerchantID").getAsLong();
    this.MerchantName = data.get("MerchantName").getAsString();
    this.MerchantPhone = data.get("MerchantPhone").getAsString();
  }

  public Long getMerchantID() {
    return MerchantID;
  }

  public String getMerchantName() {
    return MerchantName;
  }

  public String getMerchantPhone() {
    return MerchantPhone;
  }
}