package com.hyperlocal.server;

import com.github.jasync.sql.db.RowData;

public class Service {
    public Long serviceID;
    public transient Long shopID;
    public String serviceName, serviceDescription, imageURL;
    
    public Service(RowData data)
    {
        this.serviceID = (Long)data.get(0);
        this.shopID = (Long)data.get(1);
        this.serviceName = (String)data.get(2);
        this.serviceDescription = (String)data.get(3);
        this.imageURL = (String)data.get(4);
    }
}