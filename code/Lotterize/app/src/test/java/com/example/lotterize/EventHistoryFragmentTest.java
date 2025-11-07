package com.example.lotterize;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Focuses on verifying correct event status classification
 * based on user presence in waitlist, selected, final, or cancelled lists.
 */
public class EventHistoryFragmentTest {

    private String currentUserId;

    @Before
    public void setUp() {
        currentUserId = "user123";
    }

    /**
     * Helper method replicating the event classification logic
     * from loadEventHistory() in EventHistoryFragment.
     */
    private String getUserEventStatus(String userId,
                                      List<String> waitList,
                                      List<String> selectedList,
                                      List<String> finalList,
                                      List<String> cancelledList) {

        if ((selectedList != null && selectedList.contains(userId)) ||
                (finalList != null && finalList.contains(userId))) {
            return "Was Selected";
        } else if (waitList != null && waitList.contains(userId)) {
            return "Was Not Selected";
        } else if (cancelledList != null && cancelledList.contains(userId)) {
            return "Cancelled";
        } else {
            return null;
        }
    }

    @Test
    public void testUserSelected_ReturnsWasSelected() {
        String status = getUserEventStatus(
                currentUserId,
                Collections.emptyList(),
                Arrays.asList("user123"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        assertEquals("Was Selected", status);
    }

    @Test
    public void testUserInFinalList_ReturnsWasSelected() {
        String status = getUserEventStatus(
                currentUserId,
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList("user123"),
                Collections.emptyList()
        );
        assertEquals("Was Selected", status);
    }

    @Test
    public void testUserInWaitlist_ReturnsWasNotSelected() {
        String status = getUserEventStatus(
                currentUserId,
                Arrays.asList("user123"),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        assertEquals("Was Not Selected", status);
    }

    @Test
    public void testUserCancelled_ReturnsCancelled() {
        String status = getUserEventStatus(
                currentUserId,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Arrays.asList("user123")
        );
        assertEquals("Cancelled", status);
    }

    @Test
    public void testUserNotInAnyList_ReturnsNull() {
        String status = getUserEventStatus(
                currentUserId,
                Arrays.asList("otherUser"),
                Arrays.asList("someoneElse"),
                Arrays.asList("finalPerson"),
                Arrays.asList("anotherUser")
        );
        assertNull(status);
    }

    @Test
    public void testPriority_SelectedOverridesWaitlist() {
        String status = getUserEventStatus(
                currentUserId,
                Arrays.asList("user123"),
                Arrays.asList("user123"),
                Collections.emptyList(),
                Collections.emptyList()
        );
        assertEquals("Was Selected", status);
    }
}
