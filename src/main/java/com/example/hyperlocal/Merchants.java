package com.example.hyperlocal;

public class Merchants {
  private Integer MerchantID; 
  private String MerchantName;
  private String MerchantPhone;


  public Merchants(Integer MerchantID, String MerchantName, String MerchantPhone) {
    this.MerchantID = MerchantID;
    this.MerchantName = MerchantName;
    this.MerchantPhone = MerchantPhone;
  }

  public Integer getMerchantID() {
    return MerchantID;
  }

  public String getMerchantName() {
    return MerchantName;
  }

  public String getMerchantPhone() {
    return MerchantPhone;
  }
}