package org.aerogear.mobile.auth.authenticator;

import android.app.Activity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class OIDCAuthenticateOptionsTest {

    @Mock
    Activity activity;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testParameterValidation() {
        new DefaultAuthenticateOptions(null, 5);
    }

    @Test
    public void testGetOptions() {
        DefaultAuthenticateOptions opts = new DefaultAuthenticateOptions(activity, 5);
        Assert.assertEquals(activity, opts.getFromActivity());
        Assert.assertEquals(5, opts.getResultCode());
    }
}
