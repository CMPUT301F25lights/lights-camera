package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;


import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AllEntrantsFragmentUITest extends BaseUITest {

    @Test
    public void TestPreventSendingEmptyNotification() {
        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(UserActivity.class)) {

            // Navigate programmatically to AllEntrantsFragment with an eventId
            scenario.onActivity(activity -> {
                NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_activity_user);
                Bundle args = new Bundle();
                args.putString("eventId", "testEventId1");
                navController.navigate(R.id.navigation_entrantsFragment, args);
            });

            onView(withId(R.id.btn_notify_chosen)).check(matches(isDisplayed())).perform(click());

            // Click send with empty message.
            onView(withId(R.id.btn_send)).perform(click());

            // Assert TextInputLayout shows the correct error text.
            onView(withId(R.id.input_layout_message)).check(matches(isDisplayed()));
            onView(withText("Message cannot be empty")).check(matches(isDisplayed()));

            // Dialog is still open (send button is still visible).
            onView(withId(R.id.btn_send)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void TestSetCorrectTitleForNotificationDialog() {
        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(UserActivity.class)) {

            // Navigate programmatically to AllEntrantsFragment with an eventId
            scenario.onActivity(activity -> {
                NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_activity_user);
                Bundle args = new Bundle();
                args.putString("eventId", "testEventId1");
                navController.navigate(R.id.navigation_entrantsFragment, args);
            });


            // Chosen Entrants
            onView(withId(R.id.btn_notify_chosen)).check(matches(isDisplayed())).perform(click());
            onView(withText("Send to Chosen Entrants")).check(matches(isDisplayed()));

//            //Waitlisted Entrants
            onView(withId(R.id.btn_cancel)).perform(click());
            onView(withId(R.id.btn_notify_waitlist)).check(matches(isDisplayed())).perform(click());
            onView(withText("Send to Entrants in waiting list")).check(matches(isDisplayed()));

//            //Cancelled Entrants
            onView(withId(R.id.btn_cancel)).perform(click());
            onView(withId(R.id.btn_notify_cancelled)).check(matches(isDisplayed())).perform(click());
            onView(withText("Send to Cancelled Entrants")).check(matches(isDisplayed()));

        }
    }


}
