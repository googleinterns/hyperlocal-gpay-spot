package com.example.hyperlocal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Merchants {
  private String ShopName; 

  public Merchants(String shop) {
    this.ShopName = shop;
  }
  public String getShopName() {
    return ShopName;
  }
}