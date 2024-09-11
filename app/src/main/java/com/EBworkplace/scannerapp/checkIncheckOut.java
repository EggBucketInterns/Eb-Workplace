package com.EBworkplace.scannerapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class checkIncheckOut extends AppCompatActivity {

    private Button morning_check_in, morning_check_out, evening_check_in, evening_check_out;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_check_incheck_out);
        morning_check_in = findViewById(R.id.Morning_check_in);
        morning_check_out = findViewById(R.id.Morning_check_out);
        evening_check_in = findViewById(R.id.Evening_check_in);
        evening_check_out = findViewById(R.id.Evening_check_out);


        morning_check_in.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(checkIncheckOut.this, morining_check_in.class);
            startActivity(intent);
        });
        morning_check_out.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(checkIncheckOut.this, morning_check_out.class);
            startActivity(intent);
        });
        evening_check_in.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(checkIncheckOut.this, evening_check_in.class);
            startActivity(intent);
        });
        evening_check_out.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(checkIncheckOut.this, evening_check_out.class);
            startActivity(intent);
        });
    }
}