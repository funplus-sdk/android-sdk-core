package com.funplus.sdk;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class PassportClientTest {

    private static final long TIMEOUT = 30;
    private static final String EXISTING_FPID = "13042";

    private FunPlusConfig funPlusConfig = FunPlusConfigFactory.defaultFunPlusConfig();

    @After
    public void tearDown() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(funPlusConfig.context);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    @Test
    public void testGetCurrentFPID() throws Exception {
        // Given
        PassportClient passportClient = new PassportClient(funPlusConfig);

        // When
        String currentFPID = passportClient.getCurrentFPID();

        // Then
        assertFalse(currentFPID.isEmpty());
    }

    @Test
    public void testGet() throws Exception {
        // Given
        PassportClient passportClient = new PassportClient(funPlusConfig);
        String externalID = "testuser";
        FunPlusID.ExternalIDType externalIDType = FunPlusID.ExternalIDType.InAppUserID;
        final CountDownLatch latch = new CountDownLatch(1);

        final Map<String, String> result = new HashMap<>();

        // When
        passportClient.get(externalID, externalIDType, new FunPlusID.FunPlusIDHandler() {
            @Override
            public void onSuccess(String fpid) {
                result.put("fpid", fpid);
                latch.countDown();
            }

            @Override
            public void onFailure(FunPlusID.Error error) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);

        // Then
        assertEquals(result.size(), 1);
        assertEquals(result.get("fpid"), EXISTING_FPID);
    }

    @Test
    public void testGetNonExistingExternalID() throws Exception {
        // Given
        PassportClient passportClient = new PassportClient(funPlusConfig);
        String externalID = UUID.randomUUID().toString();
        FunPlusID.ExternalIDType externalIDType = FunPlusID.ExternalIDType.InAppUserID;
        final  CountDownLatch latch = new CountDownLatch(1);

        final Map<String, String> result = new HashMap<>();

        // When
        passportClient.get(externalID, externalIDType, new FunPlusID.FunPlusIDHandler() {
            @Override
            public void onSuccess(String fpid) {
                result.put("fpid", fpid);
                latch.countDown();
            }

            @Override
            public void onFailure(FunPlusID.Error error) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);

        // Then
        assertEquals(result.size(), 1);
        assertNotEquals(result.get("fpid"), EXISTING_FPID);
    }

    @Test
    public void testBind() throws Exception {
        // Given
        PassportClient passportClient = new PassportClient(funPlusConfig);
        String bindToFpid = EXISTING_FPID;
        String externalID = UUID.randomUUID().toString();
        FunPlusID.ExternalIDType externalIDType = FunPlusID.ExternalIDType.InAppUserID;
        final CountDownLatch latch = new CountDownLatch(1);

        final Map<String, String> result = new HashMap<>();

        // When
        passportClient.bind(bindToFpid, externalID, externalIDType, new FunPlusID.FunPlusIDHandler() {
            @Override
            public void onSuccess(String fpid) {
                result.put("fpid", fpid);
                latch.countDown();
            }

            @Override
            public void onFailure(FunPlusID.Error error) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);

        // Then
        assertEquals(result.size(), 1);
        assertEquals(result.get("fpid"), EXISTING_FPID);
    }

    @Test
    public void testBindExternalIDToNonExistingFPID() throws Exception {
        // Given
        PassportClient passportClient = new PassportClient(funPlusConfig);
        String bindToFpid = "non-existing";
        String externalID = UUID.randomUUID().toString();
        FunPlusID.ExternalIDType externalIDType = FunPlusID.ExternalIDType.InAppUserID;
        final CountDownLatch latch = new CountDownLatch(1);

        final Map<String, String> result = new HashMap<>();

        // When
        passportClient.bind(bindToFpid, externalID, externalIDType, new FunPlusID.FunPlusIDHandler() {
            @Override
            public void onSuccess(String fpid) {
                result.put("fpid", fpid);
                latch.countDown();
            }

            @Override
            public void onFailure(FunPlusID.Error error) {
                latch.countDown();
            }
        });

        latch.await(TIMEOUT, TimeUnit.SECONDS);

        // Then
        assertEquals(result.size(), 0);
    }

    @Test
    public void testBindExistingExternalIDToSameFPID() {
        // TODO
    }

    @Test
    public void testBindExistingExternalIDToDifferentFPID() {
        // TODO
    }
}
