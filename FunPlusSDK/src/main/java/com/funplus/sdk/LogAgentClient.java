package com.funplus.sdk;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * The <code>LogAgentClient</code> class is used to cache logs and timely upload them.
 */
class LogAgentClient extends HandlerThread implements Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "LogAgentClient";

    private static final int MAX_QUEUE_SIZE = 2000;
    private static final long DEFAULT_UPLOAD_INTERVAL = 30;     // 30 seconds

    interface ProgressHandler {
        /**
         * This method will be called in the uploading progress.
         *
         * @param status    The status of this uploading process.
         * @param total     The total count of logs.
         * @param uploaded  The count of logs uploaded. If status is <code>true</code>,
         *                  this value should equal to the <code>total</code> value.
         */
        void onProgress(boolean status, int total, int uploaded);
    }

    @NonNull private final FunPlusConfig funPlusConfig;
    @NonNull private final LogAgentDataUploader uploader;
    @NonNull private final List<String> dataQueue;
    @NonNull private final Handler internalHandler;
    @NonNull private final String archiveFilePath;
    @NonNull private final ProgressHandler progressHandler;
    private boolean isUploading = false;

    @Nullable private Timer timer;
    private long uploadInterval;

    /**
     * Constructor.
     *
     * @param funPlusConfig     The config object.
     * @param label             A globally unique label.
     * @param endpoint          The endpoint of Log Agent.
     * @param tag               The Log Agent tag.
     * @param key               The Log Agent key.
     * @param progressHandler   Progress callback.
     * @param uploadInterval    The uploading interval.
     */
    LogAgentClient(@NonNull FunPlusConfig funPlusConfig,
                   @NonNull String label,
                   @NonNull String endpoint,
                   @NonNull String tag,
                   @NonNull String key,
                   @NonNull ProgressHandler progressHandler,
                   long uploadInterval) {
        super(label, MIN_PRIORITY);
        setDaemon(true);
        start();

        this.funPlusConfig = funPlusConfig;
        this.uploader = new LogAgentDataUploader(funPlusConfig, endpoint, tag, key);
        this.progressHandler = progressHandler;
        this.uploadInterval = uploadInterval;

        internalHandler = new Handler(getLooper());
        archiveFilePath = String.format(Locale.US, "logger-archive-%s.data", label);

        // Unarchive local stored data.
        @SuppressWarnings("unchecked")
        List<String> cachedData = ObjectReaderWriter.readObject(funPlusConfig, archiveFilePath, label + "-CacheData", (Class<List<String>>)((Class)List.class));
        dataQueue = cachedData == null ? new ArrayList<String>() : cachedData;

        // Clear local stored data.
        ObjectReaderWriter.writeObject(new ArrayList<String>(), funPlusConfig, archiveFilePath, label + "-CacheData");

        this.uploadInterval = uploadInterval;

        FunPlusFactory.getActivityLifecycleCallbacksProxy().add(this);
        startTimer();
    }

    /**
     * Constructor.
     *
     * @param funPlusConfig     The config object.
     * @param label             A globally unique label.
     * @param endpoint          The endpoint of Log Agent.
     * @param tag               The Log Agent tag.
     * @param key               The Log Agent key.
     * @param progressHandler   Progress callback.
     */
    LogAgentClient(@NonNull FunPlusConfig funPlusConfig,
                   @NonNull String label,
                   @NonNull String endpoint,
                   @NonNull String tag,
                   @NonNull String key,
                   @NonNull ProgressHandler progressHandler) {
        this(funPlusConfig, label, endpoint, tag, key, progressHandler, DEFAULT_UPLOAD_INTERVAL);
    }

    /**
     * Trace an entry.
     *
     * @param entry     The entry to be traced.
     */
    void trace(@Nullable final String entry) {
        if (entry == null) {
            Log.e(LOG_TAG, "Failed to trace: log entry must not be null");
            return;
        }

        internalHandler.post(new Runnable() {
            @Override
            public void run() {
                if (dataQueue.size() > MAX_QUEUE_SIZE) {
                    dataQueue.remove(0);
                }
                dataQueue.add(entry);
            }
        });
    }

    /**
     * Trace a list of entries.
     *
     * @param entries   The entry list to be traced.
     */
    void trace(@NonNull List<String> entries) {
        for (String entry : entries) {
            trace(entry);
        }
    }

    private void upload() {
//        if (isOffline()) {
//            getLogger().i("Unable to upload, no connection");
//            return;
//        }

        internalHandler.post(new Runnable() {
            @Override
            public void run() {
                if (isUploading || dataQueue.size() == 0) {
                    return;
                }

                isUploading = true;

                uploader.upload(new ArrayList<>(dataQueue), new LogAgentDataUploader.CompletionHandler() {
                    @Override
                    public void onComplete(final boolean status, final int total, final int uploaded) {
                        internalHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                dataQueue.subList(0, uploaded).clear();
                                progressHandler.onProgress(status, total, uploaded);
                                isUploading = false;
                            }
                        });
                    }
                });
            }
        });
    }

    private void archive() {
        ObjectReaderWriter.writeObject(dataQueue, funPlusConfig, archiveFilePath, "dataQueue");
    }

    private void startTimer() {
        if (timer == null && uploadInterval > 0) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    upload();
                }
            }, 0, uploadInterval * 1000);
        }
    }

    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @NonNull
    List<String> getDataQueue() {
        return dataQueue;
    }

    // TODO
    private boolean isOffline() {
        ConnectivityManager cm = (ConnectivityManager) funPlusConfig.context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork == null || activeNetwork.isConnectedOrConnecting();

    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        archive();
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }
}
