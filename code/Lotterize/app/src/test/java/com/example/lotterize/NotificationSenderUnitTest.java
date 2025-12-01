package com.example.lotterize;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

import android.util.Log;

public class NotificationSenderUnitTest {

    private FirebaseFirestore db;
    private CollectionReference notificationsCol;
    private DocumentReference docRef;
    private Task<Void> mockTask;

    private CollectionReference usersCol;
    private Query usersQuery;
    private Task<QuerySnapshot> usersTask;
    private QuerySnapshot usersSnapshot;
    private DocumentSnapshot userSnapA;
    private DocumentSnapshot userSnapB;
    private Task<List<Object>> whenAllTask;

    private MockedStatic<FirebaseFirestore> firebaseFirestoreStaticMock;
    private MockedStatic<Tasks> tasksStaticMock;

    private MockedStatic<Log> logStaticMock;

    @Before
    public void setUp() {
        db = mock(FirebaseFirestore.class);
        notificationsCol = mock(CollectionReference.class);
        docRef = mock(DocumentReference.class);
        mockTask = mock(Task.class);

        usersCol = mock(CollectionReference.class);
        usersQuery = mock(Query.class);
        usersTask = mock(Task.class);
        usersSnapshot = mock(QuerySnapshot.class);
        userSnapA = mock(DocumentSnapshot.class);
        userSnapB = mock(DocumentSnapshot.class);
        whenAllTask = mock(Task.class);

        // Static mocks
        firebaseFirestoreStaticMock = mockStatic(FirebaseFirestore.class);
        firebaseFirestoreStaticMock.when(FirebaseFirestore::getInstance).thenReturn(db);

        tasksStaticMock = mockStatic(Tasks.class);

        // mock Log s
        logStaticMock = mockStatic(Log.class);
        logStaticMock.when(() -> Log.d(anyString(), anyString())).thenReturn(0);
        logStaticMock.when(() -> Log.d(anyString(), anyString(), any(Throwable.class))).thenReturn(0);
        logStaticMock.when(() -> Log.e(anyString(), anyString())).thenReturn(0);
        logStaticMock.when(() -> Log.e(anyString(), anyString(), any(Throwable.class))).thenReturn(0);


        // notifications collection mocking
        when(db.collection("notifications")).thenReturn(notificationsCol);
        when(notificationsCol.document()).thenReturn(docRef);
        when(docRef.getId()).thenReturn("Test_Id_123");
        when(docRef.set(any())).thenReturn(mockTask);
        when(mockTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockTask);
        when(mockTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockTask);

        // users collection mocking
        when(db.collection("users")).thenReturn(usersCol);
        when(usersCol.whereIn(eq(FieldPath.documentId()), anyList())).thenReturn(usersQuery);
        when(usersQuery.get()).thenReturn(usersTask);

        // When NotificationSender builds tasks list and calls Tasks.whenAllSuccess(...)
        tasksStaticMock.when(() -> Tasks.whenAllSuccess(anyList())).thenReturn(whenAllTask);

        // Fake user docs: both opted IN
        when(userSnapA.getId()).thenReturn("ReceiverA");
        when(userSnapB.getId()).thenReturn("ReceiverB");
        when(userSnapA.getBoolean("wantNotification")).thenReturn(true);
        when(userSnapB.getBoolean("wantNotification")).thenReturn(true);
        when(usersSnapshot.getDocuments()).thenReturn(Arrays.asList(userSnapA, userSnapB));

        // When addOnSuccessListener is attached to the whenAllTask, immediately call it
        when(whenAllTask.addOnSuccessListener(any())).thenAnswer(inv -> {
            @SuppressWarnings("unchecked")
            OnSuccessListener<List<Object>> listener = inv.getArgument(0);
            List<Object> results = new ArrayList<>();
            results.add(usersSnapshot);
            listener.onSuccess(results);
            return whenAllTask;
        });

        when(whenAllTask.addOnFailureListener(any())).thenReturn(whenAllTask);

        CurrentUser.set(null);
    }

    @After
    public void tearDown() {
        CurrentUser.set(null);
        firebaseFirestoreStaticMock.close();
        tasksStaticMock.close();
        logStaticMock.close();
    }

    private User mockCurrentUser() {
        User currentUser = new User();
        currentUser.setUserId("Sender_Id_123");
        currentUser.setUsername("Bui");
        return  currentUser;
    }

    @Test
    public void TestSendNotification_BuildsAndWritesToDb() {
        // Arrange
        CurrentUser.set(mockCurrentUser());
        CurrentUser.get().setName("Nathan Bui");
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

        sender.sendNotification(CurrentUser.get().getUserId(),
                "Hi!!! I'm Testing the Notification system",
                receiversId);
    }

    @Test
    public void TestSendNotification_MissingFields() {
        // Arrange
        CurrentUser.set(mockCurrentUser());
        NotificationSender sender = new NotificationSender();
        Notification notification = new Notification();

        // Missing notificationId, senderName, senderId and time on purpose
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
            assertEquals("Username: Bui", n.getSenderName());
            assertEquals("Hi!!! I'm Testing the Notification system", n.getMessage());
            assertNotNull(n.getTime());
            // Receivers should stay as we set them, since both wantNotification = true
            assertEquals(receiversId, n.getReceiversId());
            return mockTask;
        }).when(docRef).set(isA(Notification.class));

        sender.sendNotification(notification);
    }

    @Test
    public void TestSendNotification_NoCandidateReceivers(){
        CurrentUser.set(mockCurrentUser());

        NotificationSender sender = new NotificationSender();
        NotificationSender.NotificationCallback callback = mock(NotificationSender.NotificationCallback.class);

        ArrayList<String> receiversId = new ArrayList<>();


        sender.sendNotification(CurrentUser.get().getUserId(), "No receivers for this notification", receiversId, callback);

        verify(callback).onComplete(0);
        verify(callback, never()).onError(any());

        verify(docRef, never()).set(any(Notification.class));
    }

    @Test
    public void TestSendNotification_AllReceiversOptedOutReceivingNotif(){
        CurrentUser.set(mockCurrentUser());

        NotificationSender sender = new NotificationSender();
        NotificationSender.NotificationCallback callback = mock(NotificationSender.NotificationCallback.class);

        ArrayList<String> receiversId = new ArrayList<>();
        receiversId.add("ReceiverA");
        receiversId.add("ReceiverB");

        when(userSnapA.getBoolean("wantNotification")).thenReturn(false);
        when(userSnapB.getBoolean("wantNotification")).thenReturn(false);


        sender.sendNotification(CurrentUser.get().getUserId(), "Testing notification sender ", receiversId, callback);

        verify(callback).onComplete(0);
        verify(callback, never()).onError(any());

        verify(docRef, never()).set(any(Notification.class));
    }
}
