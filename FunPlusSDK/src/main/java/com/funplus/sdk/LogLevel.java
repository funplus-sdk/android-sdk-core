package com.funplus.sdk;

import android.util.Log;

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
