package com.fikrathhassan.socialmediaproject.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fikrathhassan.socialmediaproject.R;
import com.fikrathhassan.socialmediaproject.util.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class IntroActivity extends AppCompatActivity {

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!Utils.isSignedIn()) {
            startActivity(new Intent(IntroActivity.this, SignInActivity.class));
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_intro);

        initViews();
        initListeners();
    }

    void initViews() {
        progressBar = findViewById(R.id.progressBar);
    }

    void initListeners() {
        new Handler().postDelayed(this::getUserDetails, 2000);
    }

    void getUserDetails() {
        Utils.showView(progressBar);
        String email = Utils.getEmail();
        String encryptedPassword = Utils.getPassword();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(email)
                .get()
                .addOnCompleteListener(task -> {
                    Utils.hideView(progressBar);
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            if (document.exists()) {
                                if (document.getString("password").equals(encryptedPassword)) {
                                    startActivity(new Intent(IntroActivity.this, MainActivity.class));
                                } else {
                                    Toast.makeText(getApplicationContext(),
                                            "Your password is changed. Please login Again!",
                                            Toast.LENGTH_SHORT).show();
                                    startSignInActivity();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "User not found!",
                                        Toast.LENGTH_SHORT).show();
                                startSignInActivity();
                            }
                        }
                    }
                    finish();
                })
                .addOnFailureListener(e -> {
                    Utils.hideView(progressBar);
                    Toast.makeText(getApplicationContext(),
                            "User not found!",
                            Toast.LENGTH_SHORT).show();
                });
    }

    void startSignInActivity() {
        Utils.setLogin("", "", "", "", false);
        startActivity(new Intent(IntroActivity.this, SignInActivity.class));
    }
}