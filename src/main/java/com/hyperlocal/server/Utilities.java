package com.hyperlocal.server;

import java.util.concurrent.CompletableFuture;
import java.util.HashMap;

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

    public static String verifyAndDecodeIdJwt(String token) {
        String jsonWebKeySetString =  getResponseBody(IDENTITY_API_JWKS_URL);
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
    }

    public static String getResponseBody(String url) {
        
        System.out.println("Before time new: "+System.currentTimeMillis());
        HttpCacheContext context = HttpCacheContext.create();
        HttpGet httpget = new HttpGet(url);
        String s = null;
        try {
            CloseableHttpResponse response = Utilities.cachingClient.execute(httpget, context);
            s = EntityUtils.toString(response.getEntity());
            response.close();
            System.out.println("After time new: "+System.currentTimeMillis());
        } finally {
            return s;
        }
    }

}
