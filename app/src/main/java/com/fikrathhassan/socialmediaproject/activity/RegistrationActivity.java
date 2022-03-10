package com.fikrathhassan.socialmediaproject.activity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.PatternsCompat;

import com.fikrathhassan.socialmediaproject.R;
import com.fikrathhassan.socialmediaproject.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;

public class RegistrationActivity extends AppCompatActivity {

    EditText usr_name, usr_email, usr_password, usr_confirmPassword;
    Button btn_register;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_registration);

        initViews();
        initListeners();
    }

    void initViews() {
        usr_name = findViewById(R.id.usr_name);
        usr_email = findViewById(R.id.usr_email);
        usr_password = findViewById(R.id.usr_password);
        usr_confirmPassword = findViewById(R.id.usr_confirmPassword);
        btn_register = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
    }

    void initListeners() {
        btn_register.setOnClickListener(view -> {
            String name = usr_name.getText().toString();
            String email = usr_email.getText().toString();
            String password = usr_password.getText().toString();
            String confirmPassword = usr_confirmPassword.getText().toString();
            if (isValidEmail(email)) {
                if (password.length() >= 8 && !password.contains(" ")) {
                    if (password.equals(confirmPassword)) {
                        Utils.hideView(btn_register);
                        Utils.showView(progressBar);
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(email)
                                .get()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot document = task.getResult();
                                        if (document != null) {
                                            if (document.exists()) {
                                                Utils.hideView(progressBar);
                                                Utils.showView(btn_register);
                                                Toast.makeText(getApplicationContext(),
                                                        "Email already registered!",
                                                        Toast.LENGTH_SHORT).show();
                                            } else {
                                                String encryptedPassword = DigestUtils.md5Hex(password);
                                                Map<String, Object> user = new HashMap<>();
                                                user.put("name", name);
                                                user.put("image", "");
                                                user.put("email", email);
                                                user.put("password", encryptedPassword);
                                                FirebaseFirestore.getInstance()
                                                        .collection("users")
                                                        .document(email)
                                                        .set(user)
                                                        .addOnCompleteListener(task1 -> {
                                                            Utils.hideView(progressBar);
                                                            if (task1.isSuccessful()) {
                                                                Toast.makeText(getApplicationContext(),
                                                                        "User registered. Please login!",
                                                                        Toast.LENGTH_SHORT).show();
                                                                finish();
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Utils.hideView(progressBar);
                                                            Utils.showView(btn_register);
                                                            Toast.makeText(getApplicationContext(),
                                                                    "Registration failed. Try again!",
                                                                    Toast.LENGTH_SHORT).show();
                                                        });
                                            }
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Utils.hideView(progressBar);
                                    Utils.showView(btn_register);
                                    Toast.makeText(getApplicationContext(),
                                            "Connection failed. Try again!",
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Passwords does not match!",
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Password should be 8 characters minimum!",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        "Please enter a valid email ID",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean isValidEmail(String email) {
        return (PatternsCompat.EMAIL_ADDRESS.matcher(email).matches());
    }
}