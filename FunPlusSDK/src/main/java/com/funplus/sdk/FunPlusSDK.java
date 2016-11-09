package com.funplus.sdk;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;

import java.util.List;

public class FunPlusSDK {

    public static final String VERSION = "4.0.1-alpha";
    private static final String LOG_TAG = "FunPlusSDK";

    private static FunPlusSDK instance;
    @NonNull private final FunPlusConfig funPlusConfig;

    public static synchronized void install(@NonNull FunPlusConfig funPlusConfig){
        if (instance == null) {
            Log.i(LOG_TAG, String.format("Installing FunPlus SDK: {sdkVersion=%s, appId=%s, env=%s}", VERSION, funPlusConfig.appId, funPlusConfig.environment));
            instance = new FunPlusSDK(funPlusConfig);
        } else {
            Log.w(LOG_TAG, "FunPlus SDK has been installed, there's no need to install it again");
        }
    }

    public static synchronized void install(@NonNull Context context,
                                            @NonNull String appId,
                                            @NonNull String appKey,
                                            @NonNull SDKEnvironment environment) throws JSONException {
        if (instance == null) {
            FunPlusConfig funPlusConfig = new ConfigManager(context, appId, appKey, environment).getFunPlusConfig();
            install(funPlusConfig);
        } else {
            Log.w(LOG_TAG, "FunPlus SDK has been installed, there's no need to install it again");
        }
    }

    private FunPlusSDK(@NonNull FunPlusConfig funPlusConfig){
        this.funPlusConfig = funPlusConfig;
        FunPlusFactory.getLoggerDataConsumer(funPlusConfig);
        FunPlusFactory.getFunPlusID(funPlusConfig);
        FunPlusFactory.getFunPlusRUM(funPlusConfig);
        FunPlusFactory.getFunPlusData(funPlusConfig);
        FunPlusFactory.getFunPlusAdjust(funPlusConfig);
    }

    public static void registerActivityLifecycleCallbacks(@NonNull Application application) {
        ActivityLifecycleCallbacksProxy proxy = FunPlusFactory.getActivityLifecycleCallbacksProxy();
        List<Application.ActivityLifecycleCallbacks> instancesToRegister = proxy.getInstancesToRegister();

        for (Application.ActivityLifecycleCallbacks instance : instancesToRegister) {
            application.registerActivityLifecycleCallbacks(instance);
        }
    }

    @NonNull public static IFunPlusID getFunPlusID() {
        if (instance == null) {
            Log.e(LOG_TAG, "FunPlus SDK has not been installed yet.");
        }
        return FunPlusFactory.getFunPlusID(instance.funPlusConfig);
    }

    @NonNull public static IFunPlusRUM getFunPlusRUM() {
        if (instance == null) {
            Log.e(LOG_TAG, "FunPlus SDK has not been installed yet.");
        }
        return FunPlusFactory.getFunPlusRUM(instance.funPlusConfig);
    }

    @NonNull public static IFunPlusData getFunPlusData() {
        if (instance == null) {
            Log.e(LOG_TAG, "FunPlus SDK has not been installed yet.");
        }
        return FunPlusFactory.getFunPlusData(instance.funPlusConfig);
    }

    @NonNull public static IFunPlusAdjust getFunPlusAdjust() {
        if (instance == null) {
            Log.e(LOG_TAG, "FunPlus SDK has not been installed yet.");
        }
        return FunPlusFactory.getFunPlusAdjust(instance.funPlusConfig);
    }
}
