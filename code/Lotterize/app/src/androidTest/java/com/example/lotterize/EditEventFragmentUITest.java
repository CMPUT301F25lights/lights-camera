package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.Visibility.GONE;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
@LargeTest

public class EditEventFragmentUITest extends BaseUITest{


    @Test
    public void TestEventDetailsMatched_WithSomeMissingFields() {

        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(UserActivity.class)) {

            //Navigate to Add_Event tab
            onView(withId(R.id.navigation_addEvents)).perform(click());

            //Click on a specific event and navigate to the edit fragment
            onView(allOf(withId(R.id.text_title),withText("TestEvent"))).perform(click());

            //Assert all event's details are matched
            onView(withId(R.id.tvEventNameValue)).check(matches(withText("TestEvent")));
            onView(withId(R.id.tvDateValue)).check(matches(withText("")));
            onView(withId(R.id.tvTimeValue)).check(matches(withText("")));
            onView(withId(R.id.tvLocationValue)).check(matches(withText("Edmonton")));
            onView(withId(R.id.tvTotalSpotsValue)).check(matches(withText("10")));
            onView(withId(R.id.tvDescriptionValue)).check(matches(withText("This event is used for testing")));
            onView(withId(R.id.tvWaitlistValue)).check(matches(withText("0")));
            onView(withId(R.id.tvSampleAttendeesValue)).check(matches(withText("0")));
            onView(withId(R.id.switch_geolocation)).check(matches(isChecked()));
            onView(withId(R.id.posterTextView)).check(matches(withEffectiveVisibility(GONE)));

        }
    }
}
