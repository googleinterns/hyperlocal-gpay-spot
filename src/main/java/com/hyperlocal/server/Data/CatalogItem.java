package com.hyperlocal.server.Data;

import java.io.Serializable;

import com.github.jasync.sql.db.RowData;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CatalogItem implements Serializable {
    public  abstract Long serviceID();
    public  abstract Long shopID();
    public  abstract String serviceName();
    public  abstract String serviceDescription();
    public  abstract String imageURL();
    
    public static CatalogItem create(RowData data)
    {
        return new AutoValue_CatalogItem(
            (Long)data.get("ServiceID"),
            (Long)data.get("ShopID"),
            (String)data.get("ServiceName"),
            (String)data.get("ServiceDescription"),
            (String)data.get("ImageURL")
        );
    }
}