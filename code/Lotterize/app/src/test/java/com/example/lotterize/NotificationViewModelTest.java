package com.example.lotterize;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import androidx.arch.core.executor.ArchTaskExecutor;
import androidx.arch.core.executor.TaskExecutor;

import com.example.lotterize.ui.notifications.NotificationsViewModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

public class NotificationViewModelTest {

    private FirebaseFirestore db;
    private CollectionReference eventsCol;
    private DocumentReference eventDoc;
    private Task<DocumentSnapshot> getTask;
    private DocumentSnapshot snapshot;

    private NotificationSender mockSender;
    private NotificationsViewModel viewModel;

    @Before
    public void setUp() {
        // Make LiveData think we're always on the "main thread"
        ArchTaskExecutor.getInstance().setDelegate(new TaskExecutor() {
            @Override
            public void executeOnDiskIO(Runnable runnable) {
                runnable.run();
            }

            @Override
            public void postToMainThread(Runnable runnable) {
                runnable.run();
            }

            @Override
            public boolean isMainThread() {
                return true;
            }
        });

        db = mock(FirebaseFirestore.class);
        eventsCol = mock(CollectionReference.class);
        eventDoc = mock(DocumentReference.class);
        getTask = mock(Task.class);
        snapshot = mock(DocumentSnapshot.class);

        when(db.collection("events")).thenReturn(eventsCol);
        when(eventsCol.document(anyString())).thenReturn(eventDoc);
        when(eventDoc.get()).thenReturn(getTask);

        mockSender = mock(NotificationSender.class);

        viewModel = new NotificationsViewModel(db, mockSender, "user123", false);
    }

    @After
    public void tearDown() {
        ArchTaskExecutor.getInstance().setDelegate(null);
    }

    @Test
    public void sendToStatusTest_ReceiversListHasRecipients() {
        when(getTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            OnSuccessListener<DocumentSnapshot> listener = inv.getArgument(0);

            when(snapshot.exists()).thenReturn(true);
            when(snapshot.get("waitlist")).thenReturn(Arrays.asList("u1", "u2"));

            listener.onSuccess(snapshot);
            return getTask;
        });
        when(getTask.addOnFailureListener(any())).thenReturn(getTask);

        // When sendNotification is called, simulate onComplete(2)
        doAnswer(inv -> {
            NotificationSender.NotificationCallback cb = inv.getArgument(3);
            cb.onComplete(2);
            return null;
        }).when(mockSender).sendNotification(
                anyString(),
                anyString(),
                any(ArrayList.class),
                any(NotificationSender.NotificationCallback.class)
        );

        viewModel.sendToStatus("event123", "waitlist", "Hello chosen", "sender123");

        assertEquals("Sent to 2 entrant(s)", viewModel.toast().getValue());
    }

    @Test
    public void sendToStatusTest_NoEntrantsWantToReceiveNotifs() {
        when(getTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            OnSuccessListener<DocumentSnapshot> listener = inv.getArgument(0);

            when(snapshot.exists()).thenReturn(true);
            when(snapshot.get("waitlist")).thenReturn(Arrays.asList("u1", "u2"));

            listener.onSuccess(snapshot);
            return getTask;
        });
        when(getTask.addOnFailureListener(any())).thenReturn(getTask);

        doAnswer(inv -> {
            NotificationSender.NotificationCallback cb = inv.getArgument(3);
            cb.onComplete(0);
            return null;
        }).when(mockSender).sendNotification(anyString(), anyString(), any(ArrayList.class), any(NotificationSender.NotificationCallback.class)
        );

        viewModel.sendToStatus("event123", "waitlist", "Hello", "sender123");

        assertEquals("No entrants in this list want to receive notifications!!", viewModel.toast().getValue()
        );
    }
}
