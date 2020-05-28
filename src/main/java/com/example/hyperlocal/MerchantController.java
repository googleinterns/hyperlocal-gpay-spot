package com.example.hyperlocal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


// Rest Controller for Merchants
@RestController
public class MerchantController {
  private final JdbcTemplate jdbcTemplate;

  public MerchantController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping("/shops/{id}") 
  public CompletableFuture<Object> getCatalog(@PathVariable Integer id) {
    CompletableFuture<Object> queryResult = CompletableFuture.supplyAsync(() -> {
      try {
        return this.jdbcTemplate.queryForList(
            String.format("Select * from Merchants where MerchantID = %s", id));
      } catch(Exception e) {
        return null;
      }
    }).thenApply((listOfMerchants) -> {
      return listOfMerchants.get(0);
    });
    return queryResult;
  }
}