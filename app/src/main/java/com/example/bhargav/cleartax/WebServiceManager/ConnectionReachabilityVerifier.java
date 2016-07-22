package com.example.bhargav.cleartax.WebServiceManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Bhargav on 7/20/2016.
 */
public class ConnectionReachabilityVerifier {

    public static String TAG = "ConnectionReachabilityVerifier";

    public static boolean isConnectionReachable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            Log.e(TAG, "No network connection available.");
            return false;
        }
    }

}
