package com.hyperlocal.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class Utilities {

  /**
   * Generate a HashMap for error object
   * @param msg The error message
   * @return HashMap of {@code<"error", "message">}, {@code <"success", false>}
   */
  public static HashMap<String, Object> generateError(Object msg) {
    HashMap<String, Object> map = new HashMap<String, Object>();
    map.put("error", msg);
    map.put("success", false);
    return map;
  }

  /**
   * Compare if two doubles are in threshold range of each other
   * @param numA      the first double
   * @param numB      the second double
   * @param threshold the threshold difference for equality
   * @return true if the numbers are within {@code threshold} range of each other,
   *         false otherwise
   */
  public static boolean doubleThresholdCompare(Double numA, Double numB, Double threshold) {
    return Math.abs(numA - numB) < threshold;
  }

  /**
   * Create a (?,?,?....?) placeholder for SQL prepared statements
   * @param numOfPlaceholders the number of {@code ?} required in placeholder
   * @return Comma separated string of {@code ?}s
   */
  public static String getPlaceHolderString(Integer numOfPlaceholders) {
    if (numOfPlaceholders == 0) {
      return "";
    }
    StringBuilder result = new StringBuilder("?");
    for (Integer i = 0; i < numOfPlaceholders - 1; i++) {
      result.append(",?");
    }
    return result.toString();
  }

  /**
   * Make a get request to {@code URL} and get Response Body
   * @param URL: The URL to make a a get request too
   * @return the completableFuture of the response String
   */
  public CompletableFuture<String> getResponseBody(String URL) {
    // Create the HTTP Request to send
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(Constants.SEARCH_INDEX_URL))
        .method("GET", HttpRequest.BodyPublishers.ofString(URL))
        .setHeader("Content-Type", "application/json").build();
    return client.sendAsync(request, BodyHandlers.ofString())
      .thenApply(HttpResponse::body);
  }
}
