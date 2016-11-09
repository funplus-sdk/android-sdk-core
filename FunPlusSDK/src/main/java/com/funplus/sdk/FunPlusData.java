package com.funplus.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class FunPlusData implements IFunPlusData, SessionStatusChangeListener {

    public enum DataEventType {
        Kpi,
        Custom,
    }

    public interface EventTracedListener {
        void onKpiEventTraced(JSONObject event);
        void onCustomEventTraced(JSONObject event);
    }

    private static final String LOG_TAG = "FunPlusData";
    private static final String LOGGER_LABEL = "com.funplus.sdk.FunPlusData";
    private static final String EXTRA_PROPERTIES_FILE = "com.funplus.sdk.DataExtraProperties";

    @NonNull private final FunPlusConfig funPlusConfig;
    @NonNull private final LogAgentClient kpiLogAgentClient;
    @NonNull private final LogAgentClient customLogAgentClient;

    @NonNull private final ArrayList<EventTracedListener> listeners = new ArrayList<>();

    @NonNull private final Map<String, String> extraProperties = new HashMap<>();

    private final List<Pair<String, Date>> kpiTraceHistory = new ArrayList<>();
    private final List<Pair<String, Date>> customTraceHistory = new ArrayList<>();

    FunPlusData(@NonNull FunPlusConfig funPlusConfig) {
        this.funPlusConfig = funPlusConfig;

        String endpoint = funPlusConfig.dataEndpoint;
        String tag = funPlusConfig.dataTag;
        String key = funPlusConfig.dataKey;
        long uploadInterval = funPlusConfig.dataUploadInterval;

        this.kpiLogAgentClient = new LogAgentClient(funPlusConfig, LOGGER_LABEL, endpoint, tag + ".core", key, new LogAgentClient.ProgressHandler() {
            @Override
            public void onProgress(boolean status, int total, int uploaded) {
                Log.i(LOG_TAG, String.format(Locale.US, "Uploading data KPI events in progress: {total=%d, uploaded=%d}", total, uploaded));
            }
        }, uploadInterval);

        this.customLogAgentClient = new LogAgentClient(funPlusConfig, LOGGER_LABEL, endpoint, tag + ".custom", key, new LogAgentClient.ProgressHandler() {
            @Override
            public void onProgress(boolean status, int total, int uploaded) {
                Log.i(LOG_TAG, String.format(Locale.US, "Uploading data custom events in progress: {total=%d, uploaded=%d}", total, uploaded));
            }
        }, uploadInterval);


        HashMap cachedExtraProperties = ObjectReaderWriter.readObject(funPlusConfig, EXTRA_PROPERTIES_FILE, "ExtraProperties", HashMap.class);
        if (cachedExtraProperties != null) {
            for (Object k : cachedExtraProperties.keySet()) {
                extraProperties.put((String) k, (String) cachedExtraProperties.get(k));
            }
        }

        FunPlusFactory.getSessionManager(funPlusConfig).registerListener(this);

        Log.i(LOG_TAG, "FunPlusData ready to work");
    }

    @Override
    public void registerEventTracedListener(@NonNull EventTracedListener listener) {
        listeners.add(listener);
    }

    private void trace(@NonNull DataEventType eventType, @NonNull JSONObject event) {
        switch (eventType) {
            case Kpi:
                kpiLogAgentClient.trace(event.toString());

                for (EventTracedListener listener : listeners) {
                    listener.onKpiEventTraced(event);
                }

                kpiTraceHistory.add(new Pair<>(event.toString(), new Date()));
                break;
            case Custom:
                customLogAgentClient.trace(event.toString());

                for (EventTracedListener listener : listeners) {
                    listener.onCustomEventTraced(event);
                }
                customTraceHistory.add(new Pair<>(event.toString(), new Date()));
                break;
        }

        getLogger().i("Trace Data event: " + event);
    }

    @Override
    public void traceCustom(@NonNull JSONObject event) {
        trace(DataEventType.Custom, event);
    }

    @Override
    public void traceSessionStart() {
        try {
            trace(DataEventType.Kpi, buildDataEvent("session_start"));
        } catch (JSONException e) {
            getLogger().e("Error tracing session start event: %s", e.getMessage());
        }
    }

    @Override
    public void traceSessionEnd(long sessionLength) {
        try {
            JSONObject customProperties = new JSONObject();
            customProperties.put("session_length", sessionLength);

            trace(DataEventType.Kpi, buildDataEvent("session_end", customProperties));
        } catch (JSONException e) {
            getLogger().e("Error tracing session end event: %s", e.getMessage());
        }
    }

    @Override
    public void traceNewUser() {
        try {
            trace(DataEventType.Kpi, buildDataEvent("new_user"));
        } catch (JSONException e) {
            getLogger().e("Error tracing new user event: %s", e.getMessage());
        }
    }

    @Override
    public void tracePayment(double amount,
                             @NonNull String currency,
                             @NonNull String productId,
                             @NonNull String productName,
                             @NonNull String productType,
                             @NonNull String transactionId,
                             @NonNull String paymentProcessor,
                             @NonNull String itemsReceived,
                             @NonNull String currencyReceived,
                             @NonNull String currencyReceivedType) {
        try {
            JSONObject customProperties = new JSONObject();
            customProperties.put("amount", amount);
            customProperties.put("currency", currency);
            customProperties.put("product_id", productId);
            customProperties.put("product_name", productName);
            customProperties.put("product_type", productType);
            customProperties.put("transaction_id", transactionId);
            customProperties.put("payment_processor", paymentProcessor);
            customProperties.put("c_items_received", itemsReceived);
            customProperties.put("m_currency_received", currencyReceived);
            customProperties.put("d_currency_received_type", currencyReceivedType);

            trace(DataEventType.Kpi, buildDataEvent("payment", customProperties));
        } catch (JSONException e) {
            getLogger().e("Error tracing payment event: %s", e.getMessage());
        }
    }

    @NonNull private JSONObject buildDataEvent(@NonNull String eventName) throws JSONException {
        return buildDataEvent(eventName, null);
    }

    @NonNull private JSONObject buildDataEvent(@NonNull String eventName, @Nullable JSONObject customProperties) throws JSONException {
        ISessionManager sessionManager = FunPlusFactory.getSessionManager(funPlusConfig);
        DeviceInfo deviceInfo = FunPlusFactory.getDeviceInfo(funPlusConfig.context);

        JSONObject dict = new JSONObject();
        dict.put("event", eventName);
        dict.put("data_version", "1.0");
        dict.put("ts", "" + System.currentTimeMillis());
        dict.put("app_id", funPlusConfig.appId);
        dict.put("user_id", sessionManager.getUserId());
        dict.put("session_id", sessionManager.getSessionId());

        // Common properties.
        JSONObject properties = new JSONObject();
        properties.put("android_id", deviceInfo.androidId);
        properties.put("app_version", deviceInfo.appVersion);
        properties.put("device", deviceInfo.deviceName);
        properties.put("os", deviceInfo.osName);
        properties.put("os_version", deviceInfo.osVersion);
        properties.put("lang", deviceInfo.language);

        // Event properties.
        if (customProperties != null) {
            for (Iterator<String> iterator = customProperties.keys(); iterator.hasNext();) {
                String key = iterator.next();
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
    public void onSessionEnded(@NonNull String userId,
                               @NonNull String sessionId,
                               long sessionStartTs,
                               long sessionLength) {
        traceSessionEnd(sessionLength);
    }

    @Override
    public void onSessionStarted(@NonNull String userId,
                                 @NonNull String sessionId,
                                 long sessionStartTs) {
        traceSessionStart();
    }

    @Override
    public void setExtraProperty(@NonNull String key, @NonNull String value) {
        extraProperties.put(key, value);
        ObjectReaderWriter.writeObject(extraProperties, funPlusConfig, EXTRA_PROPERTIES_FILE, "DataExtraProperties");
    }

    @Override
    public void eraseExtraProperty(@NonNull String key) {
        if (extraProperties.containsKey(key)) {
            extraProperties.remove(key);
            ObjectReaderWriter.writeObject(extraProperties, funPlusConfig, EXTRA_PROPERTIES_FILE, "DataExtraProperties");
        }
    }

    public List<Pair<String, Date>> getKpiTraceHistory() {
        return kpiTraceHistory;
    }

    public List<Pair<String, Date>> getCustomTraceHistory() {
        return customTraceHistory;
    }
}
