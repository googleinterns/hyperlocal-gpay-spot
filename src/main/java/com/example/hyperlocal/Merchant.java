package com.example.hyperlocal;

public class Merchant {
  private Long MerchantID; 
  private String MerchantName;
  private String MerchantPhone;


  public Merchant(Long MerchantID, String MerchantName, String MerchantPhone) {
    this.MerchantID = MerchantID;
    this.MerchantName = MerchantName;
    this.MerchantPhone = MerchantPhone;
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
