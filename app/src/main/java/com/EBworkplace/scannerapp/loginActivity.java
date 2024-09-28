package com.EBworkplace.scannerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class loginActivity extends AppCompatActivity {
    private static final long OTP_TIMEOUT = 60000;
    private Button otpBtn, submit, resendOtp;
    private EditText phone, otp, resendOtpView;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String verification;
    private PhoneAuthProvider.ForceResendingToken resendToken;
    private CountDownTimer countDownTimer;
    private static boolean status = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(this);
        //FirebaseAuth.getInstance().getFirebaseAuthSettings().setAppVerificationDisabledForTesting(true);
        //FirebaseAuth.getInstance().getFirebaseAuthSettings().forceRecaptchaFlowForTesting(true);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());
        otpBtn = findViewById(R.id.otpBtn);
        otp = findViewById(R.id.otpField);
        phone = findViewById(R.id.editTextPhone);
        progressBar = findViewById(R.id.progressBar);
        submit = findViewById(R.id.submitBtn);
        resendOtpView = findViewById(R.id.resendOtpView);
        resendOtp = findViewById(R.id.resendOtp);
        phone.setVisibility(View.VISIBLE);
        otpBtn.setVisibility(View.VISIBLE);
        otpBtn.setClickable(false);
        resendOtp.setClickable(false);
        resendOtp.setVisibility(View.GONE);
        resendOtpView.setVisibility(View.GONE);
        check_signedIn();
        otpBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            String phoneNo =  phone.getText().toString();
            if (!isValidPhoneNumber(phoneNo)) {
                Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.INVISIBLE);
                return;
            }
            phoneNo = "+91" + phoneNo;
            check_valid_user(phoneNo);
        });


        submit.setOnClickListener(v -> {
            String otpText = otp.getText().toString();
            if (otpText.length() != 6) {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Enter a valid OTP", Toast.LENGTH_SHORT).show();
                return;
            }
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verification, otpText);
            signIn(credential);
            progressBar.setVisibility(View.VISIBLE);
        });

        resendOtp.setOnClickListener(v -> sendOtp(phone.getText().toString(), false));
    }
    private void check_signedIn(){
        if(FirebaseAuth.getInstance().getCurrentUser()!=null){
            Toast.makeText(loginActivity.this,"Already Signed In",Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(loginActivity.this, checkIncheckOut.class);
            startActivity(intent);
        }
        else{
            Toast.makeText(loginActivity.this,"Sign In",Toast.LENGTH_SHORT).show();
        }
    }
    //Only if verification is used
    private void check_signedIn_and_verified(){
        progressBar.setVisibility(View.VISIBLE);
        phone.setClickable(false);
        otpBtn.setClickable(false);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            db = FirebaseFirestore.getInstance();
            db.collection("Employees").document(FirebaseAuth.getInstance().getCurrentUser().getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful() && task.getResult()!=null && task.getResult().exists()){
                        DocumentSnapshot document = task.getResult();
                        boolean verified = document.getBoolean("is_Verified");
                        if(verified){
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(loginActivity.this, checkIncheckOut.class);
                            startActivity(intent);
                        }
                        else{
                            progressBar.setVisibility(View.INVISIBLE);
                            Intent intent = new Intent(loginActivity.this,MainActivity.class);
                            intent.putExtra("phone",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                            startActivity(intent);
                        }
                    }
                    else{
                        progressBar.setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(loginActivity.this,MainActivity.class);
                        intent.putExtra("phone",FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
                        startActivity(intent);
                    }
                }
            });
        }
        else{
            Toast.makeText(loginActivity.this, "Sign In", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
            phone.setClickable(true);
            otpBtn.setClickable(true);
        }
    }
    private void check_valid_user(String phoneNo){
        progressBar.setVisibility(View.VISIBLE);
        db = FirebaseFirestore.getInstance();
        Task<DocumentSnapshot> ref = db.collection("Employees").document(phoneNo).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().exists()){
                    sendOtp(phoneNo,true);
                }
                else{
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(loginActivity.this, "User not registered", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private boolean isValidPhoneNumber(String phoneNo) {
        Pattern pattern = Pattern.compile("^\\d{10}$");
        Matcher matcher = pattern.matcher(phoneNo);
        return matcher.matches();
    }

    private void sendOtp(String phoneNumber, boolean isFirst) {
        startTimer();
        if (isFirst) {
            otpBtn.setVisibility(View.INVISIBLE);
            otpBtn.setClickable(false);
        }
        progressBar.setVisibility(View.VISIBLE);
        PhoneAuthOptions.Builder options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                .setPhoneNumber(phoneNumber)
                .setTimeout(OTP_TIMEOUT, TimeUnit.MILLISECONDS)
                .setActivity(loginActivity.this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                        progressBar.setVisibility(View.GONE);
                        otp.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.VISIBLE);
                    }
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Log.e("VerificationFailed", "Error: " + e.getMessage(), e);
                        if (e instanceof FirebaseAuthInvalidCredentialsException) {
                            // Invalid request
                            Toast.makeText(getApplicationContext(), "Invalid phone number.", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseTooManyRequestsException) {
                            // SMS quota exceeded
                            Toast.makeText(getApplicationContext(), "SMS quota exceeded. Try again later.", Toast.LENGTH_SHORT).show();
                        } else if (e instanceof FirebaseNetworkException) {
                            // Network error
                            Toast.makeText(getApplicationContext(), "Network error. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            // Other errors
                            Toast.makeText(getApplicationContext(),   e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        progressBar.setVisibility(View.GONE);
                        otp.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verification = s;
                        resendToken = forceResendingToken;
                        Toast.makeText(getApplicationContext(), "OTP sent successfully", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        otp.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.VISIBLE);
                    }
                });

        if (!isFirst) {
            options.setForceResendingToken(resendToken);
        }
        PhoneAuthProvider.verifyPhoneNumber(options.build());
    }

    private void signIn(PhoneAuthCredential credential) {
        progressBar.setVisibility(View.VISIBLE);
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                progressBar.setVisibility(View.GONE);
                if (task.isSuccessful()) {
                    Intent intent = new Intent(loginActivity.this, checkIncheckOut.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getApplicationContext(), "OTP verification failed", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(OTP_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
                resendOtpView.setVisibility(View.VISIBLE);
                resendOtpView.setText("Resend OTP in " + millisUntilFinished / 1000 + " seconds");
            }

            public void onFinish() {
                resendOtpView.setVisibility(View.GONE);
                resendOtp.setVisibility(View.VISIBLE);
                resendOtp.setClickable(true);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
