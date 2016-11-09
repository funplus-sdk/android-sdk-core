package com.funplus.sdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LogAgentClientTest {
    private static final String LABEL = "test-logger";
    private static final String ENDPOINT = "https://logagent.infra.funplus.net/log";
    private static final String TAG = "test";
    private static final String KEY = "funplus";

    private FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();

    @After
    public void tearDown() {
        String archiveFilePath = String.format(Locale.US, "logger-archive-%s.data", LABEL);
        ObjectReaderWriter.writeObject(new ArrayList<String>(), funPlusConfig, archiveFilePath, "dataQueue");
    }

    @Test
    public void testTraceWithBadUploader() throws InterruptedException {
        // Given
        int testCount = 512;
        LogAgentClient logger = new LogAgentClient(funPlusConfig, LABEL, ENDPOINT, TAG, "badkey", new LogAgentClient.ProgressHandler() {
            @Override
            public void onProgress(boolean status, int total, int uploaded) {

            }
        });

        // When
        for (int i = 0; i < testCount; i++) {
            logger.trace("message_" + i);
        }

        Thread.sleep(3000);

        // Then
        assertEquals(testCount, logger.getDataQueue().size());
    }

    @Test
    public void testTraceWithUploader() throws InterruptedException {
        // Given
        int testCount = 512;
        LogAgentClient logger = new LogAgentClient(funPlusConfig, LABEL, ENDPOINT, TAG, KEY, new LogAgentClient.ProgressHandler() {
            @Override
            public void onProgress(boolean status, int total, int uploaded) {

            }
        }, 2);

        // When
        for (int i = 0; i < testCount; i++) {
            logger.trace("message_" + i);
        }

        Thread.sleep(5000);

        // Then
        assertEquals(0, logger.getDataQueue().size());
    }

    @Test
    public void testTimedUpload() throws InterruptedException {
        // Given
        int testCount = 512;
        LogAgentClient logger = new LogAgentClient(funPlusConfig, LABEL, ENDPOINT, TAG, KEY, new LogAgentClient.ProgressHandler() {
            @Override
            public void onProgress(boolean status, int total, int uploaded) {

            }
        }, 2);

        // When
        for (int i = 0; i < testCount; i++) {
            logger.trace("message_" + i);
        }

        Thread.sleep(10000);

        // Then
        assertEquals(0, logger.getDataQueue().size());
    }

    @Test
    public void testUnarchive() throws InterruptedException {
        // Given
        int testCount = 512;
        List<String> data = new ArrayList<>();
        for (int i = 0; i < testCount; i++) {
            data.add("message" + i);
        }

        String archiveFilePath = String.format(Locale.US, "logger-archive-%s.data", LABEL);
        ObjectReaderWriter.writeObject(data, funPlusConfig, archiveFilePath, LABEL + "-CacheData");

        // When
        LogAgentClient logger = new LogAgentClient(funPlusConfig, LABEL, ENDPOINT, TAG, KEY, new LogAgentClient.ProgressHandler() {
            @Override
            public void onProgress(boolean status, int total, int uploaded) {

            }
        }, 0);

        Thread.sleep(3000);

        // Then

        assertEquals(testCount, logger.getDataQueue().size());
    }
}
