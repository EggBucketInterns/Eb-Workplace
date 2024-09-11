package com.EBworkplace.scannerapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class evening_check_out extends AppCompatActivity {
    private ImageView take_photo;
        private Button   submit;
    private EditText closing_stock , money_collected;
    private Bitmap evening_check_out_image;
    private String phone;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_evening_check_out);
        take_photo = findViewById(R.id.take_photo);
        submit = findViewById(R.id.submit);
        closing_stock = findViewById(R.id.closingStock);
        money_collected = findViewById(R.id.money_collected);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(evening_check_out.this, loginActivity.class);
            Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }
        phone = mAuth.getCurrentUser().getPhoneNumber();
        storage = FirebaseStorage.getInstance();
        take_photo.setOnClickListener(v -> {
            takePhotoIntent(take_photo_intent);
        });
        submit.setOnClickListener(v -> {
            if(closing_stock.getText().toString().isEmpty() || money_collected.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "Please enter closing stock and money collected", Toast.LENGTH_LONG).show();
                return;
            }
            if(evening_check_out_image == null){
                Toast.makeText(getApplicationContext(), "Please take a photo", Toast.LENGTH_LONG).show();
                return;
            }
            handle_evening_check_out();
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
                    evening_check_out_image = (Bitmap) result.getData().getExtras().get("data");
                    Toast.makeText(getApplicationContext(), "Photo taken", Toast.LENGTH_LONG).show();
                }
            }
    );
    private void handle_evening_check_out() {
        progressBar.setVisibility(View.VISIBLE);
        String date = LocalDate.now().toString();
        DocumentReference ref = FirebaseFirestore.getInstance().collection(date).document(phone);
        String path = "Daily_Info/" + phone + "/" + date + "/" + "evening_check_out" + ".jpg";
        uploadPhoto(path, evening_check_out_image, taskSnapshot -> {
            ref.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        if (Objects.equals(task.getResult().getString("evening_check_in_time"), " ")) {
                            Toast.makeText(getApplicationContext(), "Not checked in", Toast.LENGTH_SHORT).show();
                        } else if (!Objects.equals(task.getResult().getString("evening_check_out_time"), " ")) {
                            Toast.makeText(getApplicationContext(), "Already checked out", Toast.LENGTH_LONG).show();
                        } else {
                            Map<String, Object> map = new HashMap<>();
                            map.put("evening_check_out_time", LocalTime.now().toString());
                            map.put("evening_closing_stock", closing_stock.getText().toString());
                            map.put("evening_money_collected", money_collected.getText().toString());
                            ref.update(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Checked out", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to check out", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(getApplicationContext(), "Not checked in", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_LONG).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            });
        }, e -> {
            Toast.makeText(getApplicationContext(), "Photo upload error", Toast.LENGTH_LONG).show();
        });
    }

    private void uploadPhoto(String path, Bitmap image, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        if (path.isEmpty() || image == null) {
            Toast.makeText(getApplicationContext(), "Please take a photo", Toast.LENGTH_LONG).show();
            return;
        }
        StorageReference ref = storage.getReference().child(path);
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, arr);
        UploadTask task = ref.putBytes(arr.toByteArray());
        task.addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }
}

