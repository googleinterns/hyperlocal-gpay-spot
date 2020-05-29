package com.example.hyperlocal;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.io.*;
import java.util.*;
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

	@GetMapping("/getMerchants")
	public List<String> getMerchants() {
		return this.jdbcTemplate.queryForList("SELECT * FROM `Merchants`").stream()
				.map((m) -> m.values().toString())
				.collect(Collectors.toList());
	}

	@GetMapping("/getTuples")
	public List<String> getTuples() {
		return this.jdbcTemplate.queryForList("SELECT * FROM users").stream()
				.map((m) -> m.values().toString())
				.collect(Collectors.toList());
	}
}
