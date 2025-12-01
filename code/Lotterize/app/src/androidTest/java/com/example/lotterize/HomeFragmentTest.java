package com.example.lotterize;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static androidx.test.espresso.matcher.ViewMatchers.hasChildCount;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static com.example.lotterize.ui.home.EventDetailsActivity.showingEvent;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;

import android.view.View;
import android.widget.CalendarView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.rule.IntentsRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lotterize.ui.home.EventDetailsActivity;
import com.example.lotterize.ui.home.ShowWaitingListActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.WriteBatch;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class HomeFragmentTest {

    @Rule
    public IntentsRule intentsRule = new IntentsRule();

    User currUser;

    User usrA;
    private FirebaseFirestore db;
    private CollectionReference eventsCol;

    CollectionReference usersCol;

    private DocumentReference testDoc;

    String testEventId;

    private void makeEvent(){
        CountDownLatch latch = new CountDownLatch(1);
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Timestamp now = new Timestamp(c.getTime());
        c.add(Calendar.DAY_OF_MONTH, 8);
        Timestamp regDeadline = new Timestamp(c.getTime());
        ArrayList<String> waitList = new ArrayList<>();
        ArrayList<String> selectedList = new ArrayList<>();
        ArrayList<String> cancelledList = new ArrayList<>();
        ArrayList<String> finalList = new ArrayList<>();
        ArrayList<String> filtersList = new ArrayList<>();
        waitList.add("id2");
        filtersList.add("Test Filter");
        java.util.Map<String, GeoPoint> userLocations = new java.util.HashMap<>();
        userLocations.put("user1", new GeoPoint(53.5461, -113.4938));

        usrA = new User("id2", "name2", "phone number2", "email2", "coordinates2", "username2", "password2");
        currUser = new User("id", "name", "phone number", "email", "coordinates", "username", "password");
        usersCol.add(currUser);
        usersCol.add(usrA);

        eventsCol.add(new Event()).addOnSuccessListener(documentReference -> {
            testEventId = documentReference.getId();
            testDoc = documentReference;
            Event testEvent = new Event(
                    testEventId,
                    "owner456",
                    waitList,
                    selectedList,
                    cancelledList,
                    finalList,
                    "Test Event ABCD",
                    regDeadline,
                    now,
                    regDeadline,
                    "Edmonton",
                    100,
                    "This is a test event.",
                    101,
                    "qr123",
                    "https://example.com/img.jpg",
                    "images/event123.jpg",
                    filtersList,
                    false,
                    userLocations
            );
            testDoc.set(testEvent).addOnSuccessListener( ref -> {
                testDoc.get().addOnSuccessListener(ref2 -> {
                    assertNotNull(ref2.get("eventId"));
                    latch.countDown();
                });
            });

        });
        try {
            latch.await(1000, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void setup() {
        db = FirebaseFirestore.getInstance();
        eventsCol = db.collection("events");
        usersCol = db.collection(("users"));
        makeEvent();
        CurrentUser.set(currUser);
        assertNotNull("testDoc should not be null after makeEvent()", testDoc);
        ActivityScenario<UserActivity> scenario = ActivityScenario.launch(UserActivity.class);
    }

    @After
    public void delete() {
        CountDownLatch eventsLatch = new CountDownLatch(1);

        eventsCol.whereEqualTo("eventName", "Test Event ABCD").get().addOnSuccessListener(snap -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnCompleteListener(t -> eventsLatch.countDown());
                })
                .addOnFailureListener(e -> eventsLatch.countDown());

        try {
            eventsLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        CountDownLatch usersLatch = new CountDownLatch(1);

        usersCol.whereIn("userId", Arrays.asList("id", "id2")).get().addOnSuccessListener(snap -> {
                    WriteBatch batch = db.batch();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnCompleteListener(t -> usersLatch.countDown());
                })
                .addOnFailureListener(e -> usersLatch.countDown());

        try {
            usersLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInfoButton() {
        onView(withId(R.id.info_button)).perform(click());
        onView(withId(R.id.info_text)).check(matches(isDisplayed()));
    }

    @Test
    public void filterEvents(){
        onView(withId(R.id.filter_events_button)).perform(click());
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onView(allOf(
                isDescendantOfA(withId(R.id.filter_list)),
                withText("Test Filter")
        )).perform(click());
        onView(withText("OK")).perform(click());
        onView(withId(R.id.events_list))
                .check(matches(hasDescendant(withText("Test Event ABCD"))));
    }

    public static ViewAction setDate(int year, int month, int day) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(CalendarView.class);
            }

            @Override
            public String getDescription() {
                return "Set date on CalendarView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, day);
                ((CalendarView) view).setDate(cal.getTimeInMillis(), true, true);
            }
        };
    }

    @Test
    public void searchEvent(){
        onView(withId(R.id.search_bar)).perform(typeText("T"), closeSoftKeyboard());
        onView(withId(R.id.events_list)).check(matches(hasDescendant(withText("Test Event ABCD"))));
        onView(withId(R.id.search_bar)).perform(replaceText("P"), closeSoftKeyboard());
        onView(withId(R.id.events_list)).check(matches(not(hasDescendant(withText("Test Event ABCD")))));
    }

    @Test
    public void seeEventDetails(){
        ArrayList<String> expectedOutput = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        testDoc.get().addOnSuccessListener(documentSnapshot -> {
            Timestamp date = (Timestamp) documentSnapshot.get("date");
            Timestamp regDeadline = (Timestamp) documentSnapshot.get("registrationDeadline");
            String location = "Edmonton";
            String description = "This is a test event.";
            String entrantsText = "1 (100 Total Spots)";
            SimpleDateFormat dateFormat = new SimpleDateFormat("h:mma 'on' dd/MM/yyyy");

            expectedOutput.add(dateFormat.format(date.toDate()).toLowerCase());
            expectedOutput.add(dateFormat.format(regDeadline.toDate()).toLowerCase());
            expectedOutput.add(location);
            expectedOutput.add(entrantsText);
            expectedOutput.add(description);

            latch.countDown();
        });

        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .onChildView(withId(R.id.event_details_button))
                .perform(click());
        intended(allOf(hasComponent(EventDetailsActivity.class.getName()), hasExtra("eventId", testEventId)));

        IdlingRegistry.getInstance().register(showingEvent);

        onView(withId(R.id.date_text)).check(matches(withText(expectedOutput.get(0))));
        onView(withId(R.id.deadline_text)).check(matches(withText(expectedOutput.get(1))));
        onView(withId(R.id.location_text)).check(matches(withText(expectedOutput.get(2))));
        onView(withId(R.id.entrants_text)).check(matches(withText(expectedOutput.get(3))));
        onView(withId(R.id.desc_text)).check(matches(withText(expectedOutput.get(4))));
    }

    @Test
    public void seeWaitList(){
        CountDownLatch latch = new CountDownLatch(1);
        testDoc.get().addOnSuccessListener(documentSnapshot -> {latch.countDown();});
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .onChildView(withId(R.id.event_details_button))
                .perform(click());
        intended(allOf(hasComponent(EventDetailsActivity.class.getName()), hasExtra("eventId", testEventId)));
        IdlingRegistry.getInstance().register(showingEvent);
        onView(withId(R.id.see_waitList)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intended(allOf(hasComponent(ShowWaitingListActivity.class.getName()), hasExtra("eventId", testEventId)));

        onData(anything())
                .inAdapterView(withId(R.id.listView_showList))
                .atPosition(0).onChildView(withId(R.id.user_name_text))
                .check(matches(withText("name2 (ID: id2)")));
    }

    @Test
    public void joinWaitList(){
        CountDownLatch latch = new CountDownLatch(1);
        testDoc.get().addOnSuccessListener(documentSnapshot -> {latch.countDown();});
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .onChildView(withId(R.id.event_details_button))
                .perform(click());
        intended(allOf(hasComponent(EventDetailsActivity.class.getName()), hasExtra("eventId", testEventId)));
        IdlingRegistry.getInstance().register(showingEvent);
        onView(withId(R.id.see_waitList)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intended(allOf(hasComponent(ShowWaitingListActivity.class.getName()), hasExtra("eventId", testEventId)));

        onView(withId(R.id.interactButton)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CountDownLatch latch2 = new CountDownLatch(1);
        testDoc.get().addOnSuccessListener(documentSnapshot -> {latch2.countDown();});
        try {
            latch2.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onData(anything())
                .inAdapterView(withId(R.id.listView_showList))
                .atPosition(1).onChildView(withId(R.id.user_name_text))
                .check(matches(withText("name (ID: id)")));

        onView(withId(R.id.back)).perform(click());
        onView(withId(R.id.event_details_return)).perform(click());
        onView(withId(R.id.waitlisted_events_button)).perform(click());

        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0).onChildView(withId(R.id.event_name)).check(matches(withText("Test Event ABCD")));
    }

    @Test
    public void leaveWaitlist(){
        CountDownLatch latch = new CountDownLatch(1);
        testDoc.get().addOnSuccessListener(documentSnapshot -> {latch.countDown();});
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> waitList = new ArrayList<>();
        waitList.add("id2");
        waitList.add("id");
        testDoc.update("waitList", waitList);

        onData(anything())
                .inAdapterView(withId(R.id.events_list))
                .atPosition(0)
                .onChildView(withId(R.id.event_details_button))
                .perform(click());
        intended(allOf(hasComponent(EventDetailsActivity.class.getName()), hasExtra("eventId", testEventId)));
        IdlingRegistry.getInstance().register(showingEvent);
        onView(withId(R.id.see_waitList)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        intended(allOf(hasComponent(ShowWaitingListActivity.class.getName()), hasExtra("eventId", testEventId)));
        CountDownLatch latch2 = new CountDownLatch(1);
        testDoc.get().addOnSuccessListener(documentSnapshot -> {latch2.countDown();});
        try {
            latch2.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.interactButton)).perform(click());
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        onView(withId(R.id.listView_showList)).check(matches(hasChildCount(1)));
        onData(anything())
                .inAdapterView(withId(R.id.listView_showList))
                .atPosition(0).onChildView(withId(R.id.user_name_text))
                .check(matches(not(withText("name (ID: id)"))));

        onView(withId(R.id.back)).perform(click());
        onView(withId(R.id.event_details_return)).perform(click());
        onView(withId(R.id.waitlisted_events_button)).perform(click());
        onView(withId(R.id.events_list)).check(matches(hasChildCount(0)));
    }

}
