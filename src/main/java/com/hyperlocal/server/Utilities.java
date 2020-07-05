package com.hyperlocal.server;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jwk.JsonWebKey;
import org.jose4j.jwk.JsonWebKeySet;
import org.jose4j.jwk.VerificationJwkSelector;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;


public class Utilities {
    public static final String PHONE_API_JWKS_URL = "https://www.googleapis.com/service_accounts/v1/jwk/microapps-signingservice@system.gserviceaccount.com";
    
    // Shorthand for a HashMap with error message
    public HashMap<String, Object> generateError(Object msg)
    {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("error", msg);
        map.put("success", false);
        return map;
    }

    // Avoid false negatives in Double comparisons due to precision errors
    public boolean doubleThresholdCompare(Double numA, Double numB, Double threshold)
    {
        return Math.abs(numA-numB) < threshold;
    }

    public CompletableFuture<String> verifyAndDecodePhoneJWT(String token) {
        return this.getResponseBody(PHONE_API_JWKS_URL)
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

  public static String getPlaceHolderString(Integer numOfPlaceholders) {
    StringBuilder result = new StringBuilder();
    for (Integer i = 0; i < numOfPlaceholders; i++) {
      result.append("?,");
    }
    return result.length() > 0 ? result.substring(0, result.length() - 1) : "";
  }

    public CompletableFuture<String> getResponseBody(String url) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
              .uri(URI.create(url))
              .build();
        return client.sendAsync(request, BodyHandlers.ofString())
              .thenApply(HttpResponse::body);
    }
}