package com.example.lotterize;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lotterize.ui.eventsRegistered.EventsRegisteredFragment;
import androidx.fragment.app.testing.FragmentScenario;


import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventRegisteredFragmentUITest {

    @Rule
    public ActivityScenarioRule<TestHostActivity> activityRule =
            new ActivityScenarioRule<>(TestHostActivity.class);

    @Test
    public void testListDisplay() {
        // Prepare fake fragment
        EventsRegisteredFragment fragment = new EventsRegisteredFragment();

        // Inject your fake ViewModel here if needed
        FakeEventsRegisteredViewModel fakeVm = new FakeEventsRegisteredViewModel();
        ArrayList<Event> events = new ArrayList<>();
        events.add(TestEventFactory.sample("E1", "Music Festival", "Calgary"));
        events.add(TestEventFactory.sample("E2", "Robotics Expo", "Edmonton"));
        fakeVm.emit(events);

        // Launch fragment inside the host activity
        activityRule.getScenario().onActivity(activity -> {
            fragment.setViewModel(fakeVm);
            activity.setFragment(fragment);
        });

        // Now you can use Espresso to test UI elements
    }
}