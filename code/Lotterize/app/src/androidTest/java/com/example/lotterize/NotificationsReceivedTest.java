package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.lotterize.ui.admin.NotificationsReceivedActivity;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * UI tests for {@link NotificationsReceivedActivity}.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationsReceivedTest extends BaseUITest {

    private static final String TEST_USER_ID = "notificationsUser1";

    @Before
    public void setUpNotifications() {
        // Two received notifications for TEST_USER_ID
        ArrayList<String> receivers1 = new ArrayList<>();
        receivers1.add(TEST_USER_ID);
        Notification n1 = new Notification(
                "n1",
                "sender1",
                "Sender One",
                "Hello 1",
                (Timestamp) null,
                receivers1
        );

        ArrayList<String> receivers2 = new ArrayList<>();
        receivers2.add(TEST_USER_ID);
        Notification n2 = new Notification(
                "n2",
                "sender2",
                "Sender Two",
                "Hello 2",
                (Timestamp) null,
                receivers2
        );

        mockNotificationsRepository.addReceived(TEST_USER_ID, n1);
        mockNotificationsRepository.addReceived(TEST_USER_ID, n2);
    }

    @Test
    public void notificationsAreDisplayed_whenExist() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsReceivedActivity.class
        );
        intent.putExtra("userId", TEST_USER_ID);

        try (ActivityScenario<NotificationsReceivedActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withText("Hello 1")).check(matches(isDisplayed()));
            onView(withText("Hello 2")).check(matches(isDisplayed()));

            onView(withId(R.id.text_empty_state)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void emptyStateShown_whenNoNotifications() {
        String otherUserId = "noNotificationsUser";

        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsReceivedActivity.class
        );
        intent.putExtra("userId", otherUserId);

        try (ActivityScenario<NotificationsReceivedActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.text_empty_state))
                    .check(matches(isDisplayed()))
                    .check(matches(withText("No notifications received")));
        }
    }
}
