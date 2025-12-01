package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.allOf;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.Timestamp;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AddEventsFragmentUITest extends BaseUITest {


    @Test
    public void TestShowOnlyOwnedEvents() {
        // Event that belongs to different user
        Event otherUserEvent = new Event(
                "testEventId2",
                "testUserId2",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                "TestEvent2",
                Timestamp.now(),
                null,
                null,
                "Edmonton",
                10,
                "This event is used for testing",
                0,
                null,
                null,
                null,
                new ArrayList<>(),
                false,
                null
        );
        mockEventsRepository.addEvent(otherUserEvent);

        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(UserActivity.class)) {

            //Navigate to Add_Event tab
            onView(withId(R.id.navigation_addEvents)).perform(click());

            //Assert The current user's event ("TestEvent") is visible
            onView(withText("TestEvent")).check(matches(isDisplayed()));

            //Assert the other user's event ("TestEvent2") is not in the listView
            onView(withText("TestEvent2")).check(doesNotExist());

        }
    }

    @Test
    public void TestShowEventWithLongName() {
        // Event
        Event otherUserEvent = new Event(
                "testEventId3",
                "testUserId1",
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                "This is an event with a very long name and i'm testing whether it's shown on the screen",
                Timestamp.now(),
                null,
                null,
                "Edmonton",
                10,
                "This event is used for testing",
                0,
                null,
                null,
                null,
                new ArrayList<>(),
                false,
                null
        );
        mockEventsRepository.addEvent(otherUserEvent);

        try (ActivityScenario<UserActivity> scenario = ActivityScenario.launch(UserActivity.class)) {

            //Navigate to Add_Event tab
            onView(withId(R.id.navigation_addEvents)).perform(click());

            //Assert The current event with long name  is visible
            onView(withText("TestEvent")).check(matches(isDisplayed()));

            onView(allOf(withId(R.id.text_title),withText("This is an event with a very long name and i'm testing whether it's shown on the screen"))).check(matches(isDisplayed()));

        }
    }
}
