package com.example.bhargav.cleartax.WebServiceManager;

import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.example.bhargav.cleartax.AppController;
import com.example.bhargav.cleartax.Listner.ResultCallBack;
import com.example.bhargav.cleartax.Models.Authenticated;
import com.example.bhargav.cleartax.Models.SearchResults;
import com.example.bhargav.cleartax.Models.Searches;
import com.example.bhargav.cleartax.utility.Utils;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Bhargav on 7/20/2016.
 */
public class NetworkConnector {

    public static String NetworkConnectorTAG = "NetworkConnector";

    public static void makeOauthTokenRequest(String url, final ResultCallBack<Authenticated> resultCallBack) {
        final String urlApiKey, urlApiSecret, base64Encoded;
        try {
            urlApiKey = URLEncoder.encode(Utils.CONSUMER_KEY, "UTF-8");
            urlApiSecret = URLEncoder.encode(Utils.CONSUMER_SECRET, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        String combined = urlApiKey + ":" + urlApiSecret;
        base64Encoded = Base64.encodeToString(combined.getBytes(), Base64.NO_WRAP);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(NetworkConnectorTAG, response.toString());
                if (response != null) {
                    Authenticated authenticated = null;
                    Gson gson = new Gson();
                    authenticated = gson.fromJson(response, Authenticated.class);
                    resultCallBack.onResultCallBack(authenticated, null);
                } else {
                    resultCallBack.onResultCallBack(null, new NullPointerException());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(NetworkConnectorTAG, error.toString());
                resultCallBack.onResultCallBack(null, error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Basic " + base64Encoded);
                params.put("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
                return params;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "client_credentials");
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public static void getMostRecentTweets(final String searchKeyWords, final Authenticated authenticated, String url, final ResultCallBack<Searches> resultCallBack) {

        String encodedUrl = null;
        try {
            encodedUrl = URLEncoder.encode(searchKeyWords, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return;
        }
        JsonObjectRequest mostRecentTweetsJsonObj = new JsonObjectRequest(Request.Method.GET, url+ encodedUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                if (response != null) {
                    Gson gson = new Gson();
                    SearchResults searches = gson.fromJson(String.valueOf(response), SearchResults.class);
                    resultCallBack.onResultCallBack(searches.getStatuses(), null);
                } else {
                    resultCallBack.onResultCallBack(null, new NullPointerException());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                resultCallBack.onResultCallBack(null, error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "Bearer " + authenticated.getAccessToken());
                params.put("Content-Type", "application/json");
                return params;
            }

        };

        AppController.getInstance().addToRequestQueue(mostRecentTweetsJsonObj);
    }
}
