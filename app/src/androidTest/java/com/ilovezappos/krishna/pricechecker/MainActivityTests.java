package com.ilovezappos.krishna.pricechecker;

import android.app.Instrumentation;
import android.content.Intent;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

/**
 * Created by sanjanabadam on 9/9/16.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTests {

    final static String EXPECTED_URL = "";


    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);


    @Test
    public void testMainActivity() {
        assertWorksWithValidProductString();
    }


    @Test
    public void testMainActivityWithEmptyString() {
        onView(withId(R.id.searchBox)).perform(typeText(""), closeSoftKeyboard());
        onView(withText("Get my product!")).perform(click());
        onView(withText(R.string.toast_string_main_activity)).inRoot(withDecorView(not(is(mActivityRule
                .getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    }


    @Test
    public void testRecyclerView() {
        assertWorksWithValidProductString();
        onView(withId(R.id.recyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(
                0, click()));
        onView(withText(R.string.navigate_button_product_page)).check(matches(isDisplayed()));
        onView(withText(R.string.share_me_button_product_page)).check(matches(isDisplayed()));
    }


    @Test
    public void testOpenWebPage() {
        assertWorksWithValidProductString();
        onView(withId(R.id.recyclerView)).perform(RecyclerViewActions.actionOnItemAtPosition(
                0, click()));
        Intents.init();
        Matcher<Intent> expectedIntent = allOf(hasAction(Intent.ACTION_VIEW), hasData(EXPECTED_6PM_URL));
        intending(expectedIntent).respondWith(new Instrumentation.ActivityResult(0, null));
        onView(withText(R.string.navigate_button_product_page)).perform(click());
        intended(expectedIntent);
        Intents.release();
    }


    public void assertWorksWithValidProductString() {
        Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(
                ProductResults.class.getName(), null, false);
        onView(withId(R.id.searchBox)).perform(typeText("heels"), closeSoftKeyboard());
        onView(withText(R.string.submit_button_main_activity)).perform(click());
        ProductResults nextActivity = (ProductResults) getInstrumentation()
                .waitForMonitorWithTimeout(activityMonitor, 5000);
        assertNotNull(nextActivity);
    }


}
