package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminProfileFragmentTest extends BaseUITest {

    @Before
    public void setUpUsersForAdminProfile() {
        // Seed mock users that AdminProfileFragment should display
        mockUsersRepository.setName("user1", "Alice");
        mockUsersRepository.setName("user2", "Bob");
        mockUsersRepository.setName("user3", "Charlie");
    }

    private ActivityScenario<AdminActivity> launchAdminProfileScreen() {
        ActivityScenario<AdminActivity> scenario =
                ActivityScenario.launch(AdminActivity.class);

        scenario.onActivity(activity -> {
            NavController navController =
                    Navigation.findNavController(activity, R.id.nav_host_fragment_activity_admin);
            navController.navigate(R.id.navigation_admin_profile);
        });

        return scenario;
    }

    @Test
    public void testUsersAreDisplayed() {
        ActivityScenario<AdminActivity> scenario = launchAdminProfileScreen();

        // All three mock users should be shown initially
        onView(withText("Alice")).check(matches(isDisplayed()));
        onView(withText("Bob")).check(matches(isDisplayed()));
        onView(withText("Charlie")).check(matches(isDisplayed()));

        scenario.close();
    }

    @Test
    public void testSearchFiltersUsers() {
        ActivityScenario<AdminActivity> scenario = launchAdminProfileScreen();

        onView(withId(R.id.search_profiles))
                .perform(clearText(), typeText("Ali"), closeSoftKeyboard());

        onView(withText("Alice")).check(matches(isDisplayed()));

        onView(withText("Bob")).check(doesNotExist());
        onView(withText("Charlie")).check(doesNotExist());

        scenario.close();
    }
}
