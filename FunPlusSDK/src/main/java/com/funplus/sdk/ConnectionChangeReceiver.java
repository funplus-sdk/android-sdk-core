package com.funplus.sdk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * The <code>ConnectionChangeReceiver</code> class listens to network changes.
 */
public class ConnectionChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        IFunPlusRUM funPlusRUM = FunPlusSDK.getFunPlusRUM();
        if (funPlusRUM != null) {
            funPlusRUM.onConnectionChange(networkInfo);
        }
    }
}
