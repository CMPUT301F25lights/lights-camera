package com.example.lotterize;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ImageHandlerTest {

    @Mock private FirebaseStorage mockStorage;
    @Mock private StorageReference mockStorageRef;
    @Mock private StorageReference mockImageRef;
    @Mock private UploadTask mockUploadTask;
    @Mock private Uri mockUri;

    private ImageHandler imageHandler;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        when(mockStorage.getReference()).thenReturn(mockStorageRef);
        when(mockStorageRef.child(any())).thenReturn(mockStorageRef);
        when(mockStorageRef.child(startsWith("EventImage_"))).thenReturn(mockImageRef);
        when(mockImageRef.putFile(any(Uri.class))).thenReturn(mockUploadTask);

        when(mockUploadTask.addOnSuccessListener(any(OnSuccessListener.class))).thenReturn(mockUploadTask);
        when(mockUploadTask.addOnFailureListener(any(OnFailureListener.class))).thenReturn(mockUploadTask);

        imageHandler = new ImageHandler(mockStorage);
    }

    @Test
    public void testAddImage_callsUploadFlow_success() {
        Runnable onSuccess = mock(Runnable.class);
        Runnable onFailure = mock(Runnable.class);

        imageHandler.addImage(null, mockUri, onSuccess, onFailure);

        verify(mockImageRef).putFile(mockUri);
        OnSuccessListener<UploadTask.TaskSnapshot> successListener = captureOnSuccessListener(mockUploadTask);
        assertNotNull(successListener);
    }

    @Test
    public void testAddImage_callsUploadFlow_failure() {
        Runnable onSuccess = mock(Runnable.class);
        Runnable onFailure = mock(Runnable.class);

        imageHandler.addImage(null, mockUri, onSuccess, onFailure);

        verify(mockImageRef).putFile(mockUri);
        OnFailureListener failureListener = captureOnFailureListener(mockUploadTask);
        assertNotNull(failureListener);
    }


    private OnSuccessListener<UploadTask.TaskSnapshot> captureOnSuccessListener(UploadTask task) {
        ArgumentCaptor<OnSuccessListener> captor = ArgumentCaptor.forClass(OnSuccessListener.class);
        verify(task).addOnSuccessListener(captor.capture());
        return captor.getValue();
    }

    private OnFailureListener captureOnFailureListener(UploadTask task) {
        ArgumentCaptor<OnFailureListener> captor = ArgumentCaptor.forClass(OnFailureListener.class);
        verify(task).addOnFailureListener(captor.capture());
        return captor.getValue();
    }
}
