package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lotterize.Event;
import com.example.lotterize.FakeEventsRegisteredViewModel;
import com.example.lotterize.R;
import com.example.lotterize.TestEventFactory;
import com.example.lotterize.ui.eventsRegistered.EventsRegisteredFragment;


import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventRegisteredFragmentUITest {

    @Test
    public void testListDisplayAndButtons() {


        FakeEventsRegisteredViewModel fakeVm = new com.example.lotterize.FakeEventsRegisteredViewModel();

        FragmentScenario<EventsRegisteredFragment> scenario =
                FragmentScenario.launchInContainer(
                        EventsRegisteredFragment.class,
                        null,
                        R.style.Theme_Lotterize
                );

        scenario.onFragment(fragment -> {
            fragment.setViewModel(fakeVm);
        });

        // Create fake event list
        ArrayList<Event> events = new ArrayList<>();
        events.add(TestEventFactory.sample("E1", "Music Festival", "Calgary"));
        events.add(TestEventFactory.sample("E2", "Robotics Expo", "Edmonton"));

        // Push into LiveData
        fakeVm.emit(events);

        // Validate UI elements

        // Name of event 1 is shown
        onView(withText("Music Festival")).check(matches(isDisplayed()));

        // Name of event 2 is shown
        onView(withText("Robotics Expo")).check(matches(isDisplayed()));

        // Validate Accept button is visible
        onView(withId(R.id.accept_button))
                .check(matches(isDisplayed()));

        // Click Accept to ensure it’s interactable
        onView(withId(R.id.accept_button))
                .perform(click());

        // Validate Decline button is visible
        onView(withId(R.id.decline_button))
                .check(matches(isDisplayed()));

        // Click Decline
        onView(withId(R.id.decline_button))
                .perform(click());

        // Validate Status text is visible after updates
        onView(withId(R.id.status_text))
                .check(matches(isDisplayed()));
    }
}