package com.hyperlocal.server;

import com.github.jasync.sql.db.RowData;

public class CatalogItem {
    public Long serviceID;
    public transient Long shopID;
    public String serviceName, serviceDescription, imageURL;
    
    public CatalogItem(RowData data)
    {
        this.serviceID = (Long)data.get(0);
        this.shopID = (Long)data.get(1);
        this.serviceName = (String)data.get(2);
        this.serviceDescription = (String)data.get(3);
        this.imageURL = (String)data.get(4);
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