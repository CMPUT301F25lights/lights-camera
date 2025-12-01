package com.example.lotterize;

import com.example.lotterize.ui.addEvents.EventsRepository;
import com.example.lotterize.ui.addEvents.UsersRepository;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;

import org.junit.After;
import org.junit.Before;

import java.util.ArrayList;

public abstract class BaseUITest {
    protected MockEventsRepository mockEventsRepository;
    protected MockUsersRepository mockUsersRepository;

    @Before
    public void setUp() {
        mockEventsRepository = new MockEventsRepository();
        EventsRepository.setInstance(mockEventsRepository);

        mockUsersRepository = new MockUsersRepository();
        UsersRepository.setInstance(mockUsersRepository);

        User mockUser = new User(
                "testUserId1",
                "Test User",
                "123-456-789",
                "test@ualberta.ca",
                null,
                "testUserName",
                "123"
        );

        ArrayList<String> ownedEventIds = new ArrayList<String>();
        ownedEventIds.add("testEventId1");
        mockUser.setOwnedEventIds(ownedEventIds);
        mockUser.setRegisteredEventIds(new ArrayList<>());
        mockUser.setWantNotification(true);
        CurrentUser.set(mockUser);
        mockEvent();
    }

    private void mockEvent(){
        Event event = new Event(
                "testEventId1",
                "testUserId1",
                new ArrayList<String>(),
                new ArrayList<String>(),
                new ArrayList<String>(),
                new ArrayList<String>(),
                "TestEvent",
                null,
                null,
                null,
                "Edmonton",
                10,
                "This event is used for testing",
                0,
                null,
                null,
                null,
                new ArrayList<>(),
                true,
                null
        );

        mockEventsRepository.addEvent(event);
    }

    @After
    public void tearDown() {
        EventsRepository.setInstance(new EventsRepository());
        EventsRepository.setInstance(new EventsRepository());
    }
}
