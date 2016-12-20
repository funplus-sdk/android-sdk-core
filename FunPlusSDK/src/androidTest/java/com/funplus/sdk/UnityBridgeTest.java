package com.funplus.sdk;

import android.app.Application;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class UnityBridgeTest {

    private static final String APP_ID = "test";
    private static final String APP_KEY = "funplus";
    private static final String RUM_TAG = "test";
    private static final String RUM_KEY = "funplus";
    private static final String ENV = "sandbox";

    @Test
    public void testInstallWithConfigValues() {
        // Given
        long loggerUploadInterval = 10;
        long rumUploadInterval = 5;
        double rumSampleRate = 0.8;
        String rumEventWhitelistString = "[\"level_up\", \"money_gain\"]";
        String rumUserWhitelistString = "[\"user1\", \"user2\", \"user3\"]";
        String rumUserBlacklistString = "[\"user4\", \"user5\"]";
        long dataUploadInterval = 6;
        boolean dataAutoTraceSessionEvents = false;

        Application application = (Application) InstrumentationRegistry.getContext().getApplicationContext();

        // When
        UnityBridge.install(
                application,
                APP_ID,
                APP_KEY,
                RUM_TAG,
                RUM_KEY,
                ENV,
                loggerUploadInterval,
                rumUploadInterval,
                rumSampleRate,
                rumEventWhitelistString,
                rumUserWhitelistString,
                rumUserBlacklistString,
                dataUploadInterval,
                dataAutoTraceSessionEvents
        );

        // Then
        FunPlusConfig config = FunPlusSDK.getFunPlusConfig();

        assertEquals(config.appId, APP_ID);
        assertEquals(config.appKey, APP_KEY);
        assertTrue(config.environment.equals(SDKEnvironment.Sandbox));

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
