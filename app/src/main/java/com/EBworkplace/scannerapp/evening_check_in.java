package com.EBworkplace.scannerapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.EBworkplace.scannerapp.UserModel.User_detail_model;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class evening_check_in extends AppCompatActivity {
    private ImageView take_photo;
         private Button submit;
    private EditText opening_stock;
    private Bitmap evening_check_in_image;
    private ProgressBar progressBar;
    private String phone;
    private FirebaseAuth mAuth;
    private FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_evening_check_in);
        take_photo = findViewById(R.id.take_photo);
        submit = findViewById(R.id.submit);
        opening_stock = findViewById(R.id.opening_stock);
        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(evening_check_in.this, loginActivity.class);
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
            if(evening_check_in_image == null){
                Toast.makeText(getApplicationContext(), "Please take a photo", Toast.LENGTH_SHORT).show();
                return;
            }
            handle_evening_check_in();
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
                    evening_check_in_image = (Bitmap) result.getData().getExtras().get("data");
                    Toast.makeText(getApplicationContext(), "Photo taken", Toast.LENGTH_LONG).show();
                }
            }
    );
    private void handle_evening_check_in() {
        progressBar.setVisibility(View.VISIBLE);
        String date = LocalDate.now().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(date).document(phone);
        String path = "Daily_Info/" + phone + "/" + date + "/" + "evening_check_in" + ".jpg";
        uploadPhoto(path, evening_check_in_image, taskSnapshot -> {
            ref.get().addOnCompleteListener(task -> {
                progressBar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    if (task.getResult() != null && task.getResult().exists()) {
                        if (!Objects.equals(task.getResult().getString("evening_check_in_time"), " ")) {
                            Toast.makeText(getApplicationContext(), "Already checked in", Toast.LENGTH_LONG).show();
                        } else {
                            Map<String, Object> map = new HashMap<>();
                            map.put("evening_check_in_time", LocalTime.now().toString());
                            map.put("evening_opening_stock", opening_stock.getText().toString());
                            ref.update(map).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed to check in", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        //Create new only for evening check in
                        check_local_cache_forName();
                        SharedPreferences pref = getSharedPreferences("cache",MODE_PRIVATE);
                        String outlet = pref.getString("name","null");
                        if(outlet.equals("null")){
                            outlet = " ";
                        }
                        User_detail_model user = new User_detail_model(
                                " ", " ", LocalTime.now().toString(), " ", " ", opening_stock.getText().toString(), " ", " ", " ",
                                " ",outlet
                        );
                        ref.set(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Checked in", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to check in", Toast.LENGTH_LONG).show();
                            }
                        });
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
    private void check_local_cache_forName(){
        SharedPreferences pref = getSharedPreferences("cache",MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        String name = pref.getString("name","null");
        if(name.equals("null")){
            if(mAuth.getCurrentUser() == null){
                Intent intent = new Intent(evening_check_in.this, loginActivity.class);
                Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
                startActivity(intent);
                finish();
                return;
            }
            String phone = mAuth.getCurrentUser().getPhoneNumber();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference ref = db.collection("Employees").document(phone);
            ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful() && task.getResult() != null) {
                        String name = task.getResult().getString("name");
                        editor.putString("name",name);
                        editor.apply();
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    private void uploadPhoto(String path, Bitmap image, OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener, OnFailureListener onFailureListener) {
        if (path.isEmpty() || image == null) {
            Toast.makeText(getApplicationContext(), "Please take a photo", Toast.LENGTH_SHORT).show();
            return;
        }
        StorageReference ref = storage.getReference().child(path);
        ByteArrayOutputStream arr = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, arr);
        UploadTask task = ref.putBytes(arr.toByteArray());
        task.addOnSuccessListener(onSuccessListener).addOnFailureListener(onFailureListener);
    }
}