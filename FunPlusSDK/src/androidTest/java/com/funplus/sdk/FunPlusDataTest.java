package com.funplus.sdk;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by yuankun on 13/10/2016.
 */

public class FunPlusDataTest {

    private FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();

    @Test
    public void testTraceSessionStart() {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);

        // When
        tracer.traceSessionStart();

        // Then
        assertEquals(1, tracer.getKpiTraceHistory().size());
        assertTrue(tracer.getKpiTraceHistory().get(0).first.contains("session_start"));
    }

    @Test
    public void testTraceSessionEnd() {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);

        // When
        tracer.traceSessionStart();
        tracer.traceSessionEnd(100);

        // Then
        assertEquals(2, tracer.getKpiTraceHistory().size());
        assertTrue(tracer.getKpiTraceHistory().get(0).first.contains("session_start"));
        assertTrue(tracer.getKpiTraceHistory().get(1).first.contains("session_end"));
    }

    @Test
    public void testTraceNewUser() {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);

        // When
        tracer.traceNewUser();

        // Then
        assertEquals(1, tracer.getKpiTraceHistory().size());
        assertTrue(tracer.getKpiTraceHistory().get(0).first.contains("new_user"));
    }

    @Test
    public void testTracePayment() {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);
        double amount = 399.0;
        String currency = "USD";
        String productId = "com.funplus.barnvoyage.jewelBox.270";
        String productName = "Jewel Box 270";
        String productType = "rc";
        String transactionId = "23533353";
        String paymentProcessor = "appleiap";
        String itemsReceived = "items_received";
        String currencyReceived = "currency_received";
        String currencyReceivedType = "currency_received_type";

        // When
        tracer.tracePayment(amount, currency, productId, productName, productType, transactionId, paymentProcessor, itemsReceived, currencyReceived, currencyReceivedType);

        // Then
        assertEquals(1, tracer.getKpiTraceHistory().size());
        assertTrue(tracer.getKpiTraceHistory().get(0).first.contains("payment"));
    }

    @Test
    public void testTraceCustom() throws JSONException {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);
        JSONObject event = new JSONObject();
        event.put("event", "level_up");
        event.put("target_level", 10);

        // When
        tracer.traceCustom(event);

        // Then
        assertEquals(1, tracer.getCustomTraceHistory().size());
        assertTrue(tracer.getCustomTraceHistory().get(0).first.contains("level_up"));
    }
}
