package com.hyperlocal.server;
import java.util.HashMap;

public class Helper {
    
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
}