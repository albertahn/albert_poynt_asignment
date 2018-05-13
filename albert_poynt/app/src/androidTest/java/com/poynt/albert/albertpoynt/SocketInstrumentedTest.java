package com.poynt.albert.albertpoynt;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class SocketInstrumentedTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);


    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test check.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.poynt.albert.albertpoynt", appContext.getPackageName());
    }

    @Test
    public void testFirebase() throws Exception {
        // Check for socket listener test

        mActivityRule.getActivity().setSocketListener();

    }

    @Test
    public void testUpdateResult() throws Exception {

        mActivityRule.getActivity().updateFirebasePayResult(mActivityRule.getActivity().getApplicationContext().getResources().getString(R.string.pay_show_here));

    }

    @Test
    public void testPaymentFragment() throws Exception{

        mActivityRule.getActivity().launchPayment(1000l);
    }


}
