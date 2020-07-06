package com.hyperlocal.server;
import java.util.HashMap;

public class Utilities {
    
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
}
