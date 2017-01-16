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

/**
 * We get shared instances using this helper class.
 */
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

    /**
     * Get or create the shared internal logger.
     *
     * @param funPlusConfig     The config object.
     * @return                  The shared internal logger.
     */
    @NonNull static synchronized ILogger getLogger(@NonNull FunPlusConfig funPlusConfig) {
        if (logger == null) {
            logger = new Logger(funPlusConfig);
        }
        return logger;
    }

    /**
     * Get or create the shared session manager.
     *
     * @param funPlusConfig     The config object.
     * @return                  The shared internal logger.
     */
    @NonNull static synchronized ISessionManager getSessionManager(@NonNull FunPlusConfig funPlusConfig) {
        if (sessionManager == null) {
            sessionManager = new SessionManager(funPlusConfig);
        }
        return sessionManager;
    }

    /**
     * Get or create the shared <code>FunPlusID</code> instance.
     * @param funPlusConfig     The config object.
     * @return                  The shared <code>FunPlusID</code> instance.
     */
    @NonNull static synchronized IFunPlusID getFunPlusID(@NonNull FunPlusConfig funPlusConfig) {
        if (funPlusID == null) {
            funPlusID = new FunPlusID(funPlusConfig);
        }
        return funPlusID;
    }

    /**
     * Get or create the shared <code>FunPlusRUM</code> instance.
     *
     * @param funPlusConfig     The config object.
     * @return                  The shared <code>FunPlusID</code> instance.
     */
    @NonNull static synchronized IFunPlusRUM getFunPlusRUM(@NonNull FunPlusConfig funPlusConfig) {
        if (funPlusRUM == null) {
            funPlusRUM = new FunPlusRUM(funPlusConfig);
        }
        return funPlusRUM;
    }

    /**
     * Get or create the shared <code>FunPlusData</code> instance.
     *
     * @param funPlusConfig     The config object.
     * @return                  The shared <code>FunPlusData</code> instance.
     */
    @NonNull static synchronized IFunPlusData getFunPlusData(@NonNull FunPlusConfig funPlusConfig) {
        if (funPlusData == null) {
            funPlusData = new FunPlusData(funPlusConfig);
        }
        return funPlusData;
    }

    /**
     * Get or create the shared request queue.
     *
     * @param context           The current context.
     * @return                  The shared request queue.
     */
    @NonNull static synchronized RequestQueue getRequestQueue(@NonNull Context context) {
        if (requestQueue == null) {
            Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024);
            Network network = new BasicNetwork(new HurlStack());
            requestQueue = new RequestQueue(cache, network);
            requestQueue.start();
        }
        return requestQueue;
    }

    /**
     * Get or create the shared <code>DeviceInfo</code> instance.
     *
     * @param context           The current context.
     * @return                  The shared <code>DeviceInfo</code> instance.
     */
    @NonNull static synchronized DeviceInfo getDeviceInfo(@NonNull Context context) {
        if (deviceInfo == null) {
            deviceInfo = new DeviceInfo(context);
        }
        return deviceInfo;
    }

    /**
     * Get the shared <code>LoggerDataConsumer</code> instance.
     *
     * @param funPlusConfig     The config object.
     * @return                  The shared <code>LoggerConsumer</code> instance.
     */
    @NonNull static synchronized LoggerDataConsumer getLoggerDataConsumer(@NonNull FunPlusConfig funPlusConfig) {
        if (loggerDataConsumer == null) {
            loggerDataConsumer = new LoggerDataConsumer(funPlusConfig);
        }
        return loggerDataConsumer;
    }

    /**
     * Get the shared <code>ActivityLifecycleCallbacksProxy</code> instance.
     *
     * @return                  The shared <code>ActivityLifecycleCallbacksProxy</code> instance.
     */
    @NonNull static synchronized ActivityLifecycleCallbacksProxy getActivityLifecycleCallbacksProxy() {
        if (activityLifecycleCallbacksProxy == null) {
            activityLifecycleCallbacksProxy = new ActivityLifecycleCallbacksProxy();
        }
        return activityLifecycleCallbacksProxy;
    }
}
