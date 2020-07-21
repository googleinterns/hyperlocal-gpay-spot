package com.hyperlocal.server;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

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

public class Utilities {
    public static final String IDENTITY_API_JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";

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
  public static CompletableFuture<String> verifyAndDecodeIdJwt(String token) {
    return getResponseBody(IDENTITY_API_JWKS_URL, null)
        .thenApply((jsonWebKeySetString) -> {
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
        });
  }
  
  /**
   * Make an HTTP GET request and get the response's body.
   * @param URL The URL to make the request to.
   * @param requestBody The body of the GET request. {@code null} value refers to empty body.
   * @return The CompletableFuture of the response string
   */
  public CompletableFuture<String> getResponseBody(String URL, String requestBody) {
    // Create the HTTP Request to send
    HttpClient client = HttpClient.newHttpClient();
    Builder requestBuilder = HttpRequest.newBuilder().uri(URI.create(URL));
    if(requestBody != null) {
      requestBuilder = requestBuilder
        .method("GET", HttpRequest.BodyPublishers.ofString(requestBody))
        .setHeader("Content-Type", "application/json");
    }
    HttpRequest request = requestBuilder.build();
    return client.sendAsync(request, BodyHandlers.ofString())
      .thenApply(HttpResponse::body);
  }

}
