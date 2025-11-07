package com.example.lotterize;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LotteryControllerTest {

    private FirebaseFirestore mockDb;
    private CollectionReference mockCollection;
    private DocumentReference mockDocument;
    private LotteryController controller;
    private Event event;

    @BeforeEach
    public void setUp() {
        // Mock Firestore
        mockDb = mock(FirebaseFirestore.class);
        mockCollection = mock(CollectionReference.class);
        mockDocument = mock(DocumentReference.class);

        when(mockDb.collection("events")).thenReturn(mockCollection);
        when(mockCollection.document(anyString())).thenReturn(mockDocument);
        when(mockDocument.set(any(Event.class))).thenReturn(null);

        // Inject mock Firestore using reflection since it's final
        controller = new LotteryController() {
            {
                try {
                    java.lang.reflect.Field dbField = LotteryController.class.getDeclaredField("db");
                    dbField.setAccessible(true);
                    dbField.set(this, mockDb);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };

        // Mock Event
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

        verify(mockDb.collection("events")).document("E1");
    }

    @Test
    public void testDeclineInvitationMovesUserToCancelledAndAddsFromWaitlist() {
        controller.declineInvitation(event, "u2");

        // u2 should be removed and added to cancelled
        assertFalse(event.getSelectedList().contains("u2"));
        assertTrue(event.getCancelledList().contains("u2"));

        // first waitlist user (w1) should move into selected list
        assertTrue(event.getSelectedList().contains("w1"));
        assertFalse(event.getWaitList().contains("w1"));

        verify(mockCollection).document("E1");
    }

    @Test
    public void testDeclineInvitationWithEmptyWaitlist() {
        event.getWaitList().clear();
        controller.declineInvitation(event, "u1");

        assertTrue(event.getCancelledList().contains("u1"));
        assertTrue(event.getSelectedList().isEmpty());
    }

    @Test
    public void testAcceptInvitationWithInvalidUserDoesNothing() {
        controller.acceptInvitation(event, "nonexistent");

        assertEquals(2, event.getSelectedList().size());
        assertTrue(event.getFinalList().isEmpty());
    }
}