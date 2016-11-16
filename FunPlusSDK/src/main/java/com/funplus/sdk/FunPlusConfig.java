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
    final long loggerUploadInterval;

    @NonNull final String rumEndpoint;
    @NonNull final String rumTag;
    @NonNull final String rumKey;
    final long rumUploadInterval;

    final double rumSampleRate;
    @NonNull final List<String> rumEventWhitelist;
    @NonNull final List<String> rumUserWhitelist;
    @NonNull final List<String> rumUserBlacklist;

    @NonNull final String dataEndpoint;
    @NonNull final String dataTag;
    @NonNull final String dataKey;
    final long dataUploadInterval;

    FunPlusConfig(@NonNull Context context,
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
        this.loggerUploadInterval = 120;

        this.rumEndpoint = LOG_SERVER;
        this.rumTag = rumTag;
        this.rumKey = rumKey;
        this.rumUploadInterval = 30;

        this.rumSampleRate = 1.0;
        this.rumEventWhitelist = new ArrayList<>();
        this.rumUserWhitelist = new ArrayList<>();
        this.rumUserBlacklist = new ArrayList<>();

        this.dataEndpoint = LOG_SERVER;
        this.dataTag = appId;
        this.dataKey = appKey;
        this.dataUploadInterval = 30;
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
    }
}
