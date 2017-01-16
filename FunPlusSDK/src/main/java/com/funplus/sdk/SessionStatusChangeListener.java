package com.funplus.sdk;

/**
 * Classes adopting this interface will be notified when session status changes.
 */
interface SessionStatusChangeListener {

    /**
     * This method will be revoked when a session starts.
     *
     * @param userId            The user ID.
     * @param sessionId         The session ID.
     * @param sessionStartTs    The starting timestamp.
     */
    void onSessionStarted(String userId, String sessionId, long sessionStartTs);

    /**
     * This method will be revoked when a session ends.
     *
     * @param userId            The user ID.
     * @param sessionId         The session ID.
     * @param sessionStartTs    The starting timestamp.
     * @param sessionLength     Length of the dying session.
     */
    void onSessionEnded(String userId, String sessionId, long sessionStartTs, long sessionLength);
}
