package com.hyperlocal.server;

import com.google.gson.JsonParser;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;
import java.util.logging.Level;

@Component
public class Guard {
    public boolean hasValidIdToken(HttpServletRequest request, String suppliedMerchantID) {
        String encodedIdToken = request.getHeader("X-Authorization");
        if(encodedIdToken == null) return false;
        
        String idToken = null;
        try
        {
            idToken = Utilities.get().verifyAndDecodeIdJwt(encodedIdToken);
        }
        catch(Exception e)
        {
            // idToken remains set to null: access denied.
            Logger.getLogger("Auth").log(Level.WARNING, e.getMessage(), e);
        }
        if(idToken == null) return false;

        JsonObject idTokenObject = JsonParser.parseString(idToken).getAsJsonObject();
        if(!idTokenObject.has("sub")) return false;

        String actualMerchantID = idTokenObject.get("sub").getAsString();
        if(actualMerchantID.equals(suppliedMerchantID)) return true;
        else return false;
    }
}