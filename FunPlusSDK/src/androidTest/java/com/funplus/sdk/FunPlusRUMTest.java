package com.funplus.sdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class FunPlusRUMTest {

    private FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();

    @Test
    public void testTrace() {
        // Given
        FunPlusRUM tracer = new FunPlusRUM(funPlusConfig);

        // When
        tracer.traceAppBackground();
        tracer.traceAppForeground();

        // Then
        assertEquals(2, tracer.getTraceHistory().size());
        assertTrue(tracer.getTraceHistory().get(0).first.contains("app_background"));
        assertTrue(tracer.getTraceHistory().get(1).first.contains("app_foreground"));
    }

    @Test
    public void testTraceNetworkSwitch() {
        // Given
        FunPlusRUM tracer = new FunPlusRUM(funPlusConfig);
        String sourceState = "MOBILE";
        String currentState = "WIFI";

        // When
        tracer.traceNetworkSwitch(sourceState, currentState);

        // Then
        assertEquals(1, tracer.getTraceHistory().size());
        assertTrue(tracer.getTraceHistory().get(0).first.contains("network_switch"));
    }

    @Test
    public void testTraceServiceMonitoring() {
        // Given
        FunPlusRUM tracer = new FunPlusRUM(funPlusConfig);
        String serviceName = "testservice";
        String httpUrl = "http://url.com";
        String httpStatus = "200";
        int requestSize = 120;
        int responseSize = 130;
        long httpLatency = 100;
        long requestTs = 0;
        long responseTs = 0;
        String requestId = "id1234";
        String userId = "testuser";
        String serverId = "testserver";

        // When
        tracer.traceServiceMonitoring(
                serviceName,
                httpUrl,
                httpStatus,
                requestSize,
                responseSize,
                httpLatency,
                requestTs,
                responseTs,
                requestId,
                userId,
                serverId
        );

        // Then
        assertEquals(1, tracer.getTraceHistory().size());
        assertTrue(tracer.getTraceHistory().get(0).first.contains("service_monitoring"));
    }

    @Test
    public void testOnActivityResumed() throws InterruptedException {
        // Given
        FunPlusRUM tracer = new FunPlusRUM(funPlusConfig);

        // When
        tracer.onActivityResumed(null);

        // Then
        assertEquals(1, tracer.getTraceHistory().size());
        assertTrue(tracer.getTraceHistory().get(0).first.contains("app_foreground"));
    }

    @Test
    public void testOnActivityPaused() throws InterruptedException {
        // Given
        FunPlusRUM tracer = new FunPlusRUM(funPlusConfig);

        // When
        tracer.onActivityPaused(null);

        // Then
        assertEquals(1, tracer.getTraceHistory().size());
        assertTrue(tracer.getTraceHistory().get(0).first.contains("app_background"));
    }

    @Test
    public void testSuppressHistory() {
        // Given
        FunPlusConfig funPlusConfig = FunPlusConfigFactory.rumSampleRateZeroConfig();
        FunPlusRUM tracer = new FunPlusRUM(funPlusConfig);

        // When
        tracer.traceAppBackground();
        tracer.traceAppForeground();

        // Then
        assertEquals(2, tracer.getSuppressHistory().size());
        assertTrue(tracer.getSuppressHistory().get(0).first.contains("app_background"));
        assertTrue(tracer.getSuppressHistory().get(1).first.contains("app_foreground"));
    }

    @Test
    public void testGetNetworkInfo() {
        // Given, When
        FunPlusRUM tracer = new FunPlusRUM(funPlusConfig);

        // Then
        assertNotNull(tracer.getNetworkInfo());
    }
}
