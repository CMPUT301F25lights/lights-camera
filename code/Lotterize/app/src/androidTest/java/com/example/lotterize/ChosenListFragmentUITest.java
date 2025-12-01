package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.core.app.ActivityScenario;


import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class ChosenListFragmentUITest extends BaseUITest {

    private static final String EVENT_ID = "testEventId3";

    @Before
    public void setUpChosenEvent() {


        // selectedList: 3 entrants
        ArrayList<String> selectedList = new ArrayList<>();
        selectedList.add("user1");
        selectedList.add("user2");
        selectedList.add("user3");

        // finalList: 2 enrolled
        ArrayList<String> finalList = new ArrayList<>();
        finalList.add("user1");
        finalList.add("user2");

        Event event = new Event(
                EVENT_ID,
                CurrentUser.get().getUserId(),   // ownerId
                new ArrayList<>(),               // waitList
                selectedList,                    // selectedList
                new ArrayList<>(),               // cancelledList
                finalList,                       // finalList
                "ChosenListTestEvent",           // eventName
                null,
                null,
                null,
                "Edmonton",
                3,
                "Event for ChosenEntrantsListFragment test",
                0,
                null,
                null,
                null,
                new ArrayList<>(),
                false,
                null
        );
        mockEventsRepository.addEvent(event);


        mockUsersRepository.setName("user1", "Nathan");
        mockUsersRepository.setName("user2", "Bui");
        mockUsersRepository.setName("user3", "User3");
    }

    @Test
    public void chosenList_showsThreeEntrantsWithCorrectStatus() {
        try (ActivityScenario<com.example.lotterize.UserActivity> scenario =
                     ActivityScenario.launch(com.example.lotterize.UserActivity.class)) {

            // Navigate to ChosenEntrantsListFragment with our EVENT_ID
            scenario.onActivity(activity -> {
                NavController navController = Navigation.findNavController(activity, R.id.nav_host_fragment_activity_user);

                Bundle args = new Bundle();
                args.putString("eventId", EVENT_ID);
                navController.navigate(R.id.navigation_chosenEntrantsFragment, args);
            });

            // --- Check the three rows are displayed by name/ID ---

            onView(withText("Nathan")).check(matches(isDisplayed()));
            onView(withText("Bui")).check(matches(isDisplayed()));
            onView(withText("User3")).check(matches(isDisplayed()));


            // user1 → Enrolled
            onView(allOf(withId(R.id.text_entrant_status), withText("Enrolled"), hasSibling(withText("Nathan")))).check(matches(isDisplayed()));

            // user2 → Enrolled
            onView(allOf(withId(R.id.text_entrant_status), withText("Enrolled"), hasSibling(withText("Bui")))).check(matches(isDisplayed()));

            // user3 → Remove
            onView(allOf(withId(R.id.text_entrant_status), withText("Remove"), hasSibling(withText("User3")))).check(matches(isDisplayed()));

        }
    }
}
