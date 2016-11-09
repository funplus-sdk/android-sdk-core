package com.funplus.sdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class LoggerDataConsumerTest {

    private FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();

    @Test
    public void testConsume() throws Exception {
        // Given
        LoggerDataConsumer consumer = new LoggerDataConsumer(funPlusConfig);

        // When
        getLogger().i("info message");
        getLogger().w("warn message");
        getLogger().e("error message");
        getLogger().wtf("fatal message");
        consumer.consume();

        Thread.sleep(1000);

        // Then
        assertEquals(4, consumer.getLogAgentClient().getDataQueue().size());
    }

    private ILogger getLogger() {
        return FunPlusFactory.getLogger(funPlusConfig);
    }
}
