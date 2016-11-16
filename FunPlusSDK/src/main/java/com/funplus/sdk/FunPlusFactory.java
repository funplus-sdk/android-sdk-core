package com.funplus.sdk;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;

class FunPlusFactory {
    @Nullable private static ILogger logger = null;
    @Nullable private static ISessionManager sessionManager = null;
    @Nullable private static IFunPlusID funPlusID = null;
    @Nullable private static IFunPlusRUM funPlusRUM = null;
    @Nullable private static IFunPlusData funPlusData = null;

    @Nullable private static RequestQueue requestQueue = null;
    @Nullable private static DeviceInfo deviceInfo = null;
    @Nullable private static LoggerDataConsumer loggerDataConsumer = null;
    @Nullable private static ActivityLifecycleCallbacksProxy activityLifecycleCallbacksProxy = null;

    @NonNull static ILogger getLogger(@NonNull FunPlusConfig funPlusConfig) {
        if (logger == null) {
            logger = new Logger(funPlusConfig);
        }
        return logger;
    }

    @NonNull static ISessionManager getSessionManager(@NonNull FunPlusConfig funPlusConfig) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(funPlusConfig);
        }
        return sessionManager;
    }

    @NonNull static IFunPlusID getFunPlusID(@NonNull FunPlusConfig funPlusConfig) {
        if (funPlusID == null) {
            funPlusID = new FunPlusID(funPlusConfig);
        }
        return funPlusID;
    }

    @NonNull static IFunPlusRUM getFunPlusRUM(@NonNull FunPlusConfig funPlusConfig) {
        if (funPlusRUM == null) {
            funPlusRUM = new FunPlusRUM(funPlusConfig);
        }
        return funPlusRUM;
    }

    @NonNull static IFunPlusData getFunPlusData(@NonNull FunPlusConfig funPlusConfig) {
        if (funPlusData == null) {
            funPlusData = new FunPlusData(funPlusConfig);
        }
        return funPlusData;
    }

    @NonNull static RequestQueue getRequestQueue(@NonNull Context context) {
        if (requestQueue == null) {
            Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            requestQueue = new RequestQueue(cache, network);
            requestQueue.start();
        }
        return requestQueue;
    }

    @NonNull static DeviceInfo getDeviceInfo(@NonNull Context context) {
        if (deviceInfo == null) {
            deviceInfo = new DeviceInfo(context);
        }
        return deviceInfo;
    }

    @NonNull static LoggerDataConsumer getLoggerDataConsumer(@NonNull FunPlusConfig funPlusConfig) {
        if (loggerDataConsumer == null) {
            loggerDataConsumer = new LoggerDataConsumer(funPlusConfig);
        }
        return loggerDataConsumer;
    }

    @NonNull static ActivityLifecycleCallbacksProxy getActivityLifecycleCallbacksProxy() {
        if (activityLifecycleCallbacksProxy == null) {
            activityLifecycleCallbacksProxy = new ActivityLifecycleCallbacksProxy();
        }
        return activityLifecycleCallbacksProxy;
    }
}
