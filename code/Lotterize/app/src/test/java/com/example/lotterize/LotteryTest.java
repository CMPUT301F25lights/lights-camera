package com.example.lotterize;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the Lottery class.
 * Static mocking is used for CurrentUser.get(), since it is Android-dependent,
 * and did not want to touch DB yet.
 */
public class LotteryTest {

    private Event testEvent;
    private Lottery lottery;
    private User mockUser;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Prepare a test event with 5 people in waitList and 2 total spots
        testEvent = new Event(
                "event1",
                "owner1",
                new ArrayList<>(Arrays.asList("u1", "u2", "u3", "u4", "u5")),
                new ArrayList<>(),     // selectedList
                new ArrayList<>(),     // cancelledList
                new ArrayList<>(),     // finalList
                "Music Festival",
                null,
                null,
                null,
                "Campus Park",
                2L,                    // totalSpots
                "Outdoor event",
                10L,                   // entrantsLimit
                "qr123",
                "",                    // imageUrl
                "",                    // imagePath
                new ArrayList<>(),     // filtersList
                false,                 // geolocationEnabled
                new java.util.HashMap<>()  // user locations
        );

        // Mock NotificationSender and Lottery
        NotificationSender mockSender = mock(NotificationSender.class);
        lottery = new Lottery(mockSender);

        // Mock CurrentUser for all tests
        mockUser = mock(User.class);
        when(mockUser.getUserId()).thenReturn("test-user-id");
    }

    /** Utility wrapper to run code with CurrentUser.get() mocked. */
    private void withMockedCurrentUser(Runnable testLogic) {
        try (MockedStatic<CurrentUser> mockedStatic = Mockito.mockStatic(CurrentUser.class)) {
            mockedStatic.when(CurrentUser::get).thenReturn(mockUser);
            testLogic.run();
        }
    }

    /**
     * Test that the correct number of winners are drawn
     * and moved from waitList to selectedList/finalList.
     */
    @Test
    public void testDrawWinners_SelectsCorrectNumber() {
        withMockedCurrentUser(() -> {
            List<String> winners = lottery.drawWinners(testEvent);

            assertEquals(2, winners.size());
            assertEquals(2, testEvent.getSelectedList().size());
            //assertEquals(2, testEvent.getFinalList().size());
            assertEquals(3, testEvent.getWaitList().size());

            for (String w : winners) {
                assertFalse(testEvent.getWaitList().contains(w));
            }
        });
    }

    /**
     * Test that no winners are drawn when the waitlist is empty.
     */
    @Test
    public void testDrawWinners_EmptyWaitList() {
        withMockedCurrentUser(() -> {
            Event emptyEvent = new Event(
                    "event2",
                    "owner2",
                    new ArrayList<>(),   // waitList
                    new ArrayList<>(),   // selectedList
                    new ArrayList<>(),   // cancelledList
                    new ArrayList<>(),   // finalList
                    "Empty Event",
                    null,
                   null,
                    null,
                    "Venue",
                    3L,          // totalSpots
                    "No participants",
                    10L,         // entrantsLimit
                    "qr456",
                    "",          // imageUrl
                    "",          // imagePath
                    new ArrayList<>(),   // filtersList
                    false,               // geolocationEnabled
                    new java.util.HashMap<>()  // user locations
            );

            List<String> winners = lottery.drawWinners(emptyEvent);
            assertTrue(winners.isEmpty());
            assertTrue(emptyEvent.getSelectedList().isEmpty());
            assertTrue(emptyEvent.getFinalList().isEmpty());
        });
    }

    /**
     * Test that drawReplacement correctly selects a user from waitList
     * and moves them to selected/final lists.
     */
    @Test
    public void testDrawReplacement_SelectsOne() {
        withMockedCurrentUser(() -> {
            lottery.drawWinners(testEvent);
            int initialWaitSize = testEvent.getWaitList().size();

            String replacement = lottery.drawReplacement(testEvent);

            assertNotNull(replacement);
            assertTrue(testEvent.getSelectedList().contains(replacement));
            //assertTrue(testEvent.getFinalList().contains(replacement));
            assertEquals(initialWaitSize - 1, testEvent.getWaitList().size());
        });
    }

    /**
     * Test that drawReplacement returns null if no one is left in waitList.
     */
    @Test
    public void testDrawReplacement_EmptyWaitList() {
        withMockedCurrentUser(() -> {
            testEvent.getWaitList().clear();
            String replacement = lottery.drawReplacement(testEvent);
            assertNull(replacement);
        });
    }

    /**
     * Test that multiple calls to drawWinners never exceed totalSpots or waitList size.
     */
    @Test
    public void testDrawWinners_NeverExceedsLimits() {
        withMockedCurrentUser(() -> {
            List<String> winners1 = lottery.drawWinners(testEvent);
            List<String> winners2 = lottery.drawWinners(testEvent);

            // Total winners should never exceed totalSpots
            assertTrue(testEvent.getSelectedList().size() <= testEvent.getTotalSpots());
            assertTrue(testEvent.getFinalList().size() <= testEvent.getTotalSpots());

            // No duplicates across draws
            assertEquals(
                    testEvent.getSelectedList().size(),
                    testEvent.getSelectedList().stream().distinct().count()
            );
        });
    }

    /** Local stub class for notification verification (optional). */
    private static class MockNotificationSender extends NotificationSender {
        private boolean called = false;
        private String lastMessage;

        @Override
        public void sendNotification(String senderId, String message, ArrayList<String> receiverIds) {
            called = true;
            lastMessage = message;
        }

        public boolean wasCalled() { return called; }
        public String getLastMessage() { return lastMessage; }
    }
}