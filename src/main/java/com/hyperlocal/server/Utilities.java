package com.hyperlocal.server;

import java.util.concurrent.CompletableFuture;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.cache.CacheResponseStatus;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.VerificationJwkSelector;

import org.apache.http.util.EntityUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClients;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.cache.CacheResponseStatus;


import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.VerificationJwkSelector;

public class Utilities {
  public static final String IDENTITY_API_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";
  public static CacheConfig cacheConfig = CacheConfig.custom()
          .setMaxCacheEntries(10)
          .setMaxObjectSize(10000)
          .build();
  public static RequestConfig requestConfig = RequestConfig.custom()
          .setConnectTimeout(30000)
          .setSocketTimeout(30000)
          .build();
  public static CloseableHttpClient cachingClient = CachingHttpClients.custom()
          .setCacheConfig(cacheConfig)
          .setDefaultRequestConfig(requestConfig)
          .build();
  public static HttpClient regularClient = HttpClient.newHttpClient();
    
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
   * Verify a JWT token against Google's JWKS
   * @param token The JWT token that needs to be verified
   * @return The CompletableFuture of the decoded token, if verified successfully. Otherwise, {@code null}.
   */
  public static String verifyAndDecodeIdJwt(String token) {
      String jsonWebKeySetString =  getCachedResponseBody(IDENTITY_API_JWKS_URL);
      try {
          // Set token and algorithm
          JsonWebSignature jsonWebSignature = new JsonWebSignature();
          jsonWebSignature.setAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.PERMIT,   AlgorithmIdentifiers.RSA_USING_SHA256));
          jsonWebSignature.setCompactSerialization(token);

          // Find and use relevant JWK
          JsonWebKeySet jsonWebKeySet = new JsonWebKeySet(jsonWebKeySetString);
          JsonWebKey jsonWebKey = new VerificationJwkSelector().select(jsonWebSignature, jsonWebKeySet.getJsonWebKeys());
          jsonWebSignature.setKey(jsonWebKey.getKey());

          if(!jsonWebSignature.verifySignature()) return null;

          return jsonWebSignature.getPayload();
      } catch(Exception ex) {
          Logger.getLogger("JwtVerification").log(Level.WARNING, ex.getMessage(), ex);
          return null;
      }
  }

  /**
   * Get cached response body of an HTTP GET request. If cache doesn't exist, make a new request.
   * @param URL The URL to make the request to.
   * @return The response body.
   */
  private static String getCachedResponseBody(String url) {
      HttpCacheContext context = HttpCacheContext.create();
      HttpGet httpget = new HttpGet(url);
      String responseBody = null;
      try {
          CloseableHttpResponse response = Utilities.cachingClient.execute(httpget, context);
          responseBody = EntityUtils.toString(response.getEntity());
          response.close();
      } finally {
          return responseBody;
      }
  }
  
  /**
   * Make an HTTP GET request and get the response's body.
   * @param URL The URL to make the request to.
   * @param requestBody The body of the GET request. {@code null} value refers to empty body.
   * @return The CompletableFuture of the response string.
   */
  public CompletableFuture<String> getResponseBody(String URL, String requestBody) {
    Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(URL));
    if(requestBody != null) {
      requestBuilder = requestBuilder
        .method("GET", HttpRequest.BodyPublishers.ofString(requestBody))
        .setHeader("Content-Type", "application/json");
    }
    HttpRequest request = requestBuilder.build();
    return regularClient.sendAsync(request, BodyHandlers.ofString())
      .thenApply(HttpResponse::body);
  }

}
