package com.funplus.sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

class LoggerDataConsumer implements Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "LoggerDataConsumer";

    private static final String LOG_AGENT_LABEL = "com.funplus.sdk.Logger";
    private static final long CONSUME_INTERVAL = 60;    // 60 seconds

    @NonNull private final FunPlusConfig funPlusConfig;
    @NonNull private final LogAgentClient logAgentClient;

    @Nullable private Timer timer;

    LoggerDataConsumer(@NonNull FunPlusConfig funPlusConfig) {
        this.funPlusConfig = funPlusConfig;

        String endpoint = funPlusConfig.loggerEndpoint;
        String tag = funPlusConfig.loggerTag;
        String key = funPlusConfig.loggerKey;
        long uploadInterval = funPlusConfig.loggerUploadInterval;
        this.logAgentClient = new LogAgentClient(funPlusConfig, LOG_AGENT_LABEL, endpoint, tag, key, new LogAgentClient.ProgressHandler() {
            @Override
            public void onProgress(boolean status, int total, int uploaded) {
                Log.i(LOG_TAG, String.format(Locale.US, "Uploading logs in progress: {total=%d, uploaded=%d}", total, uploaded));
            }
        }, uploadInterval);

        startTimer();
    }

    void consume() {
        List<String> logsToBeConsumed = FunPlusFactory.getLogger(funPlusConfig).consumeLogs();

        if (logsToBeConsumed.size() > 0) {
            logAgentClient.trace(logsToBeConsumed);
            Log.i(LOG_TAG, String.format(Locale.US, "%d logs to be consumed", logsToBeConsumed.size()));
        }
    }

    private void startTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    consume();
                }
            }, CONSUME_INTERVAL * 1000, CONSUME_INTERVAL * 1000);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @NonNull LogAgentClient getLogAgentClient() {
        return logAgentClient;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        startTimer();
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        stopTimer();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
