package com.funplus.sdk;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by yuankun on 13/10/2016.
 */

public class FunPlusDataTest {

    private FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();

    @Test
    public void testTraceSessionStart() throws Exception {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);

        // When
        tracer.traceSessionStart();

        // Then
        assertEquals(1, tracer.getKpiTraceHistory().size());

        String eventString = tracer.getKpiTraceHistory().get(0).first;
        JSONObject event = new JSONObject(eventString);

        assertEquals(event.getString("event"), "session_start");
        assertEquals(event.getString("data_version"), "2.0");
        assertNotNull(event.getString("app_id"));
        assertNotNull(event.getString("ts"));
        assertNotNull(event.getString("user_id"));
        assertNotNull(event.getString("session_id"));
        assertNotNull(event.getJSONObject("properties"));

        JSONObject properties = event.getJSONObject("properties");
        assertNotNull(properties.getString("app_version"));
        assertNotNull(properties.getString("os"));
        assertNotNull(properties.getString("os_version"));
        assertNotNull(properties.getString("lang"));
        assertNotNull(properties.getString("install_ts"));
    }

    @Test
    public void testTraceSessionEnd() throws Exception {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);

        // When
        tracer.traceSessionStart();
        tracer.traceSessionEnd(100);

        // Then
        assertEquals(2, tracer.getKpiTraceHistory().size());
        assertTrue(tracer.getKpiTraceHistory().get(0).first.contains("session_start"));

        String eventString = tracer.getKpiTraceHistory().get(1).first;
        JSONObject event = new JSONObject(eventString);

        assertEquals(event.getString("event"), "session_end");
        assertEquals(event.getString("data_version"), "2.0");
        assertNotNull(event.getString("app_id"));
        assertNotNull(event.getString("ts"));
        assertNotNull(event.getString("user_id"));
        assertNotNull(event.getString("session_id"));
        assertNotNull(event.getJSONObject("properties"));

        JSONObject properties = event.getJSONObject("properties");
        assertNotNull(properties.getString("app_version"));
        assertNotNull(properties.getString("os"));
        assertNotNull(properties.getString("os_version"));
        assertNotNull(properties.getString("lang"));
        assertNotNull(properties.getString("install_ts"));
    }

    @Test
    public void testTraceNewUser() throws Exception {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);

        // When
        tracer.traceNewUser();

        // Then
        assertEquals(1, tracer.getKpiTraceHistory().size());

        String eventString = tracer.getKpiTraceHistory().get(0).first;
        JSONObject event = new JSONObject(eventString);

        assertEquals(event.getString("event"), "new_user");
        assertEquals(event.getString("data_version"), "2.0");
        assertNotNull(event.getString("app_id"));
        assertNotNull(event.getString("ts"));
        assertNotNull(event.getString("user_id"));
        assertNotNull(event.getString("session_id"));
        assertNotNull(event.getJSONObject("properties"));

        JSONObject properties = event.getJSONObject("properties");
        assertNotNull(properties.getString("app_version"));
        assertNotNull(properties.getString("os"));
        assertNotNull(properties.getString("os_version"));
        assertNotNull(properties.getString("lang"));
        assertNotNull(properties.getString("install_ts"));
    }

    @Test
    public void testTracePayment() throws Exception {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);
        double amount = 399.0;
        String currency = "USD";
        String productId = "com.funplus.barnvoyage.jewelBox.270";
        String productName = "Jewel Box 270";
        String productType = "rc";
        String transactionId = "23533353";
        String paymentProcessor = "appleiap";
        String itemsReceived = "[\n" +
                "  {\n" +
                "    \"d_item_id\":\"4312\",\n" +
                "    \"d_item_name\":\"booster_butterfly\",\n" +
                "    \"d_item_type\":\"booster\",\n" +
                "    \"m_item_amount\":\"1\",\n" +
                "    \"d_item_class\":\"consumable\"\n" +
                "  }\n" +
                "]";
        String currencyReceived = "[\n" +
                "   {\n" +
                "     \"d_currency_type\":\"rc\",\n" +
                "     \"m_currency_amount\":\"20\"\n" +
                "   },\n" +
                "   {\n" +
                "     \"d_currency_type\":\"coins\",\n" +
                "     \"m_currency_amount\":\"2000\"\n" +
                "   }\n" +
                "]";

        // When
        tracer.tracePayment(amount, currency, productId, productName, productType, transactionId, paymentProcessor, itemsReceived, currencyReceived);

        // Then
        assertEquals(1, tracer.getKpiTraceHistory().size());

        String eventString = tracer.getKpiTraceHistory().get(0).first;
        JSONObject event = new JSONObject(eventString);

        assertEquals(event.getString("event"), "payment");
        assertEquals(event.getString("data_version"), "2.0");
        assertNotNull(event.getString("app_id"));
        assertNotNull(event.getString("ts"));
        assertNotNull(event.getString("user_id"));
        assertNotNull(event.getString("session_id"));
        assertNotNull(event.getJSONObject("properties"));

        JSONObject properties = event.getJSONObject("properties");

        assertNotNull(properties.getString("app_version"));
        assertNotNull(properties.getString("os"));
        assertNotNull(properties.getString("os_version"));
        assertNotNull(properties.getString("lang"));
        assertNotNull(properties.getString("install_ts"));
        assertNotNull(properties.getDouble("amount"));
        assertNotNull(properties.getString("currency"));
        assertNotNull(properties.getString("iap_product_id"));
        assertNotNull(properties.getString("iap_product_name"));
        assertNotNull(properties.getString("iap_product_type"));
        assertNotNull(properties.getString("transaction_id"));
        assertNotNull(properties.getString("payment_processor"));
        assertNotNull(properties.getJSONArray("c_items_received"));
        assertNotNull(properties.getJSONArray("c_currency_received"));
    }

    @Test
    public void testTraceCustom() throws JSONException {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);
        JSONObject event = new JSONObject();
        event.put("event", "plant");
        event.put("data_version", "2.0");
        event.put("app_id", "sdk.global.prod");
        event.put("user_id", "822b3aaa877bcecfc99b28f34521d208");
        event.put("session_id", "591da20f1f7b5ec3f44cade9fea93516_1457663293");
        event.put("ts", "1457664466725");

        JSONObject properties = new JSONObject();
        properties.put("app_version", "1.0.8");
        properties.put("os", "android");
        properties.put("os_version", "4.2.2");
        properties.put("device", "tolino tab 8.9");
        properties.put("lang", "de");
        properties.put("install_ts", 1456195734);

        JSONObject m1 = new JSONObject();
        m1.put("key", "amount");
        m1.put("value", 1);

        properties.put("m1", m1);
        event.put("properties", properties);

        // When
        tracer.traceCustom(event);

        // Then
        assertEquals(1, tracer.getCustomTraceHistory().size());

        String eventString = tracer.getCustomTraceHistory().get(0).first;
        JSONObject evt = new JSONObject(eventString);

        assertEquals(evt.getString("event"), "plant");
        assertEquals(evt.getString("data_version"), "2.0");
        assertNotNull(evt.getString("app_id"));
        assertNotNull(evt.getString("ts"));
        assertNotNull(evt.getString("user_id"));
        assertNotNull(evt.getString("session_id"));
        assertNotNull(evt.getJSONObject("properties"));

        JSONObject props = evt.getJSONObject("properties");
        assertNotNull(props.getString("app_version"));
        assertNotNull(props.getString("os"));
        assertNotNull(props.getString("os_version"));
        assertNotNull(props.getString("lang"));
        assertNotNull(props.getString("install_ts"));
        assertNotNull(props.getJSONObject("m1"));
    }

    @Test
    public void testTraceCustomEventWithNameAndProperties() throws JSONException {
        // Given
        FunPlusData tracer = new FunPlusData(funPlusConfig);
        String eventName = "plant";
        JSONObject properties = new JSONObject();
        JSONObject m1 = new JSONObject();
        m1.put("key", "amount");
        m1.put("value", 1);
        properties.put("m1", m1);

        // When
        tracer.traceCustomEventWithNameAndProperties(eventName, properties);

        // Then
        assertEquals(1, tracer.getCustomTraceHistory().size());

        String eventString = tracer.getCustomTraceHistory().get(0).first;
        JSONObject evt = new JSONObject(eventString);

        assertEquals(evt.getString("event"), "plant");
        assertEquals(evt.getString("data_version"), "2.0");
        assertNotNull(evt.getString("app_id"));
        assertNotNull(evt.getString("ts"));
        assertNotNull(evt.getString("user_id"));
        assertNotNull(evt.getString("session_id"));
        assertNotNull(evt.getJSONObject("properties"));

        JSONObject props = evt.getJSONObject("properties");
        assertNotNull(props.getString("app_version"));
        assertNotNull(props.getString("os"));
        assertNotNull(props.getString("os_version"));
        assertNotNull(props.getString("lang"));
        assertNotNull(props.getString("install_ts"));
        assertNotNull(props.getJSONObject("m1"));
    }
}
