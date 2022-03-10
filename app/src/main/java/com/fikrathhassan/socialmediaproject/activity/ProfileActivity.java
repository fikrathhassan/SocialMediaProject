package com.fikrathhassan.socialmediaproject.activity;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.fikrathhassan.socialmediaproject.R;
import com.fikrathhassan.socialmediaproject.util.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.permissionx.guolindev.PermissionX;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    MenuItem progressBar;
    ProgressDialog progressDialog;
    BottomSheetDialog bottomSheetDialog;
    ImageView usr_image;
    TextView usr_name, usr_email;
    Button btn_signOut;

    String name,email,image;

    ActivityResultLauncher<Intent> activityResultLaunch;
    Uri filePath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        setContentView(R.layout.activity_profile);

        initViews();
        initDialog();
        initListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        progressBar = menu.findItem(R.id.action_progress);
        hideProgress();
        return super.onPrepareOptionsMenu(menu);
    }

    void initViews() {
        usr_image = findViewById(R.id.usr_image);
        usr_name = findViewById(R.id.usr_name);
        usr_email = findViewById(R.id.usr_email);
        btn_signOut = findViewById(R.id.btn_signOut);
    }

    void initListeners() {
        toggleDialog();
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(Utils.getEmail())
                .get()
                .addOnCompleteListener(task -> {
                    toggleDialog();
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            name = document.getString("name");
                            email = document.getString("email");
                            image = document.getString("image");
                        }
                        Glide.with(this).load(image).placeholder(R.drawable.person).into(usr_image);
                        usr_name.setText(name);
                        usr_email.setText(email);
                    }
                })
                .addOnFailureListener(e -> {
                    hideProgress();
                    Toast.makeText(getApplicationContext(),
                            "Connection failed!",
                            Toast.LENGTH_SHORT).show();
                });
        usr_image.setOnClickListener(view -> {
            if (image.isEmpty()) {
                checkPermission();
            } else {
                bottomSheetDialog.show();
            }
        });
        activityResultLaunch = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        filePath = data.getData();
                        if (filePath != null) {
                            uploadImage();
                        }
                    }
                });
        btn_signOut.setOnClickListener(view -> {
            if (progressBar.isVisible()) {
                Toast.makeText(getApplicationContext(),
                        "Please wait to finish updating...",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            Utils.setLogin("","", "", "", false);
            finish();
        });
    }

    void initDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);

        bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.dialog_profile_image);
        LinearLayout action_change = bottomSheetDialog.findViewById(R.id.action_change);
        if (action_change != null) {
            action_change.setOnClickListener(view -> {
                bottomSheetDialog.cancel();
                checkPermission();
            });
        }
        LinearLayout action_remove = bottomSheetDialog.findViewById(R.id.action_remove);
        if (action_remove != null) {
            action_remove.setOnClickListener(view -> {
                bottomSheetDialog.cancel();
                showProgress();
                Map<String, Object> user = new HashMap<>();
                user.put("image", "");
                FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(Utils.getEmail())
                        .set(user, SetOptions.merge())
                        .addOnSuccessListener(aVoid -> {
                            hideProgress();
                            Toast.makeText(getApplicationContext(), "Profile image removed!", Toast.LENGTH_SHORT).show();
                            Glide.with(ProfileActivity.this).load(R.drawable.person).into(usr_image);
                            image = "";
                        })
                        .addOnFailureListener(e -> {
                            hideProgress();
                            Toast.makeText(getApplicationContext(), "Profile image is not removed!", Toast.LENGTH_SHORT).show();
                        });
            });
        }
    }

    void checkPermission() {
        PermissionX.init(ProfileActivity.this)
                .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        chooseImage();
                    } else {
                        Toast.makeText(ProfileActivity.this, "You need permission to access files", Toast.LENGTH_LONG).show();
                    }
                });
    }

    void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLaunch.launch(intent);
    }

    void uploadImage() {
        showProgress();
        StorageReference imagesRefs = FirebaseStorage.getInstance().getReference().child("images/"+Utils.getEmail().replaceAll("[^a-zA-Z0-9]", "")+"."+Utils.getFileExtension(ProfileActivity.this, filePath));
        UploadTask uploadTask = imagesRefs.putFile(filePath);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return imagesRefs.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            hideProgress();
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                if (downloadUri != null) {
                    Map<String, Object> user = new HashMap<>();
                    user.put("image", downloadUri.toString());
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(Utils.getEmail())
                            .set(user, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getApplicationContext(), "Profile image updated!", Toast.LENGTH_SHORT).show();
                                try {
                                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                                    Glide.with(ProfileActivity.this).load(bitmap).placeholder(R.mipmap.ic_launcher).into(usr_image);
                                    image = downloadUri.toString();
                                    Utils.setImage(image);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Profile image is not updated!", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getApplicationContext(), "Profile image link not found!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    void showProgress() {
        if (!progressBar.isVisible()) {
            progressBar.setVisible(true);
        }
    }

    void hideProgress() {
        if (progressBar.isVisible()) {
            progressBar.setVisible(false);
        }
    }

    void toggleDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else {
            progressDialog.show();
        }
    }

}
