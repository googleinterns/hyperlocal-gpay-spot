package com.example.hyperlocal;

import com.github.jasync.sql.db.RowData;

public class Service {
    public Long ServiceID;
    public transient Long ShopID;
    public String ServiceName, ServiceDescription, ImageURL;
    
    public Service(RowData data)
    {
        this.ServiceID = (Long)data.get(0);
        this.ShopID = (Long)data.get(1);
        this.ServiceName = (String)data.get(2);
        this.ServiceDescription = (String)data.get(3);
        this.ImageURL = (String)data.get(4);
    }
}