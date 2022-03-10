package com.fikrathhassan.socialmediaproject.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fikrathhassan.socialmediaproject.R;
import com.fikrathhassan.socialmediaproject.model.AllVideo;
import com.fikrathhassan.socialmediaproject.util.Utils;
import com.fikrathhassan.socialmediaproject.model.UserVideo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.permissionx.guolindev.PermissionX;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PostVideoActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    ImageView usr_image;
    EditText usr_videoTitle;
    Button btn_chooseVideo, btn_postVideo;
    TextView usr_name, file_name;
    boolean isFileChoosed;

    ActivityResultLauncher<Intent> activityResultLaunch;
    Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        setContentView(R.layout.activity_post_video);

        initViews();
        initDialog();
        initListeners();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    void initViews() {
        usr_image = findViewById(R.id.usr_image);
        usr_name = findViewById(R.id.usr_name);
        usr_videoTitle = findViewById(R.id.usr_videoTitle);
        btn_chooseVideo = findViewById(R.id.btn_chooseVideo);
        file_name = findViewById(R.id.file_name);
        btn_postVideo = findViewById(R.id.btn_postVideo);
    }

    void initDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading...");
        progressDialog.setCancelable(false);
    }

    void initListeners() {
        Glide.with(this).load(Utils.getImage()).placeholder(R.drawable.person).into(usr_image);
        usr_name.setText(Utils.getName());
        btn_chooseVideo.setOnClickListener(view ->
                PermissionX.init(PostVideoActivity.this)
                        .permissions(Manifest.permission.READ_EXTERNAL_STORAGE)
                        .request((allGranted, grantedList, deniedList) -> {
                            if (allGranted) {
                                chooseVideo();
                            } else {
                                Toast.makeText(PostVideoActivity.this, "You need permission to access files", Toast.LENGTH_LONG).show();
                            }
                        }));
        activityResultLaunch = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        filePath = data.getData();
                        if (filePath != null) {
                            isFileChoosed = true;
                            file_name.setText(getFileName(filePath));
                        }
                    }
                });
        btn_postVideo.setOnClickListener(view -> {
            if (isFileChoosed) {
                postVideo();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Please select a file!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    void chooseVideo() {
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activityResultLaunch.launch(intent);
    }

    String getFileName(Uri uri) {
        String result;
        Cursor mCursor = getApplicationContext().getContentResolver()
                .query(uri, null, null, null, null);
        int indexedname = mCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        mCursor.moveToFirst();
        result = mCursor.getString(indexedname);
        uri = Uri.parse(result);
        mCursor.close();
        result = uri.getPath();
        int cut = result.lastIndexOf('/');
        if (cut != -1)
            result = result.substring(cut + 1);
        return result;
    }

    @SuppressLint("SetTextI18n")
    void postVideo() {
        toggleDialog();
        String video_title = usr_videoTitle.getText().toString();
        StorageReference imagesRefs = FirebaseStorage.getInstance().getReference().child("videos/"+System.currentTimeMillis()+"."+Utils.getFileExtension(PostVideoActivity.this, filePath));
        UploadTask uploadTask = imagesRefs.putFile(filePath);
        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return imagesRefs.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                if (downloadUri != null) {
                    DateFormat df = new SimpleDateFormat("dd/MM/yyyy, KK:mm:ss a", Locale.getDefault());
                    String currentDateAndTime = df.format(new Date());
                    UserVideo usrVideo = new UserVideo(video_title, currentDateAndTime, downloadUri.toString());
                    WriteBatch batch = FirebaseFirestore.getInstance().batch();
                    DocumentReference userUpload = FirebaseFirestore.getInstance().collection("users/" + Utils.getEmail() + "/videos").document();
                    batch.set(userUpload, usrVideo);
                    DocumentReference allUpload = FirebaseFirestore.getInstance().collection("uploads").document();
                    AllVideo allVideo = new AllVideo(video_title, currentDateAndTime, downloadUri.toString(), Utils.getEmail(), Utils.getImage(), Utils.getName());
                    batch.set(allUpload, allVideo);
                    batch.commit()
                            .addOnCompleteListener(task1 -> {
                                Toast.makeText(getApplicationContext(), "Posted!", Toast.LENGTH_SHORT).show();
                                usr_videoTitle.setText("");
                                filePath = null;
                                file_name.setText("No file choosed");
                                isFileChoosed = false;
                            })
                            .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Post failed!", Toast.LENGTH_SHORT).show());
                } else {
                    Toast.makeText(getApplicationContext(), "Video link not found!", Toast.LENGTH_SHORT).show();
                }
                toggleDialog();
            }
        });
    }

    void toggleDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        } else {
            progressDialog.show();
        }
    }
}