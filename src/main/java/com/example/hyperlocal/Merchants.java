package com.example.hyperlocal;

public class Merchants {
  private String ShopName; 
  private double Latitude;
  private double Longitude;
  private String TypeOfService;
  private String AddressLine;


  public Merchants(String shopName, Double latitude, Double longitude, String serviceType, String address) {
    this.ShopName = shopName;
    this.Latitude = latitude;
    this.Longitude = longitude;
    this.TypeOfService = serviceType;
    this.AddressLine = address;
  }

  public String getShopName() {
    return ShopName;
  }

  public String getTypeOfService() {
    return TypeOfService;
  }

  public String getAddressLine1() {
    return AddressLine;
  }

  public Double getLatitude(){
    return Latitude;
  }

  public Double getLongitude() {
    return Longitude;
  }
}