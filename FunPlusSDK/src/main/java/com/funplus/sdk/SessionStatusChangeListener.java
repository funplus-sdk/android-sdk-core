package com.funplus.sdk;

/**
 * Created by yuankun on 13/10/2016.
 */

interface SessionStatusChangeListener {
    void onSessionStarted(String userId, String sessionId, long sessionStartTs);
    void onSessionEnded(String userId, String sessionId, long sessionStartTs, long sessionLength);
}
