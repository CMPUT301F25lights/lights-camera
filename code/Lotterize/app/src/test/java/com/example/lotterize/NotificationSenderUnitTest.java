package com.example.lotterize;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

public class NotificationSenderUnitTest {

    private FirebaseFirestore db;
    private CollectionReference notificationsCol;
    private DocumentReference docRef;
    private MockedStatic<FirebaseFirestore> firebaseFirestoreStaticMock;
    private Task<Void> mockTask;

    @Before
    public void setUp() {
        db = mock(FirebaseFirestore.class);
        notificationsCol = mock(CollectionReference.class);
        docRef = mock(DocumentReference.class);
        mockTask = mock(Task.class);

        firebaseFirestoreStaticMock = mockStatic(FirebaseFirestore.class);
        firebaseFirestoreStaticMock.when(FirebaseFirestore::getInstance).thenReturn(db);

        when(db.collection("notifications")).thenReturn(notificationsCol);
        when(notificationsCol.document()).thenReturn(docRef);
        when(docRef.getId()).thenReturn("Test_Id_123");
        when(docRef.set(any())).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockTask);

        CurrentUser.set(null);
    }

    @After
    public void tearDown() {
        CurrentUser.set(null);
        firebaseFirestoreStaticMock.close();
    }

    private User mockCurrentUser() {
        User currentUser = new User();
        currentUser.setUserId("Sender_Id_123");
        currentUser.setName("Nathan Bui");
        return  currentUser;
    }

    @Test
    public void TestSendNotification_BuildsAndWritesToDb() {
        // Arrange
        CurrentUser.set(mockCurrentUser());
        NotificationSender sender = new NotificationSender();

        ArrayList<String> receiversId = new ArrayList<>();
        receiversId.add("ReceiverA");
        receiversId.add("ReceiverB");

        // Intercept BEFORE act; assert fields of the Notification written to Firestore
        doAnswer(inv -> {
            Notification n = inv.getArgument(0);

            assertEquals("Test_Id_123", n.getNotificationId()); // from mocked docRef.getId()
            assertEquals("Sender_Id_123", n.getSenderId());
            assertEquals("Nathan Bui", n.getSenderName());
            assertEquals("Hi!!! I'm Testing the Notification system", n.getMessage());
            assertNotNull(n.getTime());
            assertEquals(receiversId, n.getReceiversId());
            return  mockTask;
        }).when(docRef).set(isA(Notification.class));

        sender.sendNotification(CurrentUser.get().getUserId(), "Hi!!! I'm Testing the Notification system", receiversId);
    }

    @Test
    public void TestSendNotification_MissingFields() {
        // Arrange
        CurrentUser.set(mockCurrentUser());
        NotificationSender sender = new NotificationSender();
        Notification notification = new Notification();

        //Missing notificationId, senderName, SenderId and time
        ArrayList<String> receiversId = new ArrayList<>();
        receiversId.add("ReceiverA");
        receiversId.add("ReceiverB");

        notification.setReceiversId(receiversId);
        notification.setMessage("Hi!!! I'm Testing the Notification system");


        // Intercept BEFORE act; assert fields of the Notification written to Firestore
        doAnswer(inv -> {
            Notification n = inv.getArgument(0);

            assertEquals("Test_Id_123", n.getNotificationId()); // from mocked docRef.getId()
            assertEquals("Sender_Id_123", n.getSenderId());
            assertEquals("Nathan Bui", n.getSenderName());
            assertEquals("Hi!!! I'm Testing the Notification system", n.getMessage());
            assertNotNull(n.getTime());
            assertEquals(receiversId, n.getReceiversId());
            return  mockTask;
        }).when(docRef).set(isA(Notification.class));

        sender.sendNotification(CurrentUser.get().getUserId(), "Hi!!! I'm Testing the Notification system", receiversId);
    }
}
