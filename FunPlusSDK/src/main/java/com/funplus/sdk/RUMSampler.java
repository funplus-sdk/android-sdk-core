package com.funplus.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

class RUMSampler {

    private static final int COUNT_OF_BIT_ONE[] = {0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4};

    private final double deviceUniqueValue;
    private final double sampleRate;
    @NonNull final private List<String> eventWhiteList;
    @NonNull final private List<String> userWhiteList;
    @NonNull final private List<String> userBlackList;

    RUMSampler(@NonNull String deviceId,
               double sampleRate,
               @NonNull List<String> eventWhiteList,
               @NonNull List<String> userWhiteList,
               @NonNull List<String> userBlackList) {
        this.deviceUniqueValue = calcDeviceUniqueValue(DeviceInfo.MD5(deviceId));
        this.sampleRate = sampleRate;
        this.eventWhiteList = eventWhiteList;
        this.userWhiteList = userWhiteList;
        this.userBlackList = userBlackList;
    }

    boolean shouldSendEvent(@NonNull JSONObject event) {
        try {
            String eventName = event.getString("event");
            String userId = event.getString("user_id");

            if (userBlackList.contains(userId)) {
                return false;
            }

            if (eventWhiteList.contains(eventName)) {
                return true;
            }

            if (userWhiteList.contains(userId)) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return deviceUniqueValue <= sampleRate;
    }

    static double calcDeviceUniqueValue(@Nullable String hash) {
        if (hash == null) {
            return 0;
        }

        int sum = 0;

        for (char c : hash.toLowerCase().toCharArray()) {
            if (c >= '0' && c <= '9') {
                sum += COUNT_OF_BIT_ONE[c - '0'];
            } else {
                sum += COUNT_OF_BIT_ONE[c - 'a' + 10];
            }
        }

        return sum / 128.0;
    }
}
