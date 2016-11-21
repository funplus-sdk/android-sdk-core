package com.funplus.sdk;

import org.json.JSONObject;

public interface IFunPlusData {
    void registerEventTracedListener(FunPlusData.EventTracedListener listener);

    void traceSessionStart();
    void traceSessionEnd(long sessionLength);
    void traceNewUser();
    void traceCustom(JSONObject event);
    void traceCustomEventWithNameAndProperties(String eventName, JSONObject properties);
    void tracePayment(double amount, String currency, String productId,
                      String productName,
                      String productType,
                      String transactionId,
                      String paymentProcessor,
                      String itemsReceived,
                      String currencyReceived);

    void setExtraProperty(String key, String value);
    void eraseExtraProperty(String key);
}
