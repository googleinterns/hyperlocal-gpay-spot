package com.hyperlocal.server;

import java.util.HashMap;
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

    // Shorthand for a HashMap with error message
    public static HashMap<String, Object> generateError(Object msg)
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("error", msg);
        map.put("success", false);
        return map;
    }

    // Avoid false negatives in Double comparisons due to precision errors
    public static boolean doubleThresholdCompare(Double numA, Double numB, Double threshold)
    {
        return Math.abs(numA-numB) < threshold;
    }

    // Create a ?,?,? placeholder with numOfPlaceholders '?' to use in SQL prepared statements
    public static String getPlaceHolderString(Integer numOfPlaceholders) {      
      if (numOfPlaceholders == 0) {
          return "";
      }
      StringBuilder result = new StringBuilder("?");
      for (Integer i = 0; i < numOfPlaceholders-1; i++) {
        result.append(",?");
      }
      return result.toString();
    }

    public static CompletableFuture<String> verifyAndDecodeIdJwt(String token) {
      return this.getResponseBody(IDENTITY_API_JWKS_URL)
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
                  ex.printStackTrace();
                  return null;
              }
          });
    }

    public static CompletableFuture<String> getResponseBody(String url) {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build();
      return client.sendAsync(request, BodyHandlers.ofString())
            .thenApply(HttpResponse::body);
    }

}
