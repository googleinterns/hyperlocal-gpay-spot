package com.example.hyperlocal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Merchants {
  private Integer MerchantID;
  private String ShopName; 

  public Merchants(Integer id, String shop) {
    this.MerchantID = id;
    this.ShopName = shop;
  }

  public Integer getMerchanID() {
    return MerchantID;
  }

  public String getShopName() {
    return ShopName;
  }
}