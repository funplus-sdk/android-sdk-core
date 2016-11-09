package com.funplus.sdk;

import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertNotNull;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class SessionManagerTest {

    private FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();

    @Test
    public void testSessionManagerInit() {
        // Given, When
        SessionManager sessionManager = new SessionManager(funPlusConfig);

        // Then
        assertNotNull(sessionManager.getUserId());
        assertNotNull(sessionManager.getSessionId());
    }
}
