package com.funplus.sdk;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * The <code>PlayServiceHelper</code> helps to retrieve device's Google Play Advertising ID.
 */
class PlayServicesHelper {
    private static final String LOG_TAG = "PlayServicesHelper";

    interface PlayAdIdReadListener {
        void onPlayAdIdRead(String playAdId);
    }

    /**
     * Get the Google Play Advertising ID, blocking.
     *
     * @param context   The current context.
     * @return          The Google Play Advertising ID, or null.
     */
    @Nullable static String getPlayAdId(@NonNull Context context) {
        try {
            Object advertisingIdInfoObject = Reflection.invokeStaticMethod(
                    "com.google.android.gms.ads.identifier.AdvertisingIdClient",
                    "getAdvertisingIdInfo",
                    new Class[]{Context.class},
                    context
            );
            return (String) Reflection.invokeInstanceMethod(advertisingIdInfoObject, "getId", null);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the Google Play Advertising ID, non blocking.
     *
     * @param context   The current context.
     * @param listener  The result listener.
     */
    static void getPlayAdId(@NonNull Context context, @NonNull final PlayAdIdReadListener listener) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Log.i(LOG_TAG, "Reading GoogleAdId in background thread");
            String playAdId = getPlayAdId(context);
            listener.onPlayAdIdRead(playAdId);

            return;
        }

        new AsyncTask<Context, Void, String>() {
            @Override
            protected String doInBackground(Context... params) {
                Context innerContext = params[0];
                return getPlayAdId(innerContext);
            }

            @Override
            protected void onPostExecute(String playAdId) {
                listener.onPlayAdIdRead(playAdId);
            }
        }.execute(context);
    }
}
