package com.hyperlocal.server.Data;

import com.github.jasync.sql.db.RowData;

public class CatalogItem {
    public Long serviceID;
    public transient Long shopID;
    public String serviceName, serviceDescription, imageURL;
    
    public CatalogItem(RowData data)
    {
        this.serviceID = (Long)data.get("ServiceID");
        this.shopID = (Long)data.get("ShopID");
        this.serviceName = (String)data.get("ServiceName");
        this.serviceDescription = (String)data.get("ServiceDescription");
        this.imageURL = (String)data.get("ImageURL");
    }

    public boolean equals(Object obj)
    {
      if(obj == null || !(obj instanceof CatalogItem)) return false;
      CatalogItem serviceObj = (CatalogItem) obj;
      return this.serviceID.equals(serviceObj.serviceID) &&
             this.shopID.equals(serviceObj.shopID) &&
             this.serviceName.equals(serviceObj.serviceName) &&
             this.serviceDescription.equals(serviceObj.serviceDescription) &&
             this.imageURL.equals(serviceObj.imageURL);
    }
}