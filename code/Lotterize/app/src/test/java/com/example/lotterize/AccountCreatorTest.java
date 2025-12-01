package com.example.lotterize;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

public class AccountCreatorTest {

    private FirebaseFirestore db;
    private CollectionReference usersCol;
    private Query usersQuery;
    private Task<QuerySnapshot> usersTask;
    private QuerySnapshot usersSnapshot;
    private DocumentReference mockDocRef;
    private Task<DocumentReference> mockAddTask;
    private AccountCreator accountCreator;
    private MockedStatic<FirebaseFirestore> firebaseFirestoreStaticMock;

    @Before
    public void setUp() {
        db = mock(FirebaseFirestore.class);
        usersCol = mock(CollectionReference.class);
        usersQuery = mock(Query.class);
        usersTask = mock(Task.class);
        usersSnapshot = mock(QuerySnapshot.class);
        mockDocRef = mock(DocumentReference.class);
        mockAddTask = mock(Task.class);

        firebaseFirestoreStaticMock = Mockito.mockStatic(FirebaseFirestore.class);
        firebaseFirestoreStaticMock.when(FirebaseFirestore::getInstance).thenReturn(db);

        // users collection mocking
        when(db.collection("users")).thenReturn(usersCol);
        when(usersCol.whereEqualTo(eq("username"), anyString())).thenReturn(usersQuery);
        when(usersQuery.get(Source.SERVER)).thenReturn(usersTask);
        when(usersSnapshot.isEmpty()).thenReturn(true); // no user exists initially

        // add() returns Task
        when(usersCol.add(any(User.class))).thenReturn(mockAddTask);
        when(mockDocRef.getId()).thenReturn("generatedId");

        // Task chaining
        when(mockAddTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockAddTask);
        when(mockAddTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockAddTask);

        accountCreator = new AccountCreator(usersCol);
    }

    @After
    public void tearDown() {
        firebaseFirestoreStaticMock.close();
    }

    @Test
    public void testCreateAccount_EmptyFields() {
        AccountCreator.Callback callback = mock(AccountCreator.Callback.class);

        accountCreator.createAccount("", "password123", callback);
        accountCreator.createAccount("username", "", callback);

        verify(callback, times(2)).onFailure("Username and password cannot be empty");
        verify(callback, never()).onSuccess(any());
    }

}
