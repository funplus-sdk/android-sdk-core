package com.funplus.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FunPlusRUM implements IFunPlusRUM, Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "FunPlusRUM";
    private static final String LOGGER_LABEL = "com.funplus.sdk.FunPlusRUM";
    private static final String EXTRA_PROPERTIES_FILE = "com.funplus.sdk.RUMExtraProperties";

    @NonNull private final FunPlusConfig funPlusConfig;
    @NonNull private final LogAgentClient logAgentClient;
    @NonNull private final RUMSampler sampler;
    @Nullable private NetworkInfo networkInfo;

    @NonNull private final Map<String, String> extraProperties = new HashMap<>();

    private final List<Pair<String, Date>> traceHistory = new ArrayList<>();
    private final List<Pair<String, Date>> suppressHistory = new ArrayList<>();

    FunPlusRUM(@NonNull FunPlusConfig funPlusConfig) {
        this.funPlusConfig = funPlusConfig;

        String endpoint = funPlusConfig.rumEndpoint;
        String tag = funPlusConfig.rumTag;
        String key = funPlusConfig.rumKey;
        long uploadInterval = funPlusConfig.rumUploadInterval;

        double sampleRate = funPlusConfig.rumSampleRate;
        List<String> eventWhiteList = funPlusConfig.rumEventWhitelist;
        List<String> userWhiteList = funPlusConfig.rumUserWhitelist;
        List<String> userBlackList = funPlusConfig.rumUserBlacklist;

        DeviceInfo deviceInfo = FunPlusFactory.getDeviceInfo(funPlusConfig.context);

        this.logAgentClient = new LogAgentClient(funPlusConfig, LOGGER_LABEL, endpoint, tag, key, new LogAgentClient.ProgressHandler() {
            @Override
            public void onProgress(boolean status, int total, int uploaded) {
                Log.i(LOG_TAG, String.format(Locale.US, "Uploading RUM events in progress: {total=%d, uploaded=%d}", total, uploaded));
            }
        }, uploadInterval);

        this.sampler = new RUMSampler(deviceInfo.androidId, sampleRate, eventWhiteList, userWhiteList, userBlackList);

        ConnectivityManager cm = (ConnectivityManager) funPlusConfig.context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            networkInfo = cm.getActiveNetworkInfo();
        }

        HashMap cachedExtraProperties = ObjectReaderWriter.readObject(funPlusConfig, EXTRA_PROPERTIES_FILE, "ExtraProperties", HashMap.class);
        if (cachedExtraProperties != null) {
            for (Object k : cachedExtraProperties.keySet()) {
                extraProperties.put((String) k, (String) cachedExtraProperties.get(k));
            }

            Log.i(LOG_TAG, "RUM extra properties: " + cachedExtraProperties);
        }

        FunPlusFactory.getActivityLifecycleCallbacksProxy().add(this);
        getLogger().i("FunPlusRUM ready to work");
    }

    private void trace(@NonNull JSONObject event) {
        if (sampler.shouldSendEvent(event)) {
            logAgentClient.trace(event.toString());
            getLogger().i("Trace RUM event: " + event);

            if (BuildConfig.DEBUG) {
                traceHistory.add(new Pair<>(event.toString(), new Date()));
            }
        } else {
            getLogger().i("Suppress RUM event: " + event);

            if (BuildConfig.DEBUG) {
                suppressHistory.add(new Pair<>(event.toString(), new Date()));
            }
        }
    }

    @Override
    public void traceAppBackground() {
        try {
            trace(buildRUMEvent("app_background"));
        } catch (JSONException e) {
            getLogger().e("Error tracing app background event: %s", e.getMessage());
        }
    }

    @Override
    public void traceAppForeground() {
        try {
            trace(buildRUMEvent("app_foreground"));
        } catch (JSONException e) {
            getLogger().e("Error tracing app foreground event: %s", e.getMessage());
        }
    }

    @Override
    public void traceNetworkSwitch(@NonNull String sourceState,
                                   @NonNull String currentState) {
        try {
            JSONObject customProperties = new JSONObject();
            customProperties.put("source_state", sourceState);
            customProperties.put("current_state", currentState);

            trace(buildRUMEvent("network_switch", customProperties));
        } catch (JSONException e) {
            getLogger().e("Error tracing network switch event: %s", e.getMessage());
        }
    }
    @Override
    public void traceServiceMonitoring(@NonNull String serviceName,
                                       @NonNull String httpUrl,
                                       String httpStatus,
                                       int requestSize,
                                       int responseSize,
                                       long httpLatency,
                                       long requestTs,
                                       long responseTs,
                                       @NonNull String requestId,
                                       @NonNull String targetUserId,
                                       @NonNull String gameServerId) {
        try {
            JSONObject customProperties = new JSONObject();
            customProperties.put("service_name", serviceName);
            customProperties.put("http_url", httpUrl);
            customProperties.put("http_status", httpStatus);
            customProperties.put("request_size", requestSize);
            customProperties.put("response_size", responseSize);
            customProperties.put("http_latency", httpLatency);
            customProperties.put("request_ts", requestTs);
            customProperties.put("response_ts", responseTs);
            customProperties.put("req_id", requestId);
            customProperties.put("target_user_id", targetUserId);
            customProperties.put("game_server_id", gameServerId);
            customProperties.put("current_state", getNetworkType(networkInfo));

            trace(buildRUMEvent("service_monitoring", customProperties));
        } catch (JSONException e) {
            getLogger().e("Error tracing service monitoring event: %s", e.getMessage());
        }
    }

    @NonNull private JSONObject buildRUMEvent(@NonNull String eventName) throws JSONException {
        return buildRUMEvent(eventName, null);
    }

    @NonNull private JSONObject buildRUMEvent(@NonNull String eventName, @Nullable JSONObject customProperties) throws JSONException {
        ISessionManager sessionManager = FunPlusFactory.getSessionManager(funPlusConfig);
        DeviceInfo deviceInfo = FunPlusFactory.getDeviceInfo(funPlusConfig.context);

        JSONObject dict = new JSONObject();
        dict.put("event", eventName);
        dict.put("data_version", "1.0");
        dict.put("ts", "" + System.currentTimeMillis());
        dict.put("app_id", funPlusConfig.appId);
        dict.put("user_id", sessionManager.getUserId());
        dict.put("session_id", sessionManager.getSessionId());
        dict.put("rum_id", deviceInfo.androidId);

        // Common properties.
        JSONObject properties = new JSONObject();
        properties.put("app_version", deviceInfo.appVersion);
        properties.put("device", deviceInfo.deviceName);
        properties.put("os", deviceInfo.osName);
        properties.put("os_version", deviceInfo.osVersion);
        properties.put("carrier", getNetworkCarrierName(funPlusConfig.context));

        // Event properties.
        if (customProperties != null) {
            for (Iterator<String> iter = customProperties.keys(); iter.hasNext();) {
                String key = iter.next();
                properties.put(key, customProperties.get(key));
            }
        }

        // Extra properties.
        for (String key : extraProperties.keySet()) {
            properties.put(key, extraProperties.get(key));
        }

        dict.put("properties", properties);
        return dict;
    }

    private ILogger getLogger() {
        return FunPlusFactory.getLogger(funPlusConfig);
    }

    @Override
    public void setExtraProperty(@NonNull String key, @NonNull String value) {
        extraProperties.put(key, value);
        ObjectReaderWriter.writeObject(extraProperties, funPlusConfig, EXTRA_PROPERTIES_FILE, "RUMExtraProperties");
    }

    @Override
    public void eraseExtraProperty(@NonNull String key) {
        if (extraProperties.containsKey(key)) {
            extraProperties.remove(key);
            ObjectReaderWriter.writeObject(extraProperties, funPlusConfig, EXTRA_PROPERTIES_FILE, "RUMExtraProperties");
        }
    }

    @Override
    public void onConnectionChange(@Nullable NetworkInfo newNetworkInfo) {
        String sourceState = getNetworkType(networkInfo);
        String currentState = getNetworkType(newNetworkInfo);

        getLogger().i(String.format(Locale.US, "Connection status changed from %s to %s", sourceState, currentState));

        traceNetworkSwitch(sourceState, currentState);
        networkInfo = newNetworkInfo;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
        traceAppForeground();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        traceAppBackground();
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }

    @NonNull List<Pair<String, Date>> getSuppressHistory() {
        return suppressHistory;
    }

    @NonNull List<Pair<String, Date>> getTraceHistory() {
        return traceHistory;
    }

    @Nullable NetworkInfo getNetworkInfo() {
        return networkInfo;
    }

    @NonNull private static String getNetworkType(@Nullable NetworkInfo networkInfo) {
        if (networkInfo == null) {
            return "NONE";
        }

        switch (networkInfo.getType()) {
            case ConnectivityManager.TYPE_MOBILE:
                return "MOBILE";
            case ConnectivityManager.TYPE_WIFI:
                return "WIFI";
            case ConnectivityManager.TYPE_BLUETOOTH:
                return "BLUETOOTH";
            case ConnectivityManager.TYPE_DUMMY:
                return "DUMMY";
            case ConnectivityManager.TYPE_VPN:
                return "VPN";
            case ConnectivityManager.TYPE_WIMAX:
                return "WIMAX";
            case ConnectivityManager.TYPE_ETHERNET:
                return "ETHERNET";
            default:
                return "UNKNOWN";
        }
    }

    @NonNull private static String getNetworkCarrierName(@NonNull Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String carrierName = manager.getNetworkOperatorName();
        return carrierName == null || carrierName.isEmpty() ? "UNKNOWN" : carrierName;
    }

}
