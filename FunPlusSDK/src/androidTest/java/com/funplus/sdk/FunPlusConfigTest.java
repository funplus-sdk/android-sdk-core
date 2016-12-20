package com.funplus.sdk;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FunPlusConfigTest {

    private static final String APP_ID = "test";
    private static final String APP_KEY = "funplus";
    private static final String RUM_TAG = "test";
    private static final String RUM_KEY = "funplus";
    private static final SDKEnvironment ENV = SDKEnvironment.Sandbox;

    private static final long DEFAULT_LOGGER_UPLOAD_INTERVAL = 60;
    private static final long DEFAULT_RUM_UPLOAD_INTERVAL = 5;
    private static final long DEFAULT_DATA_UPLOAD_INTERVAL = 5;
    private static final double DEFAULT_RUM_SAMPLE_RATE = 1.0;

    private Context context = InstrumentationRegistry.getContext();

    @Test
    public void testDefaultValuesForSandboxEnvironment() {
        // Given, When
        FunPlusConfig config = new FunPlusConfig(context, APP_ID, APP_KEY, RUM_TAG, RUM_KEY, ENV);

        // Then
        assertEquals(config.appId, APP_ID);
        assertEquals(config.appKey, APP_KEY);
        assertTrue(config.environment.equals(ENV));

        assertEquals(config.loggerTag, RUM_TAG);
        assertEquals(config.loggerKey, RUM_KEY);
        assertEquals(config.logLevel, LogLevel.INFO);
        assertEquals(config.loggerUploadInterval, DEFAULT_LOGGER_UPLOAD_INTERVAL);

        assertEquals(config.rumTag, RUM_TAG);
        assertEquals(config.rumKey, RUM_KEY);
        assertEquals(config.rumUploadInterval, DEFAULT_RUM_UPLOAD_INTERVAL);

        assertEquals(config.rumSampleRate, DEFAULT_RUM_SAMPLE_RATE, 0.001);
        assertTrue(config.rumEventWhitelist.isEmpty());
        assertTrue(config.rumUserWhitelist.isEmpty());
        assertTrue(config.rumUserBlacklist.isEmpty());

        assertEquals(config.dataTag, APP_ID);
        assertEquals(config.dataKey, APP_KEY);
        assertEquals(config.dataUploadInterval, DEFAULT_DATA_UPLOAD_INTERVAL);

        assertTrue(config.dataAutoTraceSessionEvents);
    }

    @Test
    public void testDefaultValuesForProductionEnvironment() {
        // Given, When
        FunPlusConfig config = new FunPlusConfig(context, APP_ID, APP_KEY, RUM_TAG, RUM_KEY, SDKEnvironment.Production);

        // Then
        assertEquals(config.appId, APP_ID);
        assertEquals(config.appKey, APP_KEY);
        assertTrue(config.environment.equals(SDKEnvironment.Production));

        assertEquals(config.loggerTag, RUM_TAG);
        assertEquals(config.loggerKey, RUM_KEY);
        assertEquals(config.logLevel, LogLevel.ERROR);
        assertEquals(config.loggerUploadInterval, 10 * 60);

        assertEquals(config.rumTag, RUM_TAG);
        assertEquals(config.rumKey, RUM_KEY);
        assertEquals(config.rumUploadInterval, 10);

        assertEquals(config.rumSampleRate, DEFAULT_RUM_SAMPLE_RATE, 0.001);
        assertTrue(config.rumEventWhitelist.isEmpty());
        assertTrue(config.rumUserWhitelist.isEmpty());
        assertTrue(config.rumUserBlacklist.isEmpty());

        assertEquals(config.dataTag, APP_ID);
        assertEquals(config.dataKey, APP_KEY);
        assertEquals(config.dataUploadInterval, 10);

        assertTrue(config.dataAutoTraceSessionEvents);
    }

    @Test
    public void testSettersChain() {
        // Given
        long loggerUploadInterval = 10;
        long rumUploadInterval = 4;
        long dataUploadInterval = 3;

        double rumSampleRate = 0.8;

        ArrayList<String> rumEventWhitelist = new ArrayList<>();
        rumEventWhitelist.add("level_up");
        rumEventWhitelist.add("money_gain");

        ArrayList<String> rumUserWhitelist = new ArrayList<>();
        rumUserWhitelist.add("user1");
        rumUserWhitelist.add("user2");
        rumUserWhitelist.add("user3");

        ArrayList<String> rumUserBlacklist = new ArrayList<>();
        rumUserBlacklist.add("user4");
        rumUserBlacklist.add("user5");

        FunPlusConfig config = new FunPlusConfig(context, APP_ID, APP_KEY, RUM_TAG, RUM_KEY, ENV);

        // When
        config.setLoggerUploadInterval(loggerUploadInterval)
                .setRumUploadInterval(rumUploadInterval)
                .setRumSampleRate(rumSampleRate)
                .setRumEventWhitelist(rumEventWhitelist)
                .setRumUserWhitelist(rumUserWhitelist)
                .setRumUserBlacklist(rumUserBlacklist)
                .setDataUploadInterval(dataUploadInterval)
                .setDataAutoTraceSessionEvents(false)
                .end();

        // Then
        assertEquals(config.appId, APP_ID);
        assertEquals(config.appKey, APP_KEY);
        assertTrue(config.environment.equals(ENV));

        assertEquals(config.loggerTag, RUM_TAG);
        assertEquals(config.loggerKey, RUM_KEY);
        assertEquals(config.logLevel, LogLevel.INFO);
        assertEquals(config.loggerUploadInterval, loggerUploadInterval);

        assertEquals(config.rumTag, RUM_TAG);
        assertEquals(config.rumKey, RUM_KEY);
        assertEquals(config.rumUploadInterval, rumUploadInterval);

        assertEquals(config.rumSampleRate, rumSampleRate, 0.001);
        assertEquals(config.rumEventWhitelist.size(), 2);
        assertEquals(config.rumUserWhitelist.size(), 3);
        assertEquals(config.rumUserBlacklist.size(), 2);

        assertEquals(config.dataTag, APP_ID);
        assertEquals(config.dataKey, APP_KEY);
        assertEquals(config.dataUploadInterval, dataUploadInterval);

        assertFalse(config.dataAutoTraceSessionEvents);
    }
}
