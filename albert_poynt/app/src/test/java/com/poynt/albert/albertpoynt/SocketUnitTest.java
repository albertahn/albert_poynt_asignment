package com.poynt.albert.albertpoynt;

import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.runner.RunWith;



import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

public class SocketUnitTest {

    private  MainActivity mMainActivity;


    @Before
    public void setUp() throws Exception {
        // Code that you wish to run before each test
        mMainActivity = new MainActivity();
    }

    @After
    public void tearDown() throws Exception {
        // Code to run after each test


        mMainActivity.finish();

    }


}