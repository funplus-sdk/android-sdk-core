package com.funplus.sdk;

import android.net.NetworkInfo;

public interface IFunPlusRUM {
    void traceAppBackground();
    void traceAppForeground();
    void traceNetworkSwitch(String sourceState, String currentState);
    void traceServiceMonitoring(String serviceName,
                                String httpUrl,
                                String httpStatus,
                                int requestSize,
                                int responseSize,
                                long httpLatency,
                                long requestTs,
                                long responseTs,
                                String requestId,
                                String targetUserId,
                                String gameServerId);

    void setExtraProperty(String key, String value);
    void eraseExtraProperty(String key);

    void onConnectionChange(NetworkInfo networkInfo);
}
