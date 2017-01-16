package com.funplus.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * SDK running environment.
 */
public enum SDKEnvironment {
    Sandbox,
    Production;

    /**
     * Construct an <code>SDKEnvironment</code> instance by using given string.
     *
     * @param rawValue  The given string.
     * @return          The <code>SDKEnvironment</code> instance, or null.
     */
    @Nullable public static SDKEnvironment construct(@NonNull String rawValue) {
        switch (rawValue.toLowerCase()) {
            case "sandbox":
                return SDKEnvironment.Sandbox;
            case "production":
                return SDKEnvironment.Production;
            default:
                return null;
        }
    }
}
