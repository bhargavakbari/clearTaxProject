package com.example.bhargav.cleartax.Models;

/**
 * Created by Bhargav on 7/20/2016.
 */
public class Authenticated {
    public String token_type;
    public String access_token;

    public String getTokenType() {
        return token_type;
    }

    public String getAccessToken() {
        return access_token;
    }
}
