package com.hyperlocal.server.Data;

import com.github.jasync.sql.db.RowData;
import com.google.gson.JsonObject;
import com.google.auto.value.AutoValue;

@AutoValue
abstract class Merchant {
  abstract String merchantID();
  abstract String merchantName();
  abstract String merchantPhone();

  static Merchant create() {
    return new AutoValue_Merchant(null, null, null);
  // }
  // public Merchant() {
  //   this.merchantID = null;
  //   this.merchantName = null;
  //   this.merchantPhone = null;
  // }

  // public Merchant(String merchantID, String merchantName, String merchantPhone) {
  //   this.merchantID = merchantID;
  //   this.merchantName = merchantName;
  //   this.merchantPhone = merchantPhone;
  // }

  // public Merchant(RowData data) {
  //   this.merchantID = (String) data.get("MerchantID");
  //   this.merchantName = (String) data.get("MerchantName");
  //   this.merchantPhone = (String) data.get("MerchantPhone");
  // }

  // public Merchant(JsonObject data) {
  //   this.merchantID = data.get("merchantID").getAsString();
  //   this.merchantName = data.get("merchantName").getAsString();
  //   this.merchantPhone = data.get("merchantPhone").getAsString();
  // }

  // public String getMerchantID() {
  //   return merchantID;
  // }

  // public String getMerchantName() {
  //   return merchantName;
  // }

  // public String getMerchantPhone() {
  //   return merchantPhone;
  // }

  // public boolean equals(Object obj)
  // {
  //   if(obj == null || !(obj instanceof Merchant)) return false;
  //   Merchant merchantObj = (Merchant) obj;
  //   return this.merchantID.equals(merchantObj.merchantID) &&
  //          this.merchantName.equals(merchantObj.merchantName) &&
  //          this.merchantPhone.equals(merchantObj.merchantPhone);
  // }

}