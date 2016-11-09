package com.funplus.sdk;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class SessionManager implements ISessionManager, Application.ActivityLifecycleCallbacks {
    private static final String LOG_TAG = "SessionManager";

    @NonNull private final FunPlusConfig funPlusConfig;
    @NonNull private String userId = "";
    @NonNull private String sessionId = "";
    private long sessionStartTs;

    private final List<SessionStatusChangeListener> listeners = new ArrayList<>();

    SessionManager(@NonNull FunPlusConfig funPlusConfig) {
        this.funPlusConfig = funPlusConfig;
        this.userId = FunPlusFactory.getFunPlusID(funPlusConfig).getCurrentFPID();
        this.sessionStartTs = System.currentTimeMillis() / 1000;    // milliseconds -> seconds

        String appIdJoinUserId = String.format(Locale.US, "%-23s", funPlusConfig.appId + "-" + userId).replace(' ', '0');
        this.sessionId = String.format(Locale.US, "a%s%d", appIdJoinUserId, sessionStartTs);

        FunPlusFactory.getActivityLifecycleCallbacksProxy().add(this);
    }

    public void registerListener(SessionStatusChangeListener listener) {
        listeners.add(listener);
    }

    @Override
    @NonNull public String getUserId() {
        return userId;
    }

    @Override
    @NonNull public String getSessionId() {
        return sessionId;
    }

    private void startSession() {
        this.sessionStartTs = System.currentTimeMillis() / 1000;    // milliseconds -> seconds

        String appIdJoinUserId = String.format(Locale.US, "%-23s", funPlusConfig.appId + "-" + userId).replace(' ', '0');
        this.sessionId = String.format(Locale.US, "a%s%d", appIdJoinUserId, sessionStartTs);

        Log.i(LOG_TAG, String.format(Locale.US, "Start session: {userId=%s, sessionId=%s}", userId, sessionId));

        for (SessionStatusChangeListener listener : listeners) {
            listener.onSessionStarted(userId, sessionId, sessionStartTs);
        }
    }

    private void startSessionForNewUserId(@NonNull String userId) {
        this.userId = userId;
        this.sessionStartTs = System.currentTimeMillis() / 1000;    // milliseconds -> seconds

        String appIdJoinUserId = String.format(Locale.US, "%-23s", funPlusConfig.appId + "-" + userId).replace(' ', '0');
        this.sessionId = String.format(Locale.US, "a%s%d", appIdJoinUserId, sessionStartTs);

        Log.i(LOG_TAG, String.format(Locale.US, "Start session for new user ID: {userId=%s, sessionId=%s}", userId, sessionId));

        for (SessionStatusChangeListener listener : listeners) {
            listener.onSessionStarted(userId, sessionId, sessionStartTs);
        }
    }

    private void endSession() {
        if (sessionStartTs == 0) {
            Log.w(LOG_TAG, "Unable to end session: there's no active session");
            return;
        }

        long sessionLength = System.currentTimeMillis() / 1000 - sessionStartTs;
        sessionStartTs = 0;

        Log.i(LOG_TAG, String.format(Locale.US, "End session: {userId=%s, sessionId=%s, sessionLength=%d}", userId, sessionId, sessionLength));

        for (SessionStatusChangeListener listener : listeners) {
            listener.onSessionEnded(userId, sessionId, sessionStartTs, sessionLength);
        }
    }

    @Override
    public void onUserIdChanged(@NonNull String newUserId) {
        endSession();
        startSessionForNewUserId(newUserId);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        startSession();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        endSession();
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
