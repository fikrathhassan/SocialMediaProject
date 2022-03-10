package com.fikrathhassan.socialmediaproject.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.fikrathhassan.socialmediaproject.R;
import com.fikrathhassan.socialmediaproject.util.Utils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.apache.commons.codec.digest.DigestUtils;

public class SignInActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    EditText usr_email, usr_password;
    Button btn_signIn, btn_register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_signin);

        initViews();
        initDialog();
        initListeners();
    }

    void initViews() {
        usr_email = findViewById(R.id.usr_email);
        usr_password = findViewById(R.id.usr_password);
        btn_signIn = findViewById(R.id.btn_signIn);
        btn_register = findViewById(R.id.btn_register);
    }

    void initDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Checking...");
        progressDialog.setCancelable(false);
    }

    void initListeners() {
        btn_signIn.setOnClickListener(view -> {
            String email = usr_email.getText().toString();
            String password = usr_password.getText().toString();
            String encryptedPassword = DigestUtils.md5Hex(password);
            if (!email.isEmpty()) {
                if (!password.isEmpty()) {
                    toggleDialog();
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(email)
                            .get()
                            .addOnCompleteListener(task -> {
                                toggleDialog();
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document != null) {
                                        if (document.exists()) {
                                            if (document.getString("password").equals(encryptedPassword)) {
                                                String name = document.getString("name");
                                                String image = document.getString("image");
                                                Utils.setLogin(name, email, image, encryptedPassword, true);
                                                startActivity(new Intent(SignInActivity.this, MainActivity.class));
                                                finish();
                                            } else {
                                                Toast.makeText(getApplicationContext(),
                                                        "Your password is incorrect!",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    "User not found!",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            })
                            .addOnFailureListener(e -> {
                                toggleDialog();
                                Toast.makeText(getApplicationContext(),
                                        "Connection failed!",
                                        Toast.LENGTH_SHORT).show();
                            });
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Please enter password",
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(),
                        "Please enter an email ID",
                        Toast.LENGTH_SHORT).show();
            }
        });
        btn_register.setOnClickListener(view -> startActivity(new Intent(SignInActivity.this, RegistrationActivity.class)));
    }

    void toggleDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else {
            progressDialog.show();
        }
    }

}
