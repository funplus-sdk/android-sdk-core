package com.funplus.sdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LoggerTest {

    private FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();

    @Test
    public void testTrace() {
        // Given
        Logger logger = new Logger(funPlusConfig);
        String logLevelString = "INFO";
        String message1 = "message_1";
        String message2 = "message_2";

        // When
        logger.trace(logLevelString, message1);
        logger.trace(logLevelString, message2);

        // Then
        assertEquals(2, logger.consumeLogs().size());
    }

    @Test
    public void testTraceInfo() {
        // Given
        Logger logger = new Logger(funPlusConfig);
        String message1 = "info_message_1";
        String message2 = "info_message_2";

        // When
        logger.i(message1);
        logger.i(message2);

        // Then
        List<String> logs = logger.consumeLogs();
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).contains("INFO"));
        assertTrue(logs.get(1).contains("INFO"));
    }

    @Test
    public void testTraceWarn() {
        // Given
        Logger logger = new Logger(funPlusConfig);
        String message1 = "warn_message_1";
        String message2 = "warn_message_2";

        // When
        logger.w(message1);
        logger.w(message2);

        // Then
        List<String> logs = logger.consumeLogs();
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).contains("WARN"));
        assertTrue(logs.get(1).contains("WARN"));
    }

    @Test
    public void testTraceError() {
        // Given
        Logger logger = new Logger(funPlusConfig);
        String message1 = "error_message_1";
        String message2 = "error_message_2";

        // When
        logger.e(message1);
        logger.e(message2);

        // Then
        List<String> logs = logger.consumeLogs();
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).contains("ERROR"));
        assertTrue(logs.get(1).contains("ERROR"));
    }

    @Test
    public void testTraceWtf() {
        // Given
        Logger logger = new Logger(funPlusConfig);
        String message1 = "fatal_message_1";
        String message2 = "fatal_message_2";

        // When
        logger.wtf(message1);
        logger.wtf(message2);

        // Then
        List<String> logs = logger.consumeLogs();
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).contains("FATAL"));
        assertTrue(logs.get(1).contains("FATAL"));
    }

    @Test
    public void testMixTrace() {
        // Given
        Logger logger = new Logger(funPlusConfig);
        String message1 = "info_message_1";
        String message2 = "fatal_message_1";

        // When
        logger.i(message1);
        logger.wtf(message2);

        // Then
        List<String> logs = logger.consumeLogs();
        assertEquals(2, logs.size());
        assertTrue(logs.get(0).contains("INFO"));
        assertTrue(logs.get(1).contains("FATAL"));
    }
}
