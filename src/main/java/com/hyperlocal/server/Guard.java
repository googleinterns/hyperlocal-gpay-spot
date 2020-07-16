package com.hyperlocal.server;

import org.springframework.stereotype.Component;
import org.springframework.security.core.Authentication;
import javax.servlet.http.HttpServletRequest;

@Component
public class Guard {
    public boolean hasValidIdToken(HttpServletRequest request, String suppliedMerchantID) {
        String encodedIdToken = request.getHeader("X-Authorization");
        if(encodedIdToken == null) return false;

        String idToken = Utilities.verifyAndDecodeIdJwt(encodedIdToken);
        if(idToken == null) return false;

        String actualMerchantID = JsonParser.parseString(idToken).getAsJsonObject().get("sub").getAsString();
        if(actualMerchantID.equals(suppliedMerchantID)) return true;
        else return false;
    }
}