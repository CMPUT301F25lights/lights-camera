package com.example.lotterize;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LotteryControllerTest {

    private FirebaseFirestore mockDb;
    private CollectionReference mockCollection;
    private DocumentReference mockDocument;
    private LotteryController controller;
    private Event event;

    @Before
    public void setUp() {
        // Mocks
        mockDb = mock(FirebaseFirestore.class);
        mockCollection = mock(CollectionReference.class);
        mockDocument = mock(DocumentReference.class);

        when(mockDb.collection("events")).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);

        // ✅ Mock Lottery — no real Firebase/Android code runs
        Lottery mockLottery = mock(Lottery.class);
        when(mockLottery.drawWinners(any())).thenReturn(List.of("u1")); // optional

        // Controller under test
        controller = new LotteryController(mockDb, mockLottery);

        // Test event setup
        event = new Event();
        event.setEventId("E1");
        event.setSelectedList(new ArrayList<>(List.of("u1", "u2")));
        event.setFinalList(new ArrayList<>());
        event.setCancelledList(new ArrayList<>());
        event.setWaitList(new ArrayList<>(List.of("w1", "w2")));
    }

    @Test
    public void testAcceptInvitationMovesUserToFinalList() {
        controller.acceptInvitation(event, "u1");

        assertFalse(event.getSelectedList().contains("u1"));
        assertTrue(event.getFinalList().contains("u1"));

        verify(mockCollection).document("E1");
        // Optionally verify DB set/update call if implemented
        // verify(mockDocument).set(any());
    }

    @Test
    public void testDeclineInvitationMovesUserToCancelledAndAddsFromWaitlist() {
        controller.declineInvitation(event, "u2");

        assertFalse(event.getSelectedList().contains("u2"));
        assertTrue(event.getCancelledList().contains("u2"));

        // One replacement should be drawn from waitList
        assertTrue(event.getSelectedList().contains("w1") || event.getSelectedList().contains("w2"));
        assertEquals(2, event.getSelectedList().size()); // only one replacement added (u1 + w1)
        assertEquals(1, event.getWaitList().size()); // one removed from waitlist (just w2)

        verify(mockCollection).document("E1");
    }

    @Test
    public void testDeclineInvitationWithEmptyWaitlist() {
        event.getWaitList().clear();
        controller.declineInvitation(event, "u1");

        assertTrue(event.getCancelledList().contains("u1"));
        assertFalse(event.getSelectedList().contains("u1"));
        assertTrue(event.getSelectedList().contains("u2")); // still there
        assertEquals(1, event.getSelectedList().size());
    }

    @Test
    public void testAcceptInvitationWithInvalidUserDoesNothing() {
        controller.acceptInvitation(event, "nonexistent");

        assertEquals(2, event.getSelectedList().size());
        assertTrue(event.getFinalList().isEmpty());
    }
}