package com.funplus.sdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LogAgentDataUploaderTest {

    private static final long TIMEOUT = 30;
    private static final String ENDPOINT = "https://logagent.infra.funplus.net/log";
    private static final String TAG = "test";
    private static final String KEY = "funplus";

    private FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();

    @Test
    public void testUpload() throws Exception {
        // Given
        LogAgentDataUploader uploader = new LogAgentDataUploader(funPlusConfig, ENDPOINT, TAG, KEY);
        final CountDownLatch latch = new CountDownLatch(1);
        int testCount = 512;
        List<String> data = new ArrayList<>();

        for (int i = 0; i < testCount; i++) {
            data.add("message_" + i);
        }

        // When
        final List<String> result = new ArrayList<>();
        uploader.upload(data, new LogAgentDataUploader.CompletionHandler() {
            @Override
            public void onComplete(boolean status, int total, int uploaded) {
                result.add(status + "");
                result.add(total + "");
                result.add(uploaded + "");
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);

        // Then
        assertEquals(3, result.size());
        assertEquals(true + "", result.get(0));
        assertEquals(testCount + "", result.get(1));
        assertEquals(testCount + "", result.get(2));
    }

    @Test
    public void testUploadGreatAmount() throws Exception {
        // Given
        LogAgentDataUploader uploader = new LogAgentDataUploader(funPlusConfig, ENDPOINT, TAG, KEY);
        final CountDownLatch latch = new CountDownLatch(1);
        int testCount = 8192;
        List<String> data = new ArrayList<>();

        for (int i = 0; i < testCount; i++) {
            data.add("message_" + i);
        }

        // When
        final List<String> result = new ArrayList<>();
        uploader.upload(data, new LogAgentDataUploader.CompletionHandler() {
            @Override
            public void onComplete(boolean status, int total, int uploaded) {
                result.add(status + "");
                result.add(total + "");
                result.add(uploaded + "");
                latch.countDown();
            }
        });
        latch.await(TIMEOUT * 2, TimeUnit.SECONDS);

        // Then
        assertEquals(3, result.size());
        assertEquals(true + "", result.get(0));
        assertEquals(testCount + "", result.get(1));
        assertEquals(testCount + "", result.get(2));
    }

    @Test
    public void testBadDataUploader() throws Exception {
        // Given
        LogAgentDataUploader uploader = new LogAgentDataUploader(funPlusConfig, ENDPOINT, TAG, "badkey");
        final CountDownLatch latch = new CountDownLatch(1);
        List<String> data = new ArrayList<>();
        data.add("test_log_1");
        data.add("test_log_2");

        // When
        final List<String> result = new ArrayList<>();
        uploader.upload(data, new LogAgentDataUploader.CompletionHandler() {
            @Override
            public void onComplete(boolean status, int total, int uploaded) {
                result.add(status + "");
                result.add(total + "");
                result.add(uploaded + "");
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);

        // Then
        assertEquals(3, result.size());
        assertEquals(false + "", result.get(0));
        assertEquals(2 + "", result.get(1));
        assertEquals(0 + "", result.get(2));
    }
}
