package com.hyperlocal.server.Data;

import java.util.List;
import java.util.ArrayList;

/**
 * @author: Diksha, Onish
 * @version: 1.0
 * @since: 1.0
 */
public class ShopDetails {
    public Shop shop;
    public Merchant merchant;
    public List<CatalogItem> catalog;

    public ShopDetails()
    {
        this.shop = null;
        this.merchant = null;
        this.catalog = new ArrayList<CatalogItem>();
    }

    public ShopDetails(Shop shop)
    {
        this.shop = shop;
        this.merchant = null;
        this.catalog = new ArrayList<CatalogItem>();
    }

    public ShopDetails(Shop shop, Merchant merchant, List<CatalogItem> catalog)
    {
        this.shop = shop;
        this.merchant = merchant;
        this.catalog = catalog;
    }

    public void setShop(Shop shop)
    {
        this.shop = shop;
    }

    public void setMerchant(Merchant merchant)
    {
        this.merchant = merchant;
    }

    public void addCatalogItem(CatalogItem catalogItem)
    {
        this.catalog.add(catalogItem);
    }

    public boolean equals(Object obj)
    {
      if(obj == null || !(obj instanceof ShopDetails)) return false;
      ShopDetails shopDetailsObj = (ShopDetails) obj;
      return this.shop.equals(shopDetailsObj.shop) &&
             this.merchant.equals(shopDetailsObj.merchant) &&
             this.catalog.equals(shopDetailsObj.catalog);       
    }

}