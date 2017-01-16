package com.funplus.sdk;

import android.util.Log;

/**
 * Log levels.
 */
enum LogLevel {
    INFO(Log.INFO),
    WARN(Log.WARN),
    ERROR(Log.ERROR),
    FATAL(Log.ASSERT);

    private final int androidLogLevel;

    LogLevel(int androidLogLevel) {
        this.androidLogLevel = androidLogLevel;
    }

    public int getAndroidLogLevel() {
        return androidLogLevel;
    }

    /**
     * Construct a <code>LogLevel</code> instance by using given string.
     *
     * @param logLevelString    The given string.
     * @return                  The <code>LogLevel</code> instance, default is ERROR.
     */
    static LogLevel factory(String logLevelString) {
        switch (logLevelString) {
            case "info":
                return LogLevel.INFO;
            case "warn":
                return LogLevel.WARN;
            case "fatal":
                return LogLevel.FATAL;
            case "error":
            default:
                return LogLevel.ERROR;
        }
    }
}
