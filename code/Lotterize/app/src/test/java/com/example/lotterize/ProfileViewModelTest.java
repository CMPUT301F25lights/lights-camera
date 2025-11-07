package com.example.lotterize;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.lotterize.ui.profile.ProfileViewModel;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfileViewModel using Mockito to mock Firebase Firestore.
 * These tests verify the ViewModel's behavior without requiring actual Firebase connectivity.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProfileViewModelTest {

    // Rule to execute LiveData operations synchronously for testing
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    // Mocked Firebase components
    @Mock
    private FirebaseFirestore mockFirestore;

    @Mock
    private CollectionReference mockCollectionReference;

    @Mock
    private DocumentReference mockDocumentReference;

    @Mock
    private DocumentSnapshot mockDocumentSnapshot;

    @Mock
    private Observer<User> mockObserver;

    private ProfileViewModel viewModel;
    private User testUser;
    private MockedStatic<FirebaseFirestore> mockedStaticFirestore;
    private MockedStatic<CurrentUser> mockedStaticCurrentUser;
    private EventListener<DocumentSnapshot> snapshotListener;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Create a test user
        testUser = new User(
                "testUserId123",
                "Michael Jordan",
                "1234567890",
                "jordan@example.com",
                "0,0",
                "mj",
                "password123"
        );

        mockedStaticFirestore = mockStatic(FirebaseFirestore.class);
        mockedStaticCurrentUser = mockStatic(CurrentUser.class);

        mockedStaticFirestore.when(FirebaseFirestore::getInstance).thenReturn(mockFirestore);
        when(mockFirestore.collection("users")).thenReturn(mockCollectionReference);
        when(mockCollectionReference.document(anyString())).thenReturn(mockDocumentReference);

        mockedStaticCurrentUser.when(CurrentUser::get).thenReturn(testUser);

        ArgumentCaptor<EventListener<DocumentSnapshot>> listenerCaptor =
                ArgumentCaptor.forClass(EventListener.class);
        when(mockDocumentReference.addSnapshotListener(listenerCaptor.capture()))
                .thenReturn(null);

        viewModel = new ProfileViewModel();

        snapshotListener = listenerCaptor.getValue();
    }

    @After
    public void tearDown() {
        // Close static mocks to prevent memory leaks
        if (mockedStaticFirestore != null) {
            mockedStaticFirestore.close();
        }
        if (mockedStaticCurrentUser != null) {
            mockedStaticCurrentUser.close();
        }
    }

    /**
     * Test that the ViewModel initializes with the current user's data
     */
    @Test
    public void testViewModelInitialization() {
        viewModel.getUserData().observeForever(mockObserver);

        verify(mockObserver).onChanged(testUser);
    }

    /**
     * Test that snapshot listener updates LiveData when Firestore data changes
     */
    @Test
    public void testSnapshotListenerUpdatesLiveData() {
        // Setup mock document snapshot
        when(mockDocumentSnapshot.exists()).thenReturn(true);
        when(mockDocumentSnapshot.getId()).thenReturn("testUserId123");
        when(mockDocumentSnapshot.getString("name")).thenReturn("Lebron James");
        when(mockDocumentSnapshot.getString("phoneNumber")).thenReturn("9876543210");
        when(mockDocumentSnapshot.getString("email")).thenReturn("james@example.com");
        when(mockDocumentSnapshot.getString("coordinates")).thenReturn("1,1");
        when(mockDocumentSnapshot.getString("username")).thenReturn("lbj");
        when(mockDocumentSnapshot.getString("password")).thenReturn("pass456");

        viewModel.getUserData().observeForever(mockObserver);

        snapshotListener.onEvent(mockDocumentSnapshot, null);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockObserver, atLeastOnce()).onChanged(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("Lebron James", capturedUser.getName());
        assertEquals("james@example.com", capturedUser.getEmail());
    }

    /**
     * Test that operations fail gracefully when currentUserId is null
     */
    @Test
    public void testOperationsWithNullUser() {
        mockedStaticCurrentUser.when(CurrentUser::get).thenReturn(null);

        ProfileViewModel nullUserViewModel = new ProfileViewModel();

        nullUserViewModel.updateName("Test");
        nullUserViewModel.updateEmail("test@test.com");
        nullUserViewModel.updatePhoneNumber("123456");
        nullUserViewModel.deleteAccount();

        verify(mockDocumentReference, never()).update(anyString(), any());
        verify(mockDocumentReference, never()).delete();
    }
}