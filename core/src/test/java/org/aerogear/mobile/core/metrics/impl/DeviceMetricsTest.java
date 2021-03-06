package org.aerogear.mobile.core.metrics.impl;

import android.app.Application;
import android.support.test.filters.SmallTest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@SmallTest
public class DeviceMetricsTest {

    @Test
    public void testType() {
        Application context = RuntimeEnvironment.application;

        DeviceMetrics deviceMetrics = new DeviceMetrics(context);
        assertEquals("device", deviceMetrics.identifier());
    }

    @Test
    public void testData() {
        Application context = RuntimeEnvironment.application;

        DeviceMetrics deviceMetrics = new DeviceMetrics(context);
        Map<String, String> result = deviceMetrics.data();

        assertNotNull(result.get("platform"));
        assertNotNull(result.get("platformVersion"));
    }

}
