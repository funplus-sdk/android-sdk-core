package com.funplus.sdk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yuankun on 08/10/2016.
 */
class FunPlusConfigFactory {

    private static final String APP_ID = "1007";
    private static final String APP_KEY = "FUNPLUS_GAME_KEY";
    private static final String CONFIG_ETAG = "";
    private static final SDKEnvironment ENV = SDKEnvironment.Sandbox;

    static FunPlusConfig defaultFunPlusConfig() {
        try {
            Context context = InstrumentationRegistry.getContext();
            JSONObject configDict = new JSONObject();

            configDict.put("logger_endpoint", "https://logagent.infra.funplus.net/log");
            configDict.put("logger_tag", "test");
            configDict.put("logger_key", "funplus");
            configDict.put("logger_level", "info");

            configDict.put("rum_endpoint", "https://logagent.infra.funplus.net/log");
            configDict.put("rum_tag", "test");
            configDict.put("rum_key", "funplus");

            configDict.put("data_endpoint", "https://logagent.infra.funplus.net/log");
            configDict.put("data_tag", "test");
            configDict.put("data_key", "funplus");

            configDict.put("adjust_app_token", "cchqrhzyr4zu");
            configDict.put("adjust_app_open_event_token", "st1hu7");

            return new FunPlusConfig(context, APP_ID, APP_KEY, ENV, CONFIG_ETAG, configDict);
        } catch (JSONException e) {
            return null;
        }
    }

    static FunPlusConfig rumSampleRateZeroConfig() {
        try {
            Context context = InstrumentationRegistry.getContext();
            JSONObject configDict = new JSONObject();

            configDict.put("logger_endpoint", "https://logagent.infra.funplus.net/log");
            configDict.put("logger_tag", "test");
            configDict.put("logger_key", "funplus");
            configDict.put("logger_level", "info");

            configDict.put("rum_endpoint", "https://logagent.infra.funplus.net/log");
            configDict.put("rum_tag", "test");
            configDict.put("rum_key", "funplus");
            configDict.put("rum_sample_rate", 0.0);

            configDict.put("data_endpoint", "https://logagent.infra.funplus.net/log");
            configDict.put("data_tag", "test");
            configDict.put("data_key", "funplus");

            configDict.put("adjust_app_token", "cchqrhzyr4zu");
            configDict.put("adjust_app_open_event_token", "st1hu7");

            return new FunPlusConfig(context, APP_ID, APP_KEY, ENV, CONFIG_ETAG, configDict);
        } catch (JSONException e) {
            return null;
        }
    }
}
