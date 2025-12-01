package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;

import static java.util.EnumSet.allOf;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.rule.IntentsRule;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class AdminEventsTest {

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
        ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(AdminActivity.class);
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
    public void adminSearchEvent(){
        CountDownLatch latch = new CountDownLatch(1);
        testDoc.get().addOnSuccessListener(documentSnapshot -> {latch.countDown();});
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.admin_search_bar)).perform(typeText("Test Event"), closeSoftKeyboard());
        onView(withId(R.id.recycler_admin_events)).check(matches(hasDescendant(withText("Test Event ABCD"))));
        onView(withId(R.id.admin_search_bar)).perform(replaceText("P"), closeSoftKeyboard());
        onView(withId(R.id.recycler_admin_events)).check(matches(not(hasDescendant(withText("Test Event ABCD")))));
    }

    @Test
    public void adminDeleteEventFromDetails(){
        CountDownLatch latch = new CountDownLatch(1);
        testDoc.get().addOnSuccessListener(documentSnapshot -> {latch.countDown();});
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.admin_search_bar)).perform(typeText("Test Event ABCD"), closeSoftKeyboard());
        onView(withText("View Details")).perform(click());
        onView(withId(R.id.see_waitList)).perform(click());
        onView(withText("YES")).perform((click()));
        testDoc.get().addOnSuccessListener(documentSnapshot -> {latch.countDown();});
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.admin_search_bar)).perform(replaceText("Test Event ABCD"), closeSoftKeyboard());
        onView(withText("View Details")).check(doesNotExist());
    }

    @Test
    public void adminDeleteFromHomePage(){
        CountDownLatch latch = new CountDownLatch(1);
        testDoc.get().addOnSuccessListener(documentSnapshot -> {latch.countDown();});
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.admin_search_bar)).perform(typeText("Test Event ABCD"), closeSoftKeyboard());
        onView(withText("Delete")).perform(click());
        onView(withText("YES")).perform((click()));
        onView(withId(R.id.admin_search_bar)).perform(replaceText("Test Event ABCD"), closeSoftKeyboard());
        onView(withText("View Details")).check(doesNotExist());
    }



}
