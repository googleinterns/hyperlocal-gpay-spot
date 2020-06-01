package com.example.hyperlocal;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import com.github.jasync.sql.db.Connection;
import com.github.jasync.sql.db.QueryResult;
import com.github.jasync.sql.db.ResultSet;
import com.github.jasync.sql.db.mysql.MySQLConnectionBuilder;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// Rest Controller for Merchants
@RestController
public class MerchantController {

  @GetMapping("/merchants/{id}")
  public CompletableFuture<List> getCatalog(@PathVariable Integer id) {

    Connection connection = MySQLConnectionBuilder.createConnectionPool(
        "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
    CompletableFuture<List> future = CompletableFuture.supplyAsync(() -> {
      CompletableFuture<QueryResult> queryResult = connection
          .sendPreparedStatement( 
            String.format("Select * from Merchants where MerchantID = %s", id)
          );
      return queryResult.join().getRows().get(0);
    });
    return future;
  }

  @PostMapping("merchants") 
  public void addCatalogItem() {
    Connection connection = MySQLConnectionBuilder.createConnectionPool(
      "jdbc:mysql:///hyperlocal?socketFactory=com.google.cloud.sql.mysql.SocketFactory&cloudSqlInstance=speedy-anthem-217710:us-central1:hyperlocal");
      connection.sendPreparedStatement( 
          String.format("INSERT INTO Merchants (`ShopName`, `Latitude`, `Longitude`, `TypeOfService`, `AddressLine1`) VALUES ('Dummy Shop', 13.63168521, 42.76488548, 'Groceries', 'Ring Colony');"
      ));
  }
}