package com.funplus.sdk;

interface ISessionManager {
    String getUserId();
    String getSessionId();

    void registerListener(SessionStatusChangeListener listener);

    void onUserIdChanged(String newUserId);
}
