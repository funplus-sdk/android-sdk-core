package com.funplus.sdk;

import java.util.List;

interface ILogger {
    void i(String message, Object... parameters);
    void w(String message, Object... parameters);
    void e(String message, Object... parameters);
    void wtf(String message, Object... parameters);
    List<String> consumeLogs();
}
