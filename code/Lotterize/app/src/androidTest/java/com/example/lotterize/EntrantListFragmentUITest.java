package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
public class EntrantListFragmentUITest extends BaseUITest {

    private static final String EVENT_ID = "testEventId2";  // use same ID as in BaseUITest

    @Before
    public void setUpEntrantEvent() {
        //Create some entrant IDs for the waitlist
        ArrayList<String> waitList = new ArrayList<>();
        waitList.add("userWait1");
        waitList.add("userWait2");

        //Override the event with a version that has waitList filled
        Event event = new Event(
                EVENT_ID,
                CurrentUser.get().getUserId(),   // ownerId
                waitList,                        // waitList
                new ArrayList<>(),               // selectedList
                new ArrayList<>(),               // cancelledList
                new ArrayList<>(),               // finalList
                "EntrantListTestEvent",          // eventName
                null,
                null,
                null,
                "Edmonton",
                10,
                "Event for EntrantListFragment test",
                0,
                null,
                null,
                null,
                new ArrayList<>(),
                false,
                null
        );

        mockEventsRepository.addEvent(event);

        mockUsersRepository.setName("userWait1", "Nathan");
        mockUsersRepository.setName("userWait2", "Bui");
    }

    @Test
    public void testShowWaitList() {
        try (ActivityScenario<UserActivity> scenario =
                     ActivityScenario.launch(UserActivity.class)) {

            scenario.onActivity(activity -> {
                NavController navController =
                        Navigation.findNavController(activity,
                                R.id.nav_host_fragment_activity_user);

                Bundle args = new Bundle();
                args.putString("eventId", EVENT_ID);
                args.putString("status", "WAITLIST");

                navController.navigate(R.id.navigation_entrantList, args);
            });

            // Title text
            onView(withId(R.id.title))
                    .check(matches(withText("Waitlist Entrants")));

            // Our names should now be displayed in the ListView
            onView(withText("Nathan")).check(matches(isDisplayed()));
            onView(withText("Bui")).check(matches(isDisplayed()));
        }
    }
}
