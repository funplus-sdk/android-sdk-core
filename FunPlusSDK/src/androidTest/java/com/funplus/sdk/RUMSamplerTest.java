package com.funplus.sdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class RUMSamplerTest {

    @Test
    public void testCalcDeviceUniqueValue() {
        // Given
        String deviceId1 = "0123456789abcdef";      // 32 ones
        double expectedValue1 = 32 / 128.0;
        String deviceId2 = "89abcdef";              // 20 ones
        double expectedValue2 = 20 / 128.0;
        String deviceId3 = "";                      // 0 ones
        double expectedValue3 = 0 / 128.0;

        // When
        double value1 = RUMSampler.calcDeviceUniqueValue(deviceId1);
        double value2 = RUMSampler.calcDeviceUniqueValue(deviceId2);
        double value3 = RUMSampler.calcDeviceUniqueValue(deviceId3);

        // Then
        assertEquals(expectedValue1, value1, 0.001);
        assertEquals(expectedValue2, value2, 0.001);
        assertEquals(expectedValue3, value3, 0.001);
    }

    @Test
    public void testSampleRateOne() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 1.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event");
        event.put("user_id", "user");

        // Then
        assertTrue(sampler.shouldSendEvent(event));
    }

    @Test
    public void testSampleRateZero() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 0.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event");
        event.put("user_id", "user");

        // Then
        assertFalse(sampler.shouldSendEvent(event));
    }

    @Test
    public void testSampleRateZeroEventInWhitelist() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 0.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();
        eventWhitelist.add("event_to_be_sent");

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event_to_be_sent");
        event.put("user_id", "user");

        // Then
        assertTrue(sampler.shouldSendEvent(event));
    }

    @Test
    public void testSampleRateOneEventInWhitelist() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 1.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();
        eventWhitelist.add("event_to_be_sent");

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event_to_be_sent");
        event.put("user_id", "user");

        // Then
        assertTrue(sampler.shouldSendEvent(event));
    }

    @Test
    public void testSampleRateZeroEventNotInWhitelist() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 0.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();
        eventWhitelist.add("event_to_be_sent");

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event_not_to_be_sent");
        event.put("user_id", "user");

        // Then
        assertFalse(sampler.shouldSendEvent(event));
    }

    @Test
    public void testSampleRateOneEventNotInWhitelist() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 1.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();
        eventWhitelist.add("event_to_be_sent");

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event_not_to_be_sent");
        event.put("user_id", "user");

        // Then
        assertTrue(sampler.shouldSendEvent(event));
    }

    @Test
    public void testSampleRateZeroUserInWhitelist() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 0.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();
        userWhitelist.add("user_allowed");

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event");
        event.put("user_id", "user_allowed");

        // Then
        assertTrue(sampler.shouldSendEvent(event));
    }

    @Test
    public void testSampleRateOneUserInWhitelist() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 0.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();
        userWhitelist.add("user_allowed");

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event");
        event.put("user_id", "user_allowed");

        // Then
        assertTrue(sampler.shouldSendEvent(event));
    }

    @Test
    public void testSampleRateOneUserInBlacklist() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 1.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();
        userBlacklist.add("user_not_allowed");

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event");
        event.put("user_id", "user_not_allowed");

        // Then
        assertFalse(sampler.shouldSendEvent(event));
    }

    @Test
    public void testSampleRateZeroUserInBlacklist() throws Exception {
        // Given
        String deviceId = "abcdefg";
        double sampleRate = 0.0;
        List<String> eventWhitelist = new ArrayList<>();
        List<String> userWhitelist = new ArrayList<>();
        List<String> userBlacklist = new ArrayList<>();
        userBlacklist.add("user_not_allowed");

        // When
        RUMSampler sampler = new RUMSampler(deviceId, sampleRate, eventWhitelist, userWhitelist, userBlacklist);
        JSONObject event = new JSONObject();
        event.put("event", "event");
        event.put("user_id", "user_not_allowed");

        // Then
        assertFalse(sampler.shouldSendEvent(event));
    }
}
