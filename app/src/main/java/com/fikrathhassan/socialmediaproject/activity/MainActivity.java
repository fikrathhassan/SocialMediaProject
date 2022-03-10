package com.fikrathhassan.socialmediaproject.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.fikrathhassan.socialmediaproject.R;
import com.fikrathhassan.socialmediaproject.adapter.AllVideosAdapter;
import com.fikrathhassan.socialmediaproject.model.AllVideo;
import com.fikrathhassan.socialmediaproject.util.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AllVideosAdapter.OnShareClickListner {

    BottomNavigationView navigation;
    ProgressBar progressBar;
    AllVideosAdapter videosAdapter;
    List<AllVideo> videoList = new ArrayList<>();
    RecyclerView recyclerView;

    boolean doubleBackToExitPressedOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }

        setContentView(R.layout.activity_main);

        initViews();
        initListeners();
        initRecyclerView();
        getVideos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Utils.isSignedIn()) {
            startActivity(new Intent(MainActivity.this, SignInActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            getVideos();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onShareClicked(int position) {
        AllVideo response = videoList.get(position);
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "Watch " + response.getVideo_title());
        share.putExtra(Intent.EXTRA_TEXT, "Watch " + response.getVideo_title() +
                " uploaded on " + response.getUpload_time() +
                " at " + response.getVideo_url());
        startActivity(Intent.createChooser(share, "Share"));
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler(Looper.getMainLooper()).postDelayed(() -> doubleBackToExitPressedOnce=false, 2000);
    }

    void initViews() {
        navigation = findViewById(R.id.navigation);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
    }

    void initListeners() {
        navigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.navigation_myPost) {
                startActivity(new Intent(MainActivity.this, MyPostActivity.class));
            } else if (id == R.id.navigation_createPost) {
                startActivity(new Intent(MainActivity.this, PostVideoActivity.class));
            } else if (id == R.id.navigation_account) {
                startActivity(new Intent(MainActivity.this, ProfileActivity.class));
            } else {
                return false;
            }
            return true;
        });
    }

    void initRecyclerView() {
        RecyclerView.LayoutManager layout
                = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        videosAdapter = new AllVideosAdapter(MainActivity.this, videoList, MainActivity.this);
        recyclerView.setAdapter(videosAdapter);
    }

    void getVideos() {
        videoList.clear();
        videosAdapter.clearItems();
        Utils.showView(progressBar);
        FirebaseFirestore.getInstance()
                .collection("uploads")
                .orderBy("upload_time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    Utils.hideView(progressBar);
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                AllVideo video = new AllVideo(document.getString("video_title"),
                                        document.getString("upload_time"),
                                        document.getString("video_url"),
                                        document.getString("user_email"),
                                        document.getString("user_image"),
                                        document.getString("user_name"));
                                videoList.add(video);
                            }
                        }
                        if (videoList.size() > 0) {
                            videosAdapter.notifyItemRangeInserted(0, videoList.size());
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "No videos available!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Utils.hideView(progressBar);
                    Toast.makeText(getApplicationContext(),
                            "Connection failed!",
                            Toast.LENGTH_SHORT).show();
                });
    }

}