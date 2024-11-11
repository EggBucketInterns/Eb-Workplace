package com.EBworkplace.scannerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.EBworkplace.scannerapp.UserModel.PhotoUploadWorker;
import com.EBworkplace.scannerapp.UserModel.User_detail_model;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;

public class morining_check_in extends AppCompatActivity {
    private ImageView take_photo;
    private Button   submit;
    private EditText opening_stock;
    private Bitmap morning_check_in_image;
    private ProgressBar progressBar;
    private String phone;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_morining_check_in);
        take_photo = findViewById(R.id.take_photo);
        submit = findViewById(R.id.submit);
        opening_stock = findViewById(R.id.opening_stock);
        progressBar = findViewById(R.id.progressbarmcin);
        progressBar.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(morining_check_in.this, loginActivity.class);
            Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }

        phone = mAuth.getCurrentUser().getPhoneNumber();
        storage = FirebaseStorage.getInstance();
        take_photo.setOnClickListener(v -> {
            takePhotoIntent(take_photo_intent);
        });
        
        submit.setOnClickListener(v->{
            if(opening_stock.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "Please enter opening stock", Toast.LENGTH_SHORT).show();
                return;
            }
            if(morning_check_in_image == null){
                Toast.makeText(getApplicationContext(), "Please take a photo", Toast.LENGTH_SHORT).show();
                return;
            }
            handle_morning_check_in();
        });
    }
    private void takePhotoIntent(ActivityResultLauncher<Intent> resultLauncher){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            resultLauncher.launch(intent);
        }
    }
    private final ActivityResultLauncher<Intent> take_photo_intent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    morning_check_in_image = (Bitmap) result.getData().getExtras().get("data");
                    Toast.makeText(getApplicationContext(), "Photo taken", Toast.LENGTH_LONG).show();
                }
            }
    );
    //old code
//    private void handle_morning_check_in() {
//        progressBar.setVisibility(View.VISIBLE);
//        String date = LocalDate.now().toString();
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        DocumentReference ref = db.collection(date).document(phone);
//        String path = "Daily_Info/" + phone + "/" + date + "/" + "morning_check_in" + ".jpg";
//
//        uploadPhoto(path, morning_check_in_image, taskSnapshot -> {
//            ref.get().addOnCompleteListener(task -> {
//                progressBar.setVisibility(View.INVISIBLE);
//                if (task.isSuccessful()) {
//                    if (task.getResult() != null && task.getResult().exists()) {
//                        Toast.makeText(getApplicationContext(), "Already signed in for today", Toast.LENGTH_LONG).show();
//                    } else {
//                        // Ensure local cache is updated before proceeding
//                        checkLocalCacheForName(() -> {
//                            SharedPreferences pref = getSharedPreferences("cache", MODE_PRIVATE);
//                            String outlet = pref.getString("name", "null");
//                            if(outlet.equals("null")){
//                                outlet = " ";
//                            }
//                            User_detail_model user = new User_detail_model(
//                                    LocalTime.now().toString(), " ", " ", " ",
//                                    opening_stock.getText().toString(), " ",
//                                    " ", " ", " ",
//                                    " ", outlet
//                            );
//                            ref.set(user).addOnCompleteListener(task1 -> {
//                                if (task1.isSuccessful()) {
//                                    Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_LONG).show();
//                                } else {
//                                    Toast.makeText(getApplicationContext(), "Failed to check in", Toast.LENGTH_LONG).show();
//                                    Log.e("FirestoreError", "Error setting document", task1.getException());
//                                }
//                            });
//                        });
//                    }
//                } else {
//                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_LONG).show();
//                    Log.e("FirestoreError", "Error getting document", task.getException());
//                }
//            });
//        }, e -> {
//            progressBar.setVisibility(View.INVISIBLE);
//            Toast.makeText(getApplicationContext(), "Photo upload error", Toast.LENGTH_LONG).show();
//            Log.e("UploadError", "Error uploading photo", e);
//        });
//    }
    private void handle_morning_check_in() {
        progressBar.setVisibility(View.VISIBLE);
        String date = LocalDate.now().toString();
        String path = "Daily_Info/" + phone + "/" + date + "/" + "morning_check_in" + ".jpg";
        String imagePath = saveBitmapToFile(morning_check_in_image);
        saveCheckInData(date);

    // Prepare data for the worker
        Data workerData = new Data.Builder()
                .putString("path", path)
                .putString("imagePath", imagePath) // Pass file path instead of Bitmap
                .putString("phone", phone)
                .build();
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED) // Only run when network is connected
                .build();
    // Enqueue the worker
        OneTimeWorkRequest uploadWorkRequest = new OneTimeWorkRequest.Builder(PhotoUploadWorker.class)
            .setInputData(workerData)
            .setConstraints(constraints)
            .build();

        WorkManager.getInstance(getApplicationContext()).enqueue(uploadWorkRequest);

    // Monitor the worker's state
        WorkManager.getInstance(getApplicationContext()).getWorkInfoByIdLiveData(uploadWorkRequest.getId())
                .observe(this, workInfo -> {
                    if (workInfo != null && workInfo.getState().isFinished()) {

                        if (workInfo.getState() == WorkInfo.State.SUCCEEDED) {
//                            Toast.makeText(getApplicationContext(), "Photo uploaded ", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Photo upload failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });


    }

    private void saveCheckInData(String date) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(date).document(phone);

        checkLocalCacheForName(() -> {
            SharedPreferences pref = getSharedPreferences("cache", MODE_PRIVATE);
            String outlet = pref.getString("name", "null");
            if (outlet.equals("null")) {
                outlet = " ";
            }
            User_detail_model user = new User_detail_model(
                    LocalTime.now().toString(), " ", " ", " ",
                    opening_stock.getText().toString(), " ",
                    " ", " ", outlet
            );
            ref.get().addOnCompleteListener(task -> {

                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Already signed in for today", Toast.LENGTH_LONG).show();
                    } else {
                        // Ensure local cache is updated before proceeding
                        checkLocalCacheForName(() -> {


                            ref.set(user).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_LONG).show();
                                } else {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(getApplicationContext(), "Failed to check in", Toast.LENGTH_LONG).show();
                                    Log.e("FirestoreError", "Error setting document", task1.getException());
                                }
                            });
                        });
                    }
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_LONG).show();
                    Log.e("FirestoreError", "Error getting document", task.getException());
                }
            });
        });


    }



    private void checkLocalCacheForName(Runnable callback) {
        SharedPreferences pref = getSharedPreferences("cache", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String name = pref.getString("name", "null");
        if (name.equals("null")) {
            if (mAuth.getCurrentUser() == null) {
                Intent intent = new Intent(morining_check_in.this, loginActivity.class);
                Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
                return;
            }
            String phone = mAuth.getCurrentUser().getPhoneNumber();
            if (phone == null) {
                Toast.makeText(getApplicationContext(), "Phone number is unavailable", Toast.LENGTH_SHORT).show();
                return;
            }
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference ref = db.collection("Employees").document(phone);
            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        String fetchedName = document.getString("name");
                        if (fetchedName != null && !fetchedName.isEmpty()) {
                            editor.putString("name", fetchedName);
                            editor.apply();
                            Log.d("CacheUpdate", "Name successfully updated in cache: " + fetchedName);
                        } else {
                            Toast.makeText(getApplicationContext(), "Name field is empty", Toast.LENGTH_SHORT).show();
                            Log.e("CacheUpdate", "Name field is empty in Firestore document.");
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Document does not exist", Toast.LENGTH_SHORT).show();
                        Log.e("CacheUpdate", "Document does not exist in Firestore.");
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
                    Log.e("CacheUpdate", "Error getting document", task.getException());
                }
                // Invoke callback after completing cache update
                callback.run();
            });
        } else {
            // Invoke callback if cache check is not needed
            callback.run();
        }
    }


//    private void uploadPhoto(String path, Bitmap image, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
//        if (path.isEmpty() || image == null) {
//            Toast.makeText(getApplicationContext(), "Please take a photo", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        StorageReference ref = storage.getReference().child(path);
//        ByteArrayOutputStream arr = new ByteArrayOutputStream();
//        image.compress(Bitmap.CompressFormat.JPEG, 50, arr);
//        UploadTask task = ref.putBytes(arr.toByteArray());
//        task.addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
//    }
    private String saveBitmapToFile(Bitmap bitmap) {
        File file = new File(getCacheDir(), "morning_check_in.jpg");

        try {
            // Check if the file already exists
            if (file.exists()) {
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    Log.i("FileDeletion", "Existing file deleted successfully: " + file.getAbsolutePath());
                } else {
                    Log.e("FileDeletion", "Failed to delete existing file: " + file.getAbsolutePath());
                }
            }

            // Save the new bitmap to file
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);
            outputStream.flush();
            outputStream.close();

            Log.i("FileSave", "Bitmap saved successfully at: " + file.getAbsolutePath());
            return file.getAbsolutePath();

        } catch (IOException e) {
            Log.e("FileSaveError", "Error saving bitmap to file", e);
            return null;
        }
    }


}