package com.funplus.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public enum SDKEnvironment {
    Sandbox,
    Production;

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
