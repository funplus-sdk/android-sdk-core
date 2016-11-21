package com.funplus.sdk;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by yuankun on 02/10/2016.
 */

@RunWith(AndroidJUnit4.class)
@SmallTest
public class ConfigManagerTest {

//    private static final String APP_ID = "test";
//    private static final String APP_KEY = "test_funplus";
//    private static final SDKEnvironment ENV = SDKEnvironment.Sandbox;
//    private static final String CONFIG_ETAG_SAVED_KEY = "com.funplus.sdk.ConfigEtag";
//
//    @After
//    public void tearDown() {
//        Context context = InstrumentationRegistry.getContext();
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();
//        editor.apply();
//    }
//
//    @Test
//    public void testInitWithoutTimer() {
//        // Given, When
//        Context context = InstrumentationRegistry.getContext();
//        ConfigManager configManager = new ConfigManager(context, APP_ID, APP_KEY, ENV);
//
//        // Then
//        assertNull(configManager.getTimer());
//    }
//
//    @Test
//    public void testInitWithTimer() {
//        // Given, When
//        Context context = InstrumentationRegistry.getContext();
//        long interval = 30;
//        ConfigManager configManager = new ConfigManager(context, APP_ID, APP_KEY, ENV, interval);
//
//        // Then
//        assertNotNull(configManager.getTimer());
//    }
//
//    @Test
//    public void testRetrieveEmptyConfigEtag() {
//        // Given
//        Context context = InstrumentationRegistry.getContext();
//        ConfigManager configManager = new ConfigManager(context, APP_ID, APP_KEY, ENV);
//
//        // When
//        String etag = configManager.getConfigEtag();
//
//        // Then
//        assertTrue(etag.isEmpty());
//    }
//
//    @Test
//    public void testSaveConfigEtag() {
//        // Given
//        String expectedEtag = "test_config_etag";
//
//        Context context = InstrumentationRegistry.getContext();
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString(CONFIG_ETAG_SAVED_KEY, expectedEtag);
//        editor.apply();
//
//        ConfigManager configManager = new ConfigManager(context, APP_ID, APP_KEY,  ENV);
//
//        // When
//        String etag = configManager.getConfigEtag();
//
//        // Then
//        assertEquals(expectedEtag, etag);
//    }
//
//    @Test
//    public void testGetFunPlusConfig() throws Exception {
//        // Given
//        Context context = InstrumentationRegistry.getContext();
//        ConfigManager configManager = new ConfigManager(context, APP_ID, APP_KEY,  ENV);
//
//        // When
//        FunPlusConfig funPlusConfig = configManager.getFunPlusConfig();
//
//        // Then
//        assertNotNull(funPlusConfig);
//    }
}
