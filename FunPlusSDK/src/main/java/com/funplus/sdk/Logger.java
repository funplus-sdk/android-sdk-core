package com.funplus.sdk;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class Logger implements ILogger {
    private static final String LOG_TAG = "FunPlusSDK";

    @NonNull private final FunPlusConfig funPlusConfig;
    @NonNull private final LogLevel logLevel;
    @NonNull private final List<String> logs;

    Logger(@NonNull FunPlusConfig funPlusConfig) {
        this.funPlusConfig = funPlusConfig;
        this.logLevel = funPlusConfig.logLevel;
        this.logs = new ArrayList<>();
    }

    @Override
    public void i(@NonNull String message, Object... parameters) {
        if (logLevel.getAndroidLogLevel() <= Log.INFO) {
            String formattedMessage = String.format(Locale.US, message, parameters);
            Log.i(LOG_TAG, formattedMessage);
            trace("INFO", formattedMessage);
        }
    }

    @Override
    public void w(@NonNull String message, Object... parameters) {
        if (logLevel.getAndroidLogLevel() <= Log.WARN) {
            String formattedMessage = String.format(Locale.US, message, parameters);
            Log.d(LOG_TAG, formattedMessage);
            trace("WARN", formattedMessage);
        }
    }

    @Override
    public void e(@NonNull String message, Object... parameters) {
        if (logLevel.getAndroidLogLevel() <= Log.ERROR) {
            String formattedMessage = String.format(Locale.US, message, parameters);
            Log.e(LOG_TAG, formattedMessage);
            trace("ERROR", formattedMessage);
        }
    }

    @Override
    public void wtf(@NonNull String message, Object... parameters) {
        if (logLevel.getAndroidLogLevel() <= Log.ASSERT) {
            String formattedMessage = String.format(Locale.US, message, parameters);
            Log.wtf(LOG_TAG, formattedMessage);
            trace("FATAL", formattedMessage);
        }
    }

    @NonNull public synchronized List<String> consumeLogs() {
        List<String> l = new ArrayList<>(logs);
        logs.clear();
        return l;
    }

    void trace(@NonNull String logLevelString, String message) {
        List<StackTraceElement> stackTrace = Arrays.asList(Thread.currentThread().getStackTrace());

        if (stackTrace.size() < 4) {
            Log.e(LOG_TAG, "Seems the stack trace is not correct");
            return;
        }

        ArrayList<StackTraceElement> stackTraceElements = new ArrayList<>();
        stackTraceElements.addAll(stackTrace.subList(4, stackTrace.size()));

        StackTraceElement caller = stackTraceElements.get(0);
        String className = caller.getClassName();
        String methodName = caller.getMethodName();
        int line = caller.getLineNumber();

        String callStackSymbols = "";
        if (logLevelString.equals("ERROR") || logLevelString.equals("FATAL")) {
            callStackSymbols = TextUtils.join("\n", stackTraceElements);
        }

        String log = String.format(Locale.US, "[%s %s.%s():%d] %s", logLevelString, className, methodName, line, message);

        try {
            JSONObject entry = buildLogEntry(logLevelString, log, callStackSymbols);
            synchronized (this) {
                logs.add(entry.toString());
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Failed to build log entry");
        }
    }

    @NonNull private JSONObject buildLogEntry(@NonNull String logLevelString,
                                              @NonNull String log,
                                              @NonNull String callStackSymbols) throws JSONException {
        ISessionManager sessionManager = FunPlusFactory.getSessionManager(funPlusConfig);
        DeviceInfo deviceInfo = FunPlusFactory.getDeviceInfo(funPlusConfig.context);

        JSONObject dict = new JSONObject();
        dict.put("event", "log_entry");
        dict.put("ts", System.currentTimeMillis() + "");
        dict.put("app_id", funPlusConfig.appId);
        dict.put("user_id", sessionManager.getUserId());
        dict.put("session_id", sessionManager.getSessionId());
        dict.put("rum_id", deviceInfo.androidId);
        dict.put("data_version", "1.0");

        JSONObject properties = new JSONObject();
        properties.put("app_version", deviceInfo.appVersion);
        properties.put("sdk_version", FunPlusSDK.VERSION);
        properties.put("config_etag", funPlusConfig.configEtag);
        properties.put("device", deviceInfo.deviceName);
        properties.put("os", deviceInfo.osName);
        properties.put("os_version", deviceInfo.osVersion);
        properties.put("log", log);
        properties.put("log_level", logLevelString);
        properties.put("call_stack_symbols", callStackSymbols);

        dict.put("properties", properties);
        return dict;
    }
}
