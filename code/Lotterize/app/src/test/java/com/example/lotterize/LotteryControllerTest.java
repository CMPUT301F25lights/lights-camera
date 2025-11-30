package com.example.lotterize;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import java.util.Random;

public class LotteryControllerTest {

    private FirebaseFirestore mockDb;
    private CollectionReference mockCollection;
    private DocumentReference mockDocument;
    private LotteryController controller;
    private Event event;
    private Random random;

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

        // When drawReplacement is called, pick the first from waitlist
        when(mockLottery.drawReplacement(any())).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            if (!e.getWaitList().isEmpty()) {
                String next = e.getWaitList().remove(0);
                e.getSelectedList().add(next);
                return next;
            }
            return null;
        });

        // Controller under test
        controller = new LotteryController(mockDb, mockLottery);

        // Test event setup
        event = new Event();
        event.setEventId("E1");
        event.setSelectedList(new ArrayList<>(List.of("u1", "u2")));
        event.setFinalList(new ArrayList<>());
        event.setCancelledList(new ArrayList<>());
        event.setWaitList(new ArrayList<>(List.of("w1", "w2")));
        event.setTotalSpots(2);
    }

    @Test
    public void testAcceptInvitationMovesUserToFinalList() {
        controller.acceptInvitation(event, "u1");

        assertTrue(event.getSelectedList().contains("u1"));
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
    @Test
    public void testAcceptWithReplacementDoesNotOverfillFinalList() {

        FirebaseFirestore mockDb = mock(FirebaseFirestore.class);
        CollectionReference mockCollection = mock(CollectionReference.class);
        DocumentReference mockDocument = mock(DocumentReference.class);

        // Your test creates an Event later — set ID here:
        String eventId = "E1";

        // Firestore mocks must match EXACT calls
        when(mockDb.collection("events")).thenReturn(mockCollection);
        when(mockCollection.document(eventId)).thenReturn(mockDocument);

        // Mock Firestore .set() chain
        Task<Void> mockTask = mock(Task.class);
        when(mockDocument.set(any())).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any())).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any())).thenReturn(mockTask);

        // Mock lottery replacement behavior
        Lottery mockLottery = mock(Lottery.class);

        when(mockLottery.drawReplacement(any())).thenAnswer(invocation -> {
            Event e = invocation.getArgument(0);
            ArrayList<String> waitList = e.getWaitList();
            ArrayList<String> selectedList = e.getSelectedList();
            ArrayList<String> finalList = e.getFinalList();

            if (waitList == null || waitList.isEmpty()) {
                return null;
            }

            int totalSpots = (int) e.getTotalSpots();

            // No open spots → no replacement allowed
            if (selectedList.size() >= totalSpots) {
                return null;
            }

            // shuffle for fairness
            //Collections.shuffle(waitList, random);

            // Pick the first after shuffle
            String replacement = waitList.get(0);

            // Update
            waitList.remove(0);
            selectedList.add(replacement);
            return null;
        });


        LotteryController controller = new LotteryController(mockDb, mockLottery);

        // --- Setup event ---
        Event event = new Event();
        event.setEventId(eventId);   // required for document("E1")
        event.setTotalSpots(2L);
        event.setSelectedList(new ArrayList<>(List.of("userA", "userB")));
        event.setFinalList(new ArrayList<>());
        event.setWaitList(new ArrayList<>(List.of("userC", "userD")));
        event.setCancelledList(new ArrayList<>());


        // Accept userA
        controller.acceptInvitation(event, "userA");
        assertEquals(1, event.getFinalList().size());
        assertEquals(2, event.getSelectedList().size());
        controller.declineInvitation(event, "userB");

        System.out.println("Final List: " + event.getFinalList());
        System.out.println("Selected List: " + event.getSelectedList());
        System.out.println("Wait List: " + event.getWaitList());
        System.out.println("Cancelled List: " + event.getCancelledList());

        // Decline userB → triggers replacement userC

        controller.acceptInvitation(event, "userC");


        // --- Assertions ---
        assertEquals(2, event.getFinalList().size());
        assertTrue(event.getSelectedList().containsAll(event.getFinalList()));
        assertEquals(1, event.getWaitList().size());
        assertTrue(event.getFinalList().size() <= event.getTotalSpots());
    }
}