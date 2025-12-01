package com.example.lotterize;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertTrue;
import static java.util.regex.Pattern.matches;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.lotterize.AdminActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminProfileFragmentTest {

    private FirebaseFirestore db;
    private CollectionReference usersCol;

    @Before
    public void setup() throws Exception {
        db = FirebaseFirestore.getInstance();
        usersCol = db.collection("users");

        // Clean previous test users
        CountDownLatch wipeLatch = new CountDownLatch(1);
        usersCol.whereIn("userId", Arrays.asList("testUser1", "testUser2"))
                .get()
                .addOnSuccessListener(snap -> {
                    WriteBatch batch = db.batch();
                    for (QueryDocumentSnapshot doc : snap) batch.delete(doc.getReference());
                    batch.commit().addOnSuccessListener(v -> wipeLatch.countDown());
                });
        wipeLatch.await(3, TimeUnit.SECONDS);

        // Insert test users
        CountDownLatch insertLatch = new CountDownLatch(2);
        Map<String, Object> u1 = new HashMap<>();
        u1.put("userId", "testUser1");
        u1.put("username", "User One");
        Map<String, Object> u2 = new HashMap<>();
        u2.put("userId", "testUser2");
        u2.put("username", "User Two");
        usersCol.document("testUser1").set(u1).addOnSuccessListener(v -> insertLatch.countDown());
        usersCol.document("testUser2").set(u2).addOnSuccessListener(v -> insertLatch.countDown());
        insertLatch.await(3, TimeUnit.SECONDS);
    }

    @Test
    public void testDeleteUser_fromAdminProfileFragment() throws InterruptedException {
        // Launch AdminActivity
        ActivityScenario<AdminActivity> scenario = ActivityScenario.launch(AdminActivity.class);

        // Navigate to AdminProfileFragment
        onView(withId(R.id.nav_view_admin))
                .perform(click()); // adjust if you have a menu item or button to go to the fragment
        // Or use Navigation component:
        scenario.onActivity(activity -> {
            NavController navController = Navigation.findNavController(
                    activity, R.id.nav_host_fragment_activity_admin);
            navController.navigate(R.id.navigation_admin_profile);
        });

        // Wait for Firestore to load the user
        onView(isRoot()).perform(waitForView(withText("name2"), 5000));

        // Verify the user exists in the list
        onView(allOf(withText("name2"))).perform(scrollTo());

        // Click the delete button next to that user
        onView(allOf(
                withText("Delete"),
                isDescendantOfA(hasDescendant(withText("name2")))
        )).perform(click());

        // Confirm deletion in the AlertDialog
        onView(withText("Delete")).perform(click());

        // Wait for deletion to complete and Toast to appear (optional)
        Thread.sleep(3000); // crude wait; replace with IdlingResource for production

        // Verify user is no longer in the list
        onView(withText("name2")).check(doesNotExist());
    }

    /**
     * Helper for waiting for a view to appear in the hierarchy
     */
    public static ViewAction waitForView(final Matcher<View> matcher, final long timeout) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for a specific view with matcher <" + matcher + "> for " + timeout + "ms.";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + timeout;

                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        if (matcher.matches(child)) {
                            return;
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < endTime);

                throw new AssertionError("View " + matcher + " not found within " + timeout + "ms");
            }
        };
    }
}
