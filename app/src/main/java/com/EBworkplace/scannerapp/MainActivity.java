package com.EBworkplace.scannerapp;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanner;
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning;
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    FirebaseUser user;
    private FirebaseStorage storage;
    private FirebaseFirestore db;
    private ProgressBar progressBar;
    private StorageReference ref;
    private Button buttonUpload, buttonGallery, buttonScanAadhar , buttonScanPan , buttonScan10th , buttonScan12th ;
    private static List<Bitmap> bitmaps = new ArrayList<>();
    private static String phoneNo ;

    private final ActivityResultLauncher<IntentSenderRequest> scannerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartIntentSenderForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    GmsDocumentScanningResult scanningResult = GmsDocumentScanningResult.fromActivityResultIntent(result.getData());
                    if (scanningResult != null) {
                        List<GmsDocumentScanningResult.Page> pages = scanningResult.getPages();
                        for (GmsDocumentScanningResult.Page page : pages) {
                            Uri imageUri = page.getImageUri();
                            try {
                                InputStream imageStream = getContentResolver().openInputStream(imageUri);
                                Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                                bitmaps.add(bitmap);
                                Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                                e.printStackTrace();
                            }
                        }

                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = getIntent();
        phoneNo = intent.getExtras().getString("phone");
        buttonScanAadhar = findViewById(R.id.button_scan_aadhar);
        buttonScanPan = findViewById(R.id.button_scan_pan);
        buttonScan10th = findViewById(R.id.button_scan_10);
        buttonScan12th = findViewById(R.id.button_scan_12);
        buttonGallery = findViewById(R.id.galleryBtn);
        buttonUpload = findViewById(R.id.upload);
        storage = FirebaseStorage.getInstance();
        progressBar = findViewById(R.id.progressBar);
        db=FirebaseFirestore.getInstance();
        checkIfexists(user.getUid());
        buttonScanAadhar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDocumentScanning();
            }
        });
        buttonScanPan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDocumentScanning();
            }
        });
        buttonScan10th.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDocumentScanning();
            }
        });
        buttonScan12th.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDocumentScanning();
            }
        });
        buttonGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchSelectPictureIntent();
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImages();
            }
        });

    }
    private void checkIfexists(String uid) {
        DocumentReference ref = db.collection("Employees").document(uid);
        ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        return;
                    }
                    else{
                        createUser(ref);
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, "Network error please try later", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void createUser(DocumentReference ref) {
        //UserModel user = new UserModel(phoneNo,false, Timestamp.now());
        ref.set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "User Created", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "User Creation Failed, Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void startDocumentScanning() {
        GmsDocumentScannerOptions options = new GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(false)
                .setPageLimit(2)
                .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
                .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_BASE)
                .build();

        GmsDocumentScanner scanner = GmsDocumentScanning.getClient(options);

        scanner.getStartScanIntent(this)
                .addOnSuccessListener(intentSender -> {
                    IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(intentSender).build();
                    scannerLauncher.launch(intentSenderRequest);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(MainActivity.this, "Failed to start document scanning", Toast.LENGTH_SHORT).show();
                });
    }

    private void dispatchSelectPictureIntent() {
        Intent selectPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        selectPictureIntent.setType("image/*");
        selectPictureLauncher.launch(selectPictureIntent);
    }

    private final ActivityResultLauncher<Intent> selectPictureLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        Bitmap imageBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImageUri));
                        bitmaps.add(imageBitmap);
                        Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        Log.e("MainActivity", "Failed to load image", e);
                    }
                }
            });

    private byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadImages() {
        if(user==null){
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }
        checkIfexists(user.getUid());
        progressBar.setVisibility(View.VISIBLE);
        if(bitmaps.isEmpty()){
            Toast.makeText(this, "No images selected", Toast.LENGTH_SHORT).show();
            return;
        }
        if(bitmaps.size()>=9){
            Toast.makeText(this, "Maximum number of images reached , please restart app and scan again", Toast.LENGTH_SHORT).show();
            return;
        }
        Random rand = new Random();
        ref = storage.getReference().child("Images/"+phoneNo);
        for (int i = 0; i < bitmaps.size(); i++) {
            Bitmap bitmap = bitmaps.get(i);
            byte[] data = getBytesFromBitmap(bitmap);
            String path = "image_" + rand.nextInt(50000) + ".jpg";
            StorageReference imageRef = ref.child(path);

            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(MainActivity.this, "Images uploaded", Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Image upload failed", Toast.LENGTH_LONG).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);
                    if(progress==100){
                        progressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }
}
