package com.funplus.sdk;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * The <code>RUMSampler</code> class suppress specific events by the pre-defined rules.
 *
 * Here is the ordered rules list:
 *
 * <ul>
 *     <li>1. User blacklist: Suppress events with given user ID.</li>
 *     <li>2. User whitelist: Do not suppress events with given user ID.</li>
 *     <li>3. Event whitelist: Do not suppress events with given event type.</li>
 *     <li>4. A device unique value: Suppress all events if the value is greater than the sample rate.</li>
 * </ul>
 *
 */
class RUMSampler {

    private static final int COUNT_OF_BIT_ONE[] = {0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4};

    private final double deviceUniqueValue;
    private final double sampleRate;
    @NonNull final private List<String> eventWhiteList;
    @NonNull final private List<String> userWhiteList;
    @NonNull final private List<String> userBlackList;

    /**
     * Constructor of <code>RUMSampler</code>
     *
     * @param deviceId          The device ID.
     * @param sampleRate        The sampling rate.
     * @param eventWhiteList    The event whitelist.
     * @param userWhiteList     The user whitelist.
     * @param userBlackList     The user blacklist.
     */
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

    /**
     * Determine whether or not the given event should be sent.
     *
     * @param event     The given event in JSON format.
     * @return          True if event should be sent, otherwise false.
     */
    boolean shouldSendEvent(@NonNull JSONObject event) {
        try {
            String eventName = event.getString("event");
            String userId = event.getString("user_id");

            // Step 1: Check user blacklist.
            if (userBlackList.contains(userId)) {
                return false;
            }

            // Step 2: Check user whitelist.
            if (userWhiteList.contains(userId)) {
                return true;
            }

            // Step 3: Check event whitelist.
            if (eventWhiteList.contains(eventName)) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Step 4: Check device unique value.
        return deviceUniqueValue <= sampleRate;
    }

    /**
     * Calculate the device unique value by using given hash string.
     *
     * @param hash      The given hash string.
     * @return          The device unique value.
     */
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
