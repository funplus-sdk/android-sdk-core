package com.funplus.sdk;

import android.support.test.InstrumentationRegistry;
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
public class DeviceInfoTest {

    private DeviceInfo deviceInfo;

    @Before
    public void setUp() {
        deviceInfo = new DeviceInfo(InstrumentationRegistry.getContext());
    }

    @Test
    public void testDeviceInfo() {
        assertNotNull(deviceInfo.androidId);
        assertNotNull(deviceInfo.deviceType);
        assertNotNull(deviceInfo.deviceName);
        assertNotNull(deviceInfo.deviceManufacturer);
        assertNotNull(deviceInfo.osName);
        assertNotNull(deviceInfo.osVersion);
        assertNotNull(deviceInfo.apiLevel);
        assertNotNull(deviceInfo.language);
        assertNotNull(deviceInfo.country);
        assertNotNull(deviceInfo.hardwareName);
    }
}
