package com.fikrathhassan.socialmediaproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fikrathhassan.socialmediaproject.R;
import com.fikrathhassan.socialmediaproject.adapter.UserVideosAdapter;
import com.fikrathhassan.socialmediaproject.model.UserVideo;
import com.fikrathhassan.socialmediaproject.util.Utils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class MyPostActivity extends AppCompatActivity implements UserVideosAdapter.OnShareClickListner {

    ProgressBar progressBar;
    UserVideosAdapter videosAdapter;
    List<UserVideo> videoList = new ArrayList<>();
    RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        setContentView(R.layout.activity_my_post);

        initViews();
        initRecyclerView();
        getVideos();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mypost, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_add_post) {
            startActivity(new Intent(this, PostVideoActivity.class));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onShareClicked(int position) {
        UserVideo response = videoList.get(position);
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, "Watch " + response.getVideo_title());
        share.putExtra(Intent.EXTRA_TEXT, "Watch " + response.getVideo_title() +
                " uploaded on " + response.getUpload_time() +
                " at " + response.getVideo_url());
        startActivity(Intent.createChooser(share, "Share"));
    }

    void initViews() {
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
    }

    void initRecyclerView() {
        RecyclerView.LayoutManager layout
                = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        videosAdapter = new UserVideosAdapter(MyPostActivity.this, videoList, MyPostActivity.this);
        recyclerView.setAdapter(videosAdapter);
    }

    void getVideos() {
        videoList.clear();
        videosAdapter.clearItems();
        Utils.showView(progressBar);
        FirebaseFirestore.getInstance()
                .collection("users/"+Utils.getEmail()+"/videos")
                .orderBy("upload_time", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    Utils.hideView(progressBar);
                    if (task.isSuccessful()) {
                        if (task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                UserVideo video = new UserVideo(document.getString("video_title"),
                                        document.getString("upload_time"),
                                        document.getString("video_url"));
                                videoList.add(video);
                            }
                        }
                        if (videoList.size() > 0) {
                            videosAdapter.notifyItemRangeInserted(0, videoList.size());
                        } else {
                            Toast.makeText(getApplicationContext(),
                                    "No videos uploaded!",
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
