package com.funplus.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The `ConfigManager` class manages SDK configurations. It handles with the following tasks:
 *
 * <ul>
 *     <li>1. Parse configurations from default config file.</li>
 *     <li>2. Unarchive/archive configurations from/to local storage.</li>
 *     <li>3. Fetch configurations from remote config server.</li>
 * </ul>
 */
class ConfigManager implements Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "ConfigManager";

    private static final String SANDBOX_ENDPOINT = "https://fpcs-sandbox.funplusgame.com/service/gameconf";
    private static final String PRODUCTION_ENDPOINT = "https://fpcs.funplusgame.com/service/gameconf";

    private static final String CONFIG_DEFAULT_FILE_NAME = "funsdk-default-config.json";
    private static final String CONFIG_ETAG_SAVED_KEY = "com.funplus.sdk.ConfigEtag";
    private static final String CONFIG_DICT_SAVED_KEY = "com.funplus.sdk.ConfigDict";

    @NonNull private final Context context;
    @NonNull private final String appId;
    @NonNull private final String appKey;
    @NonNull private final SDKEnvironment env;
    @NonNull private final String endpoint;
    @NonNull private String configEtag;
    @NonNull private JSONObject configDict;

    @Nullable private Timer timer;
    private final long syncInterval;

    ConfigManager(@NonNull Context context,
                  @NonNull String appId,
                  @NonNull String appKey,
                  @NonNull SDKEnvironment environment,
                  long syncInterval) {
        this.context = context;
        this.appId = appId;
        this.appKey = appKey;
        this.env = environment;
        this.syncInterval = syncInterval * 1000;    // seconds -> milliseconds

        endpoint = env.equals(SDKEnvironment.Sandbox) ? SANDBOX_ENDPOINT : PRODUCTION_ENDPOINT;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        configEtag = preferences.getString(CONFIG_ETAG_SAVED_KEY, "");

        configDict = load();

        archive();
        startTimer();
        FunPlusFactory.getActivityLifecycleCallbacksProxy().add(this);
    }

    ConfigManager(@NonNull Context context,
                  @NonNull String appId,
                  @NonNull String appKey,
                  @NonNull SDKEnvironment environment) {
        this(context, appId, appKey, environment, 0);
    }

    @NonNull String getConfigEtag() {
        return configEtag;
    }

    @NonNull FunPlusConfig getFunPlusConfig() throws JSONException {
        return new FunPlusConfig(context, appId, appKey, env, configEtag, configDict);
    }

    /**
     * Load app configurations.
     *
     * @return the config JSON object.
     */
    @NonNull private JSONObject load() {
        // 1. Unarchive from local storage.
//        JSONObject dict = unarchive();
//        if (dict != null) {
//            return dict;
//        }

        JSONObject dict;

        // 2. Parse the default config file.
        dict = parse();
        if (dict != null) {
            return dict;
        }

        // 3. Otherwise, something is wrong. Return an empty JSON object.
        return new JSONObject();
    }

    private JSONObject parse() {
        String fileContent = readObjectFromAssetsFile(context, CONFIG_DEFAULT_FILE_NAME);

        if (fileContent == null) {
            Log.e(LOG_TAG, "Failed to parse default config");
            return null;
        }

        try {
            JSONObject configDict = new JSONObject(fileContent).getJSONObject(env.name().toLowerCase());
            Log.i(LOG_TAG, "Parsed default config: " + configDict.toString());
            return configDict;
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to parse default config: " + e.getMessage());
            return null;
        }
    }

    private void fetch(@NonNull Response.Listener<FPCSResponse> listener,
                       @NonNull Response.ErrorListener errorListener) {
        String appVersion = FunPlusFactory.getDeviceInfo(context).appVersion;
        String url = String.format(Locale.US, "%s?app_id=%s&app_version=%s&platform=%s", endpoint, appId, appVersion, "android");

        Map<String, String> headers = new HashMap<>();
        headers.put("If-None-Match", configEtag);

        ConfigFetchRequest request = new ConfigFetchRequest(url, headers, listener, errorListener);
        FunPlusFactory.getRequestQueue(context).add(request);
    }

    private void sync() {
        Response.Listener<FPCSResponse> listener = new Response.Listener<FPCSResponse>() {
            @Override
            public void onResponse(FPCSResponse response) {
                if (!response.configEtag.isEmpty() && !configEtag.equals(response.configEtag)) {
                    configEtag = response.configEtag;
                    configDict = response.configDict;
                    archive();
                }
            }
        };

        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(LOG_TAG, "Failed to sync config with remote: " + error.getMessage());
            }
        };

        fetch(listener, errorListener);
    }

    private void archive() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CONFIG_ETAG_SAVED_KEY, configEtag);
        editor.putString(CONFIG_DICT_SAVED_KEY, configDict.toString());
        editor.apply();
    }

    private JSONObject unarchive() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String config = preferences.getString(CONFIG_ETAG_SAVED_KEY, "");

        try {
            return new JSONObject(config);
        } catch (JSONException e) {
            Log.w(LOG_TAG, "Failed to unarchive config dict");
            return null;
        }
    }

    private void startTimer() {
        if (timer == null && syncInterval > 0) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sync();
                }
            }, syncInterval, syncInterval);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable Timer getTimer() {
        return timer;
    }

    @Nullable private static String readObjectFromAssetsFile(@NonNull Context context,
                                                             @NonNull String filename) {
        Closeable closeable = null;
        try {
            StringBuilder content = new StringBuilder();

            InputStream inputStream = context.getResources().getAssets().open(filename);
            closeable = inputStream;

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            closeable = inputStreamReader;

            BufferedReader reader = new BufferedReader(inputStreamReader);
            closeable = reader;

            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            return  content.toString();
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to open file for reading: " + e.getMessage());
        }

        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to close file for reading: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        // Do nothing.
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // Do nothing.
    }

    @Override
    public void onActivityResumed(Activity activity) {
        startTimer();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        stopTimer();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // Do nothing.
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        // Do nothing.
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        // Do nothing.
    }

    private class FPCSResponse {
        @NonNull final String configEtag;
        @NonNull final JSONObject configDict;

        FPCSResponse(@NonNull String configEtag,
                     @NonNull JSONObject configDict) {
            this.configEtag = configEtag;
            this.configDict = configDict;
        }
    }

    private class ConfigFetchRequest extends Request<FPCSResponse> {

        @NonNull private final Map<String, String> headers;
        @NonNull private final Response.Listener<FPCSResponse> listener;

        ConfigFetchRequest(@NonNull String url,
                           @NonNull Map<String, String> headers,
                           @NonNull Response.Listener<FPCSResponse> listener,
                           @NonNull Response.ErrorListener errorListener) {
            super(Method.GET, url, errorListener);
            this.headers = headers;
            this.listener = listener;
        }

        @Override
        @NonNull public Map<String, String> getHeaders() throws AuthFailureError {
            return headers;
        }

        @Override
        public void deliverResponse(@NonNull FPCSResponse response) {
            listener.onResponse(response);
        }

        @Override
        @NonNull protected Response<FPCSResponse> parseNetworkResponse(@NonNull NetworkResponse response) {
            String configEtag = response.headers.containsKey("Etag") ? response.headers.get("Etag") : "";

            try {
                String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "UTF-8"));
                JSONObject configDict = new JSONObject(jsonString);
                return Response.success(new FPCSResponse(configEtag, configDict), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new ParseError(e));
            } catch (JSONException e) {
                return Response.error(new ParseError(e));
            }
        }
    }
}