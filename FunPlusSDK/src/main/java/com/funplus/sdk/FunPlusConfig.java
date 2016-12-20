package com.funplus.sdk;

import android.content.Context;
import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FunPlusConfig {
    private static final String LOG_SERVER = "https://logagent.infra.funplus.net/log";

    @NonNull final Context context;
    @NonNull final String appId;
    @NonNull final String appKey;
    @NonNull final SDKEnvironment environment;
    @NonNull final String configEtag;

    @NonNull final String loggerEndpoint;
    @NonNull final String loggerTag;
    @NonNull final String loggerKey;
    @NonNull final LogLevel logLevel;
    long loggerUploadInterval;

    @NonNull final String rumEndpoint;
    @NonNull final String rumTag;
    @NonNull final String rumKey;
    long rumUploadInterval;

    double rumSampleRate;
    @NonNull List<String> rumEventWhitelist;
    @NonNull List<String> rumUserWhitelist;
    @NonNull List<String> rumUserBlacklist;

    @NonNull final String dataEndpoint;
    @NonNull final String dataTag;
    @NonNull final String dataKey;
    long dataUploadInterval;

    boolean dataAutoTraceSessionEvents;

    public FunPlusConfig(@NonNull Context context,
                  @NonNull String appId,
                  @NonNull String appKey,
                  @NonNull String rumTag,
                  @NonNull String rumKey,
                  @NonNull SDKEnvironment environment) {
        this.context = context;
        this.appId = appId;
        this.appKey = appKey;
        this.environment = environment;

        this.configEtag = "deprecated";

        this.loggerEndpoint = LOG_SERVER;
        this.loggerTag = rumTag;
        this.loggerKey = rumKey;
        this.logLevel = environment == SDKEnvironment.Sandbox ? LogLevel.INFO : LogLevel.ERROR;
        this.loggerUploadInterval = environment == SDKEnvironment.Sandbox ? 60 : 10 * 60;

        this.rumEndpoint = LOG_SERVER;
        this.rumTag = rumTag;
        this.rumKey = rumKey;
        this.rumUploadInterval = environment == SDKEnvironment.Sandbox ? 5 : 10;

        this.rumSampleRate = 1.0;
        this.rumEventWhitelist = new ArrayList<>();
        this.rumUserWhitelist = new ArrayList<>();
        this.rumUserBlacklist = new ArrayList<>();

        this.dataEndpoint = LOG_SERVER;
        this.dataTag = appId;
        this.dataKey = appKey;
        this.dataUploadInterval = environment == SDKEnvironment.Sandbox ? 5 : 10;

        dataAutoTraceSessionEvents = true;
    }

    @Deprecated
    FunPlusConfig(@NonNull Context context,
                  @NonNull String appId,
                  @NonNull String appKey,
                  @NonNull SDKEnvironment environment,
                  @NonNull String configEtag,
                  @NonNull JSONObject configDict) throws JSONException {
        this.context = context;
        this.appId = appId;
        this.appKey = appKey;
        this.environment = environment;
        this.configEtag = configEtag;

        loggerEndpoint = configDict.getString("logger_endpoint");
        loggerTag = configDict.getString("logger_tag");
        loggerKey = configDict.getString("logger_key");
        logLevel = LogLevel.factory(configDict.getString("logger_level"));
        loggerUploadInterval = configDict.has("logger_upload_interval") ? configDict.getLong("logger_upload_interval") : 60;

        rumEndpoint = configDict.getString("rum_endpoint");
        rumTag = configDict.getString("rum_tag");
        rumKey = configDict.getString("rum_key");
        rumUploadInterval = configDict.has("rum_upload_interval") ? configDict.getLong("rum_upload_interval") : 10;

        rumSampleRate = configDict.has("rum_sample_rate") ? configDict.getDouble("rum_sample_rate") : 1.0;

        rumEventWhitelist = new ArrayList<>();
        if (configDict.has("rum_event_whitelist")) {
            JSONArray eventWhitelistJson = configDict.getJSONArray("rum_event_whitelist");
            for (int i = 0; i < eventWhitelistJson.length(); i++) {
                rumEventWhitelist.add(eventWhitelistJson.getString(i));
            }
        }

        rumUserWhitelist = new ArrayList<>();
        if (configDict.has("rum_user_whitelist")) {
            JSONArray userWhitelistJson = configDict.getJSONArray("rum_user_whitelist");
            for (int i = 0; i < userWhitelistJson.length(); i++) {
                rumUserWhitelist.add(userWhitelistJson.getString(i));
            }
        }

        rumUserBlacklist = new ArrayList<>();
        if (configDict.has("rum_user_blacklist")) {
            JSONArray userBlacklistJson = configDict.getJSONArray("rum_user_blacklist");
            for (int i = 0; i < userBlacklistJson.length(); i++) {
                rumUserBlacklist.add(userBlacklistJson.getString(i));
            }
        }

        dataEndpoint = configDict.getString("data_endpoint");
        dataTag = configDict.getString("data_tag");
        dataKey = configDict.getString("data_key");
        dataUploadInterval = configDict.has("data_upload_interval") ? configDict.getLong("data_upload_interval") : 10;

        dataAutoTraceSessionEvents = true;
    }

    public FunPlusConfig setLoggerUploadInterval(long value) {
        loggerUploadInterval = value;
        return this;
    }

    public FunPlusConfig setRumUploadInterval(long value) {
        rumUploadInterval = value;
        return this;
    }

    public FunPlusConfig setRumSampleRate(double value) {
        rumSampleRate = value;
        return this;
    }

    public FunPlusConfig setRumEventWhitelist(@NonNull List<String> value) {
        rumEventWhitelist = value;
        return this;
    }

    public FunPlusConfig setRumUserWhitelist(@NonNull List<String> value) {
        rumUserWhitelist = value;
        return this;
    }

    public FunPlusConfig setRumUserBlacklist(@NonNull List<String> value) {
        rumUserBlacklist = value;
        return this;
    }

    public FunPlusConfig setDataUploadInterval(long value) {
        dataUploadInterval = value;
        return this;
    }

    public FunPlusConfig setDataAutoTraceSessionEvents(boolean value) {
        dataAutoTraceSessionEvents = value;
        return this;
    }

    /**
     * This method should be called at the end of the settings chain.
     * It is just a stub.
     */
    public void end() {
        // Do nothing
    }
}
