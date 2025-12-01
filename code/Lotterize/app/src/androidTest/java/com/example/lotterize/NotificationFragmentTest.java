package com.example.lotterize;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Set;

@RunWith(AndroidJUnit4.class)
@LargeTest

public class NotificationFragmentTest{
    @Test
    public void test_NotificationsTabIsDisplayed() {
        //Create a mock logged-in user (entrant/organizer)
        User mockUser = new User(
                "testUserId",
                "Test User",
                "555-5555",
                "test@example.com",
                null,
                "testUsername",
                "testPassword"
        );
        mockUser.setOwnedEventIds(new ArrayList<>());
        mockUser.setRegisteredEventIds(new ArrayList<>());
        mockUser.setWantNotification(true);

        //Set mockUser before launching the activity
        CurrentUser.set(mockUser);

        // launch UserActivity
        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(UserActivity.class)) {

            //Navigate to Notifications tab
            onView(withId(R.id.navigation_notifications)).perform(click());

            //Assert the ListView is visible
            onView(withId(R.id.list_notifications)).check(matches(isDisplayed()));
        }
    }




}
