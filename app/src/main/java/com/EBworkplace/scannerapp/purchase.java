package com.EBworkplace.scannerapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

public class purchase extends AppCompatActivity {

    private EditText quantityInput;
    private Button submitButton;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private String phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_purchase); // Replace with your actual layout file name

        quantityInput = findViewById(R.id.quantity_input); // EditText for quantity
        submitButton = findViewById(R.id.submit); // Button for submission
        progressBar = findViewById(R.id.progressbar); // ProgressBar for loading feedback
        progressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        // Check if user is logged in
        if (mAuth.getCurrentUser() == null) {
            Intent intent = new Intent(purchase.this, loginActivity.class);
            Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
            startActivity(intent);
            finish();
        }

        phone = mAuth.getCurrentUser().getPhoneNumber();

        submitButton.setOnClickListener(v -> {
            String quantity = quantityInput.getText().toString();
            if (quantity.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter quantity", Toast.LENGTH_SHORT).show();
                return;
            }
            handlePurchase(quantity);
        });
    }

    private void handlePurchase(String quantity) {
        progressBar.setVisibility(View.VISIBLE);
        String date = LocalDate.now().toString();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection(date).document(phone);

        // Prepare data to upload
        Map<String, Object> purchaseData = new HashMap<>();
        purchaseData.put("purchase_time", LocalTime.now().toString());
        purchaseData.put("purchased_stock", quantity);

        // Upload data to Firestore
        ref.set(purchaseData).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.INVISIBLE);
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "Purchase recorded", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to record purchase", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
