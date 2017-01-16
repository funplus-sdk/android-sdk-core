package com.funplus.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * See <link>http://wiki.ifunplus.cn/display/BI/Business+Intelligence+Specification+v2.0</link>
 */
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

    /**
     * Constructor.
     *
     * @param funPlusConfig     The config object.
     */
    FunPlusData(@NonNull FunPlusConfig funPlusConfig) {
        this.funPlusConfig = funPlusConfig;

        String endpoint = funPlusConfig.dataEndpoint;
        String tag = funPlusConfig.dataTag;
        String key = funPlusConfig.dataKey;
        long uploadInterval = funPlusConfig.dataUploadInterval;

        this.kpiLogAgentClient = new LogAgentClient(funPlusConfig, LOGGER_LABEL + ".core", endpoint, tag + ".core", key, new LogAgentClient.ProgressHandler() {
            @Override
            public void onProgress(boolean status, int total, int uploaded) {
                Log.i(LOG_TAG, String.format(Locale.US, "Uploading data KPI events in progress: {total=%d, uploaded=%d}", total, uploaded));
            }
        }, uploadInterval);

        this.customLogAgentClient = new LogAgentClient(funPlusConfig, LOGGER_LABEL + ".custom", endpoint, tag + ".custom", key, new LogAgentClient.ProgressHandler() {
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

        if (funPlusConfig.dataAutoTraceSessionEvents) {
            FunPlusFactory.getSessionManager(funPlusConfig).registerListener(this);
        }

        Log.i(LOG_TAG, "FunPlusData ready to work");
    }

    /**
     * Register a event listener.
     *
     * @param listener      The listener to be registered.
     */
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

    /**
     * Trace a `session_start` event.
     */
    @Override
    public void traceSessionStart() {
        try {
            trace(DataEventType.Kpi, buildDataEvent("session_start"));
        } catch (JSONException e) {
            getLogger().e("Error tracing session start event: %s", e.getMessage());
        }
    }

    /**
     * Trace a `session_end` event.
     *
     * @param sessionLength     Length of the ending session.
     */
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

    /**
     * Trace a `new_user` event.
     */
    @Override
    public void traceNewUser() {
        try {
            trace(DataEventType.Kpi, buildDataEvent("new_user"));
        } catch (JSONException e) {
            getLogger().e("Error tracing new user event: %s", e.getMessage());
        }
    }

    /**
     * Trace a custom event.
     *
     * @param event     The event to be traced.
     */
    @Override
    public void traceCustom(@NonNull JSONObject event) {
        trace(DataEventType.Custom, event);
    }

    @Override
    public void traceCustomEventWithNameAndProperties(@NonNull String eventName, @NonNull JSONObject properties) {
        try {
            trace(DataEventType.Custom, buildDataEvent(eventName, properties));
        } catch (JSONException e) {
            getLogger().e("Error tracing custom event: %s", e.getMessage());
        }
    }

    /**
     * Trace a Data payment event.
     *
     * @param amount                Numeric value which corresponds to the cost of the purchase
     *                              in the monetary unit multiplied by 100.
     * @param currency              The 3-letter ISO 4217 resource Code. See
     *                              <link>http://www.xe.com/iso4217.php</link>
     * @param productId             The ID of the product purchased.
     * @param productName           The name of the product purchased (optional).
     * @param productType           The type of the product purchased (optional).
     * @param transactionId         The unique transaction ID sent back by the payment processor.
     * @param paymentProcessor      The payment processor.
     * @param itemsReceived         A string of JSON array, consisting of one or more items
     *                              received (optional).
     * @param currencyReceived      A string of JSON array, consisting one or more types of
     *                              currency received (optional).
     */
    @Override
    public void tracePayment(double amount,
                             @NonNull String currency,
                             @NonNull String productId,
                             @Nullable String productName,
                             @Nullable String productType,
                             @NonNull String transactionId,
                             @NonNull String paymentProcessor,
                             @Nullable String itemsReceived,
                             @Nullable String currencyReceived) {
        productName = (productName == null) ? "" : productName;
        productType = (productType == null) ? "" : productType;

        itemsReceived = (itemsReceived == null || itemsReceived.isEmpty()) ? "[]" : itemsReceived;
        currencyReceived = (currencyReceived == null || currencyReceived.isEmpty()) ? "[]" : currencyReceived;

        // Init with empty JSON arrays.
        JSONArray cItemsReceived = new JSONArray();
        JSONArray cCurrencyReceived = new JSONArray();

        try {
            cItemsReceived = new JSONArray(itemsReceived);
        } catch (JSONException e) {
            getLogger().e("Error parsing parameter: `itemsReceived`");
        }

        try {
            cCurrencyReceived = new JSONArray(currencyReceived);
        } catch (JSONException e) {
            getLogger().e("Error parsing parameter: `currencyReceived`");
        }

        try {
            JSONObject customProperties = new JSONObject();
            customProperties.put("amount", amount);
            customProperties.put("currency", currency);
            customProperties.put("iap_product_id", productId);
            customProperties.put("iap_product_name", productName);
            customProperties.put("iap_product_type", productType);
            customProperties.put("transaction_id", transactionId);
            customProperties.put("payment_processor", paymentProcessor);
            customProperties.put("c_items_received", cItemsReceived);
            customProperties.put("c_currency_received", cCurrencyReceived);

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
        dict.put("data_version", "2.0");
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
        properties.put("install_ts", FunPlusSDK.getInstallTs() + "");

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

    /**
     * Set extra property to Data events.
     *
     * @param key       The property key.
     * @param value     The property value.
     */
    @Override
    public void setExtraProperty(@NonNull String key, @NonNull String value) {
        extraProperties.put(key, value);
        ObjectReaderWriter.writeObject(extraProperties, funPlusConfig, EXTRA_PROPERTIES_FILE, "DataExtraProperties");
    }

    /**
     * Erase an extra property.
     *
     * @param key       Key of the property to be erased.
     */
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
