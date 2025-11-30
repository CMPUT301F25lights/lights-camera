package com.example.lotterize;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Handles image uploads, removal, and cancellation.
 * Keeps track of upload status and uploaded image info.
 */
public class ImageHandler {

    private final FirebaseStorage storage;
    private final StorageReference storageRef;
    private UploadTask currentUploadTask;
    private String uploadedImageUrl = null;
    private String uploadedImagePath = null;

    public ImageHandler() {
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference().child("event_posters");
    }

    // Constructor for testing
    public ImageHandler(FirebaseStorage storage) {
        this.storage = storage;
        this.storageRef = storage.getReference().child("event_posters");
    }


    /** Adds/uploads an image */
    public void addImage(Context context, @NonNull Uri uri, Runnable onSuccess, Runnable onFailure) {
        removeImage(context, null); // remove previous image if any

        StorageReference imageRef = storageRef.child("EventImage_" + System.currentTimeMillis() + ".jpg");
        if (context != null)Toast.makeText(context,"Uploading image ...",Toast.LENGTH_SHORT).show();
        currentUploadTask = imageRef.putFile(uri);
        currentUploadTask.addOnSuccessListener(taskSnapshot -> {
            uploadedImagePath = imageRef.getPath();
            imageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                uploadedImageUrl = downloadUri.toString();
                if (onSuccess != null) onSuccess.run();
                if (context != null)Toast.makeText(context, "Image uploaded successfully!", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                uploadedImageUrl = null;
                uploadedImagePath = null;
                if (onFailure != null) onFailure.run();
                if (context != null)Toast.makeText(context, "Failed to get download URL", Toast.LENGTH_SHORT).show();
                removeImage(context, null);
            });
        }).addOnFailureListener(e -> {
            uploadedImageUrl = null;
            uploadedImagePath = null;
            if (onFailure != null) onFailure.run();
            if (context != null)Toast.makeText(context, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    /** Removes the current image from Firebase */
    public void removeImage(Context context, Runnable onSuccess) {
        cancelUpload(context);

        // delete previously uploaded image
        if (uploadedImagePath != null) {
            StorageReference imgRef = storage.getReference().child(uploadedImagePath);
            imgRef.delete().addOnSuccessListener(unused -> {
                if (onSuccess != null) onSuccess.run();
                if (context != null)Toast.makeText(context, "Image removed from Firebase", Toast.LENGTH_SHORT).show();
            }).addOnFailureListener(e -> {
                if (context != null)Toast.makeText(context, "Failed to delete image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }

        uploadedImageUrl = null;
        uploadedImagePath = null;
        currentUploadTask = null;
    }

    /** Cancel ongoing upload */
    public void cancelUpload(Context context) {
        if (currentUploadTask != null && !currentUploadTask.isComplete()) {
            currentUploadTask.cancel();
            if (context != null)Toast.makeText(context, "Upload cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    /** Check if an upload is in progress */
    public boolean isUploading() {
        return currentUploadTask != null && !currentUploadTask.isComplete();
    }

    /** Getters */
    public String getUploadedImageUrl() { return uploadedImageUrl; }
    public String getUploadedImagePath() { return uploadedImagePath; }

    /** Setters */
    public void setExistingImage(String imageUrl, String imagePath) {
        this.uploadedImageUrl = imageUrl;
        this.uploadedImagePath = imagePath;
        this.currentUploadTask = null; // Clear any old task references
    }
}