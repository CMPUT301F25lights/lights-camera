package com.example.lotterize;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.Before;
import org.junit.Test;

public class EventTest {
    @Test
    public void addEventDetailsFromSnapShotTest_AllFieldPresent(){
        DocumentSnapshot doc = mock(DocumentSnapshot.class);

        when(doc.getString("eventId")).thenReturn("Event_Id_1");
        when(doc.getString("ownerId")).thenReturn("Owner_Id_1");
        when(doc.getString("eventName")).thenReturn("My Event");
        when(doc.getString("location")).thenReturn("Edmonton");
        when(doc.getString("description")).thenReturn("No description");
        //when(doc.getString("qrCode")).thenReturn("QR_123");

        // timestamps
        Timestamp date = new Timestamp(123, 0);
        Timestamp registrationStart = new Timestamp(124, 0);
        Timestamp registrationDeadline = new Timestamp(125, 0);
        when(doc.getTimestamp("date")).thenReturn(date);
        when(doc.getTimestamp("registrationStart")).thenReturn(registrationStart);
        when(doc.getTimestamp("registrationDeadline")).thenReturn(registrationDeadline);

        when(doc.getLong("totalSpots")).thenReturn(100L);
        when(doc.getLong("entrantsLimit")).thenReturn(101L);

        when(doc.get("waitList")).thenReturn(new ArrayList<>(Arrays.asList("AttendeeA","AttendeeB")));
        when(doc.get("selectedList")).thenReturn(new ArrayList<>(Arrays.asList("AttendeeA","AttendeeB")));
        when(doc.get("cancelledList")).thenReturn(new ArrayList<>(List.of("AttendeeB")));
        when(doc.get("finalList")).thenReturn(new ArrayList<>(List.of("AttendeeA")));


        Event event = Event.addEventDetailsFromSnapShot(doc);

        assertEquals("Event_Id_1", event.getEventId());
        assertEquals("Owner_Id_1", event.getOwnerId());
        assertEquals("My Event", event.getEventName());
        assertEquals("Edmonton", event.getLocation());
        assertEquals("No description", event.getDescription());
        //assertEquals("QR_123", event.getQrCode());
        assertEquals(date, event.getDate());
        assertEquals(registrationStart, event.getRegistrationStart());
        assertEquals(registrationDeadline, event.getRegistrationDeadline());
        assertEquals(100, event.getTotalSpots());
        assertEquals(101, event.getEntrantsLimit());
        assertEquals(Arrays.asList("AttendeeA","AttendeeB"), event.getWaitList());
        assertEquals(Arrays.asList("AttendeeA","AttendeeB"), event.getSelectedList());
        assertEquals(Arrays.asList("AttendeeB"), event.getCancelledList());
        assertEquals(Arrays.asList("AttendeeA"), event.getFinalList());
    }

    @Test
    public void addEventDetailsFromSnapShotTest_MissingFields(){
        DocumentSnapshot doc = mock(DocumentSnapshot.class);

        when(doc.getString("eventId")).thenReturn("Event_Id_1");
        when(doc.getString("ownerId")).thenReturn("Owner_Id_1");

        when(doc.get("waitList")).thenReturn(null);
        when(doc.get("selectedList")).thenReturn(null);
        when(doc.get("cancelledList")).thenReturn(null);
        when(doc.get("finalList")).thenReturn(null);


        Event event = Event.addEventDetailsFromSnapShot(doc);

        assertEquals("Event_Id_1", event.getEventId());
        assertEquals("Owner_Id_1", event.getOwnerId());

        assertNull(event.getEventName());
        assertNull(event.getLocation());
        assertNull(event.getDescription());
        //assertNull(event.getQrCode());

        assertEquals(0L, event.getTotalSpots());
        assertEquals(0L, event.getEntrantsLimit());

        assertNotNull(event.getWaitList());
        assertTrue(event.getWaitList().isEmpty());

        assertNotNull(event.getSelectedList());
        assertTrue(event.getSelectedList().isEmpty());

        assertNotNull(event.getCancelledList());
        assertTrue(event.getCancelledList().isEmpty());

        assertNotNull(event.getFinalList());
        assertTrue(event.getFinalList().isEmpty());
    }
}
