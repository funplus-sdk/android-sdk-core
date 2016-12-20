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

    public static void setRUMExtraProperty(@NonNull String key, @NonNull String value) {
        FunPlusSDK.getFunPlusRUM().setExtraProperty(key, value);
    }

    public static void eraseRUMExtraProperty(@NonNull String key) {
        FunPlusSDK.getFunPlusRUM().eraseExtraProperty(key);
    }

    public static void traceDataCustom(String eventString) {
        try {
            JSONObject event = new JSONObject(eventString);
            FunPlusSDK.getFunPlusData().traceCustom(event);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Invalid custom event string, error: " + e.getMessage());
        }
    }

    public static void traceCustomEventWithNameAndProperties(@NonNull String eventName, @NonNull String propertiesString) {
        try {
            JSONObject properties = new JSONObject(propertiesString);
            FunPlusSDK.getFunPlusData().traceCustomEventWithNameAndProperties(eventName, properties);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(LOG_TAG, "Invalid event properties string, error: " + e.getMessage());
        }
    }

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

    public static void setDataExtraProperty(@NonNull String key, @NonNull String value) {
        FunPlusSDK.getFunPlusData().setExtraProperty(key, value);
    }

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
