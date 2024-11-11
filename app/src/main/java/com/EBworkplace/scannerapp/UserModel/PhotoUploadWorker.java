package com.EBworkplace.scannerapp.UserModel;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class PhotoUploadWorker extends Worker {
    private final FirebaseStorage storage;
    private final Context context;

    public PhotoUploadWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        storage = FirebaseStorage.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        String path = getInputData().getString("path");
        String imagePath = getInputData().getString("imagePath");

        if (path == null || imagePath == null) {
            Log.e("PhotoUploadWorker", "Invalid input data: path or imagePath is null");
            return Result.failure();
        }

        // Load the image file from local storage
        File imageFile = new File(imagePath);
        if (!imageFile.exists()) {
            Log.e("PhotoUploadWorker", "Image file does not exist at path: " + imagePath);
            return Result.failure();
        }

        // Firebase Storage Reference
        StorageReference ref = storage.getReference().child(path);

        try {
            // Check if the file already exists in Firebase Storage
            ref.getMetadata().addOnSuccessListener(metadata -> {
                // File exists, display a Toast message
                Log.i("PhotoUploadWorker", "File already exists in Firebase Storage");
                showToast("Already has photo");
            }).addOnFailureListener(exception -> {
                // File does not exist, proceed to upload
                try {
                    byte[] data = Files.readAllBytes(imageFile.toPath());
                    uploadFile(ref, data, path, imagePath);
                } catch (IOException e) {
                    Log.e("PhotoUploadWorker", "Error reading image file", e);
                }
            });

            return Result.success();
        } catch (Exception e) {
            Log.e("PhotoUploadWorker", "Unexpected error during upload", e);
            return Result.failure();
        }
    }

    // Upload file to Firebase Storage
    private void uploadFile(StorageReference ref, byte[] data, String path, String imagePath) {
        UploadTask uploadTask = ref.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            Log.i("PhotoUploadWorker", "Image uploaded successfully to path: " + path);
            deleteImageFile(imagePath);
        }).addOnFailureListener(e -> {
            Log.e("PhotoUploadWorker", "Error uploading image", e);
        });
    }

    // Delete image after upload
    private boolean deleteImageFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                Log.i("FileDeletion", "File successfully deleted: " + filePath);
                return true;
            } else {
                Log.e("FileDeletion", "Failed to delete file: " + filePath);
                return false;
            }
        } else {
            Log.e("FileDeletion", "File does not exist at path: " + filePath);
            return false;
        }
    }

    // Display a Toast message
    private void showToast(final String message) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }
}
