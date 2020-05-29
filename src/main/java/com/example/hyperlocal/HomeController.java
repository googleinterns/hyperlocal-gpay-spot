package com.example.hyperlocal;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.JdbcTemplate;

@RestController
public class HomeController {
	
	private final JdbcTemplate jdbcTemplate;

	public HomeController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("/hello")
	public String index() {
		return "Hello, world!";
	}

	@GetMapping("/api/v1/shops/{merchantID}")
    public List getCatalog(@PathVariable("merchantID") String merchantID) {
		return this.jdbcTemplate.queryForList("SELECT * FROM `Catalog` WHERE `MerchantID` = '"+merchantID+"'").stream()
		.map((m) -> m.values().toString())
		.collect(Collectors.toList());
	}
	
	@GetMapping("/api/v2/shops/{merchantID}")
    public CompletableFuture getCatalogAsync(@PathVariable("merchantID") String merchantID) {
		CompletableFuture promise = CompletableFuture.supplyAsync(() -> {
				return this.jdbcTemplate.queryForList("SELECT * FROM `Catalog` WHERE `MerchantID` = '"+merchantID+"'"); 
			}).thenApply((itemsList) -> {
				return itemsList.stream().map((m) -> m.values().toString()).collect(Collectors.toList());
			});
		return promise;
	}

	@GetMapping("/getMerchants")
	public List<String> getMerchants() {
		return this.jdbcTemplate.queryForList("SELECT * FROM `Merchants`").stream()
		.map((m) -> m.values().toString())
		.collect(Collectors.toList());
	}
}
