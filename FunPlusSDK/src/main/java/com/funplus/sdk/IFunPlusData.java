package com.funplus.sdk;

import org.json.JSONObject;

public interface IFunPlusData {
    void registerEventTracedListener(FunPlusData.EventTracedListener listener);

    void traceCustom(JSONObject event);
    void traceSessionStart();
    void traceSessionEnd(long sessionLength);
    void traceNewUser();
    void tracePayment(double amount, String currency, String productId,
                      String productName,
                      String productType,
                      String transactionId,
                      String paymentProcessor,
                      String itemsReceived, String currencyReceived,
                      String currencyReceivedType);

    void setExtraProperty(String key, String value);
    void eraseExtraProperty(String key);
}
