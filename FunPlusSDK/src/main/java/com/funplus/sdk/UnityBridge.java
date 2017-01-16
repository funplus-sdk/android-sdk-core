package com.funplus.sdk;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * The `UnityBridge` class is a wrapper layer that exposes native APIs to Unity layer.
 */

public class UnityBridge {

    private static final String LOG_TAG = "FunPlusUnityBridge";

    /**
     * Install the SDK.
     *
     * @param application   The Android application instance.
     * @param appId         The FunPlus App ID.
     * @param appKey        The FunPlus App key.
     * @param rumTag        The RUM tag.
     * @param rumKey        The RUM key.
     * @param environment   The environment, can only take "sandbox" or "production".
     */
    public static void install(@NonNull Application application,
                               @NonNull String appId,
                               @NonNull String appKey,
                               @NonNull String rumTag,
                               @NonNull String rumKey,
                               @NonNull String environment) {
        Log.i(LOG_TAG, "FunPlusSDK Installing......");
        SDKEnvironment env = SDKEnvironment.construct(environment);
        if (env == null) {
            Log.e(LOG_TAG, "Cannot resolve the `environment` parameter");
            return;
        }

        FunPlusSDK.install(application, appId, appKey, rumTag, rumKey, env);
        FunPlusSDK.registerActivityLifecycleCallbacks(application);
    }

    /**
     * Install the SDK with a full list of parameters, in order to override default
     * configuration values.
     *
     * @param application                   The Android application instance.
     * @param appId                         The FunPlus App ID.
     * @param appKey                        The FunPlus App key.
     * @param rumTag                        The RUM tag.
     * @param rumKey                        The RUM key.
     * @param environment                   The environment, can only take "sandbox" or "production".
     * @param loggerUploadInterval          Upload interval for SDK internal logger.
     * @param rumUploadInterval             Upload interval for RUM.
     * @param rumSampleRate                 Sampling rate for RUM.
     * @param rumEventWhitelistString       Event whitelist for RUM.
     * @param rumUserWhitelistString        User whitelist for RUM.
     * @param rumUserBlacklistString        User blacklist for RUM.
     * @param dataUploadInterval            Upload interval for Data.
     * @param dataAutoTraceSessionEvents    Whether to auto trace session events or not.
     */
    public static void install(@NonNull Application application,
                               @NonNull String appId,
                               @NonNull String appKey,
                               @NonNull String rumTag,
                               @NonNull String rumKey,
                               @NonNull String environment,
                               long loggerUploadInterval,
                               long rumUploadInterval,
                               double rumSampleRate,
                               @NonNull String rumEventWhitelistString,
                               @NonNull String rumUserWhitelistString,
                               @NonNull String rumUserBlacklistString,
                               long dataUploadInterval,
                               boolean dataAutoTraceSessionEvents) {
        Log.i(LOG_TAG, "FunPlusSDK Installing......");
        SDKEnvironment env = SDKEnvironment.construct(environment);
        if (env == null) {
            Log.e(LOG_TAG, "Cannot resolve the `environment` parameter");
            return;
        }

        try {
            JSONArray rumEventWhitelistJsonArray = new JSONArray(rumEventWhitelistString);
            JSONArray rumUserWhitelistJsonArray = new JSONArray(rumUserWhitelistString);
            JSONArray rumUserBlacklistJsonArray = new JSONArray(rumUserBlacklistString);

            ArrayList<String> rumEventWhitelist = new ArrayList<>();
            for (int i = 0; i < rumEventWhitelistJsonArray.length(); i++) {
                rumEventWhitelist.add(rumEventWhitelistJsonArray.getString(i));
            }

            ArrayList<String> rumUserWhitelist = new ArrayList<>();
            for (int i = 0; i < rumUserWhitelistJsonArray.length(); i++) {
                rumUserWhitelist.add(rumUserWhitelistJsonArray.getString(i));
            }

            ArrayList<String> rumUserBlacklist = new ArrayList<>();
            for (int i = 0; i < rumUserBlacklistJsonArray.length(); i++) {
                rumUserBlacklist.add(rumUserBlacklistJsonArray.getString(i));
            }

            FunPlusConfig funPlusConfig = new FunPlusConfig(application, appId, appKey, rumTag, rumKey, env);

            funPlusConfig.setLoggerUploadInterval(loggerUploadInterval)
                    .setRumUploadInterval(rumUploadInterval)
                    .setRumSampleRate(rumSampleRate)
                    .setRumEventWhitelist(rumEventWhitelist)
                    .setRumUserWhitelist(rumUserWhitelist)
                    .setRumUserBlacklist(rumUserBlacklist)
                    .setDataUploadInterval(dataUploadInterval)
                    .setDataAutoTraceSessionEvents(dataAutoTraceSessionEvents)
                    .end();

            FunPlusSDK.install(funPlusConfig);
            FunPlusSDK.registerActivityLifecycleCallbacks(application);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Invalid parameter(s)");
        }
    }

    /**
     * Get an FPID using given external ID.
     *
     * @param externalID            The external ID to be associated with.
     * @param externalIDTypeString  Type of the external ID.
     */
    public static void getFPID(@NonNull String externalID,
                               @NonNull String externalIDTypeString) {
        FunPlusID.ExternalIDType externalIDType = FunPlusID.ExternalIDType.construct(externalIDTypeString);
        FunPlusSDK.getFunPlusID().get(externalID, externalIDType, new FunPlusID.FunPlusIDHandler() {
            @Override
            public void onSuccess(String fpid) {
                sendUnityMessage("onGetFPIDSuccess", fpid);
            }

            @Override
            public void onFailure(FunPlusID.Error error) {
                sendUnityMessage("onGetFPIDFailure", error.toString());
            }
        });
    }

    /**
     * Bind the FPID to another external ID.
     *
     * @param fpid                  The given FPID.
     * @param externalID            The external ID to be associated with.
     * @param externalIDTypeString  Type of the external ID.
     */
    public static void bindFPID(@NonNull String fpid,
                                @NonNull String externalID,
                                @NonNull String externalIDTypeString) {
        FunPlusID.ExternalIDType externalIDType = FunPlusID.ExternalIDType.construct(externalIDTypeString);
        FunPlusSDK.getFunPlusID().bind(fpid, externalID, externalIDType, new FunPlusID.FunPlusIDHandler() {
            @Override
            public void onSuccess(String fpid) {
                sendUnityMessage("onBindFPIDSuccess", fpid);
            }

            @Override
            public void onFailure(FunPlusID.Error error) {
                sendUnityMessage("onBindFPIDFailure", error.toString());
            }
        });
    }

    /**
     * Trace a RUM `service_monitoring` event.
     *
     * @param serviceName       Name of the service.
     * @param httpUrl           Requesting URL of the service.
     * @param httpStatus        The request status (not HTTP response status code).
     * @param requestSize       Size of the request body.
     * @param responseSize      Size of the response body.
     * @param httpLatency       The request duration (in milliseconds).
     * @param requestTs         Timestamp when the request is started.
     * @param responseTs        Timestamp when the response is fully received.
     * @param requestId         Identifier of current request.
     * @param targetUserId      The target user ID, see RUM spec.
     * @param gameServerId      The game server ID, see RUM spec.
     */
    public static void traceRUMServiceMonitoring(@NonNull String serviceName,
                                                 @NonNull String httpUrl,
                                                 @NonNull String httpStatus,
                                                 int requestSize,
                                                 int responseSize,
                                                 long httpLatency,
                                                 long requestTs,
                                                 long responseTs,
                                                 @NonNull String requestId,
                                                 @NonNull String targetUserId,
                                                 @NonNull String gameServerId) {
        FunPlusSDK.getFunPlusRUM().traceServiceMonitoring(
                serviceName,
                httpUrl,
                httpStatus,
                requestSize,
                responseSize,
                httpLatency,
                requestTs,
                responseTs,
                requestId,
                targetUserId,
                gameServerId
        );
    }

    /**
     * Set extra property for RUM events.
     *
     * @param key       The property key.
     * @param value     The property value.
     */
    public static void setRUMExtraProperty(@NonNull String key, @NonNull String value) {
        FunPlusSDK.getFunPlusRUM().setExtraProperty(key, value);
    }

    /**
     * Erase an extra property.
     *
     * @param key       Key of the property to be erased.
     */
    public static void eraseRUMExtraProperty(@NonNull String key) {
        FunPlusSDK.getFunPlusRUM().eraseExtraProperty(key);
    }

    /**
     * Trace a Data custom event.
     *
     * @param eventString   The event string, see Data spec.
     */
    public static void traceDataCustom(String eventString) {
        try {
            JSONObject event = new JSONObject(eventString);
            FunPlusSDK.getFunPlusData().traceCustom(event);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Invalid custom event string, error: " + e.getMessage());
        }
    }

    /**
     * Trace a Data custom event.
     *
     * @param eventName         The event name.
     * @param propertiesString  The event's property string.
     */
    public static void traceCustomEventWithNameAndProperties(@NonNull String eventName, @NonNull String propertiesString) {
        try {
            JSONObject properties = new JSONObject(propertiesString);
            FunPlusSDK.getFunPlusData().traceCustomEventWithNameAndProperties(eventName, properties);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Invalid event properties string, error: " + e.getMessage());
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
    public static void traceDataPayment(double amount,
                                        @NonNull String currency,
                                        @NonNull String productId,
                                        @NonNull String productName,
                                        @NonNull String productType,
                                        @NonNull String transactionId,
                                        @NonNull String paymentProcessor,
                                        @NonNull String itemsReceived,
                                        @NonNull String currencyReceived) {
        FunPlusSDK.getFunPlusData().tracePayment(
                amount,
                currency,
                productId,
                productName,
                productType,
                transactionId,
                paymentProcessor,
                itemsReceived,
                currencyReceived
        );
    }

    /**
     * Set extra property to Data events.
     *
     * @param key       The property key.
     * @param value     The property value.
     */
    public static void setDataExtraProperty(@NonNull String key, @NonNull String value) {
        FunPlusSDK.getFunPlusData().setExtraProperty(key, value);
    }

    /**
     * Erase an extra property.
     *
     * @param key       Key of the property to be erased.
     */
    public static void eraseDataExtraProperty(@NonNull String key) {
        FunPlusSDK.getFunPlusData().eraseExtraProperty(key);
    }

    private static void sendUnityMessage(@NonNull String event, @Nullable String message) {
        try {
            Class unityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer");
            @SuppressWarnings("unchecked")
            Method unitySendMessageMethod = unityPlayerClass.getMethod("UnitySendMessage", String.class, String.class, String.class);
            unitySendMessageMethod.invoke(null, "FunPlusEventListener", event, message);
        } catch (ClassNotFoundException e) {
            Log.e(LOG_TAG, "Could not find UnityPlayer class: " + e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e(LOG_TAG, "Could not find UnitySendMessage method: " + e.getMessage());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to invoke UnitySendMessage method: " + e.getMessage());
        }
    }
}
