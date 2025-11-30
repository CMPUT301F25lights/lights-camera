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

import java.util.Set;

@RunWith(AndroidJUnit4.class)
@LargeTest

public class AddEventsFragmentUITest {
    @Before
    public void setUp(){
        User mockUser = new User(
                "testUserId",
                "Test User",
                "555-5555",
                "test@example.com",
                null,
                "testUsername",
                "testPassword"
        );
        mockUser.setOwnedEventIds(new java.util.ArrayList<>());
        mockUser.setRegisteredEventIds(new java.util.ArrayList<>());
        mockUser.setWantNotification(true);
        CurrentUser.set(mockUser);
    }

//    public void clickingEvent_opensEditScreenWithCorrectDetails() {
//        // 1. Go to the organizer "My Events" tab if needed.
//        //    Replace R.id.navigation_my_events with your bottom-nav ID.
//        onView(withId(R.id.navigation_my_events))
//                .perform(click());
//
//        // 2. Wait a bit for Firestore to load the events into the list.
//        //    (Simple CMPUT-301 level hack instead of IdlingResource.)
//        onView(isRoot()).perform(waitFor(2000));
//
//        // 3. Click on the event with name "testingnov2" in the list.
//        onView(withText("testingnov2"))
//                .perform(click());
//
//        // 4. We are now on EditEventFragment.
//        //    Check that all fields show the correct values.
//        //
//        //    IMPORTANT: replace these IDs with your actual EditText IDs
//        //    from fragment_edit_event.xml.
//
//        // Event name
//        onView(withId(R.id.edit_event_name))
//                .check(matches(withText("testingnov2")));
//
//        // Date
//        onView(withId(R.id.edit_event_date))
//                .check(matches(withText("October 10, 2001")));
//
//        // Time
//        onView(withId(R.id.edit_event_time))
//                .check(matches(withText("10:10 AM")));
//
//        // Location
//        onView(withId(R.id.edit_event_location))
//                .check(matches(withText("edmonton")));
//
//        // Total spots
//        onView(withId(R.id.edit_event_total_spots))
//                .check(matches(withText("10")));
//
//        // Description
//        onView(withId(R.id.edit_event_description))
//                .check(matches(withText("a")));
//
//        // Limit entrants on waitlist
//        onView(withId(R.id.edit_event_waitlist_limit))
//                .check(matches(withText("10")));
//
//        // Sample attendees
//        onView(withId(R.id.edit_event_sample_attendees))
//                .check(matches(withText("0")));
//
//        // Optional: check that the "Edit Event" title is visible
//        onView(withText("Edit Event"))
//                .check(matches(isDisplayed()));
//    }
}
