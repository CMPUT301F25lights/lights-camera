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

import com.example.lotterize.ui.admin.NotificationsSentActivity;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * UI tests for {@link NotificationsSentActivity}.
 */
@RunWith(AndroidJUnit4.class)
public class NotificationsSentTest extends BaseUITest {

    private static final String TEST_USER_ID = "notificationsSender1";

    @Before
    public void setUpNotifications() {
        // Two sent notifications from TEST_USER_ID
        ArrayList<String> receivers = new ArrayList<>();
        receivers.add("receiver1");

        Notification n1 = new Notification(
                "s1",
                TEST_USER_ID,
                "Sender Name",
                "Sent message 1",
                (Timestamp) null,
                receivers
        );

        Notification n2 = new Notification(
                "s2",
                TEST_USER_ID,
                "Sender Name",
                "Sent message 2",
                (Timestamp) null,
                receivers
        );

        mockNotificationsRepository.addSent(TEST_USER_ID, n1);
        mockNotificationsRepository.addSent(TEST_USER_ID, n2);
    }

    @Test
    public void sentNotificationsAreDisplayed_whenExist() {
        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsSentActivity.class
        );
        intent.putExtra("userId", TEST_USER_ID);

        try (ActivityScenario<NotificationsSentActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withText("Sent message 1")).check(matches(isDisplayed()));
            onView(withText("Sent message 2")).check(matches(isDisplayed()));

            onView(withId(R.id.text_empty_state)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void emptyStateShown_whenNoSentNotifications() {
        String otherUserId = "noSentNotificationsUser";

        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                NotificationsSentActivity.class
        );
        intent.putExtra("userId", otherUserId);

        try (ActivityScenario<NotificationsSentActivity> scenario =
                     ActivityScenario.launch(intent)) {

            onView(withId(R.id.text_empty_state))
                    .check(matches(isDisplayed()))
                    .check(matches(withText("No notifications sent")));
        }
    }
}
