package com.example.lotterize;


import com.google.firebase.Timestamp;
import java.util.ArrayList;

/**
 * Represents an event.
 * Contains all metadata about an event including owner, timing,
 * registration status lists, participant limits, and QR reference.
 *
 * Firestore requires a public no-argument constructor and public
 * getters/setters to properly deserialize objects.
 */
public class Event {

    private String eventId;
    private String ownerId;
    private ArrayList<String> waitList;
    private ArrayList<String> selectedList;
    private ArrayList<String> cancelledList;
    private ArrayList<String> finalList;
    private String eventName;
    private Timestamp date;
    private Timestamp registrationStart;
    private Timestamp registrationDeadline;
    private String location;
    private Long totalSpots;
    private String description;
    private Long entrantsLimit;
    private String qrCode;

    /**
     * Required empty constructor for Firestore.
     * DO NOT remove — Firestore uses reflection to populate fields.
     */
    public Event() {}


    /**
     * Full constructor for manually creating an Event.
     *
     * @param eventId Unique Firestore ID of the event (nullable until saved)
     * @param ownerId ID of organizer who created the event
     * @param waitList List of user IDs currently waiting
     * @param selectedList List of selected entrant IDs
     * @param cancelledList List of users who cancelled
     * @param finalList List of final confirmed users
     * @param eventName Display name of the event
     * @param date Timestamp when the event occurs
     * @param registrationStart Timestamp when sign-up opens
     * @param registrationDeadline Timestamp when sign-up closes
     * @param location Physical address or venue
     * @param totalSpots Maximum number of possible entrants
     * @param description Text description shown to users
     * @param entrantsLimit Limit on entries before selection/lottery
     * @param qrCode Reference string to QR image/data
     */
    public Event(String eventId, String ownerId,
                 ArrayList<String> waitList, ArrayList<String> selectedList,
                 ArrayList<String> cancelledList, ArrayList<String> finalList,
                 String eventName, Timestamp date, Timestamp registrationStart,
                 Timestamp registrationDeadline, String location,
                 long totalSpots, String description,
                 long entrantsLimit, String qrCode) {

        this.eventId = eventId;
        this.ownerId = ownerId;
        this.waitList = waitList;
        this.selectedList = selectedList;
        this.cancelledList = cancelledList;
        this.finalList = finalList;
        this.eventName = eventName;
        this.date = date;
        this.registrationStart = registrationStart;
        this.registrationDeadline = registrationDeadline;
        this.location = location;
        this.totalSpots = totalSpots;
        this.description = description;
        this.entrantsLimit = entrantsLimit;
        this.qrCode = qrCode;
    }

    /** @return Firestore ID of the event */
    public String getEventId() { return eventId; }
    /** @param eventId Firestore event document ID */
    public void setEventId(String eventId) { this.eventId = eventId; }

    /** @return ID of creator (organizer) */
    public String getOwnerId() { return ownerId; }
    /** @param ownerId Organizer's user ID */
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    /** @return List of users waiting */
    public ArrayList<String> getWaitList() { return waitList; }
    /** @param waitList Pending user IDs */
    public void setWaitList(ArrayList<String> waitList) { this.waitList = waitList; }

    /** @return List of selected users */
    public ArrayList<String> getSelectedList() { return selectedList; }
    /** @param selectedList Final selected user IDs */
    public void setSelectedList(ArrayList<String> selectedList) { this.selectedList = selectedList; }

    /** @return List of cancelled users */
    public ArrayList<String> getCancelledList() { return cancelledList; }
    /** @param cancelledList IDs of users who cancelled */
    public void setCancelledList(ArrayList<String> cancelledList) { this.cancelledList = cancelledList; }

    /** @return Fully accepted (final) user list */
    public ArrayList<String> getFinalList() { return finalList; }
    /** @param finalList Final approved participant IDs */
    public void setFinalList(ArrayList<String> finalList) { this.finalList = finalList; }

    /** @return The event’s displayed name */
    public String getEventName() { return eventName; }
    /** @param eventName Event display name */
    public void setEventName(String eventName) { this.eventName = eventName; }

    /** @return Timestamp when the event happens */
    public Timestamp getDate() { return date; }
    /** @param date New timestamp for event date */
    public void setDate(Timestamp date) { this.date = date; }

    /** @return Sign-up opening time */
    public Timestamp getRegistrationStart() { return registrationStart; }
    /** @param registrationStart New open time */
    public void setRegistrationStart(Timestamp registrationStart) { this.registrationStart = registrationStart; }

    /** @return Deadline to sign up */
    public Timestamp getRegistrationDeadline() { return registrationDeadline; }
    /** @param registrationDeadline New cutoff time */
    public void setRegistrationDeadline(Timestamp registrationDeadline) { this.registrationDeadline = registrationDeadline; }

    /** @return Event location (venue) */
    public String getLocation() { return location; }
    /** @param location New venue information */
    public void setLocation(String location) { this.location = location; }

    /** @return Maximum number of entrants */
    public long getTotalSpots() { return totalSpots; }
    /** @param totalSpots Updated capacity */
    public void setTotalSpots(long totalSpots) { this.totalSpots = totalSpots; }

    /** @return Event description text */
    public String getDescription() { return description; }
    /** @param description Updated event description */
    public void setDescription(String description) { this.description = description; }

    /** @return Maximum entrants allowed before selection */
    public long getEntrantsLimit() { return entrantsLimit; }
    /** @param entrantsLimit New entrant limit */
    public void setEntrantsLimit(long entrantsLimit) { this.entrantsLimit = entrantsLimit; }

    /** @return QR code identifier associated with event */
    public String getQrCode() { return qrCode; }
    /** @param qrCode Reference to event QR code */
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}