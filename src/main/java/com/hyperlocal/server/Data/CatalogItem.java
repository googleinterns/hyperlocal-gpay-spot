package com.hyperlocal.server.Data;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.jasync.sql.db.RowData;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class CatalogItem implements Serializable {
    @JsonProperty public abstract Long serviceID();
    @JsonProperty public abstract Long shopID();
    @JsonProperty public abstract String serviceName();
    @JsonProperty public abstract String serviceDescription();
    @JsonProperty public abstract String imageURL();
    
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
