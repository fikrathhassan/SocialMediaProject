package com.fikrathhassan.socialmediaproject.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.fikrathhassan.socialmediaproject.R;
import com.fikrathhassan.socialmediaproject.model.AllVideo;

import java.util.List;

public class AllVideosAdapter extends RecyclerView.Adapter<AllVideosAdapter.CustomViewHolder> {

    Context context;
    List<AllVideo> dataList;
    OnShareClickListner onShareClickListner;

    public AllVideosAdapter(Context context, List<AllVideo> dataList, OnShareClickListner onShareClickListner) {
        this.context = context;
        this.dataList = dataList;
        this.onShareClickListner = onShareClickListner;
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder {
        VideoView usr_video;
        ImageView usr_image;
        TextView usr_name, usr_videoTitle, usr_videoUploadTime;
        Button btn_like, btn_comment, btn_share;

        CustomViewHolder(View itemView) {
            super(itemView);
            usr_image = itemView.findViewById(R.id.usr_image);
            usr_name = itemView.findViewById(R.id.usr_name);
            usr_videoTitle = itemView.findViewById(R.id.usr_videoTitle);
            usr_videoUploadTime = itemView.findViewById(R.id.usr_videoUploadTime);
            usr_video = itemView.findViewById(R.id.usr_video);
            btn_share = itemView.findViewById(R.id.btn_share);
            btn_like = itemView.findViewById(R.id.btn_like);
            btn_like.setOnClickListener(view -> Toast.makeText(context,
                    "You have clicked LIKE",
                    Toast.LENGTH_SHORT).show());
            btn_comment = itemView.findViewById(R.id.btn_comment);
            btn_comment.setOnClickListener(view -> Toast.makeText(context,
                    "You have clicked COMMENT",
                    Toast.LENGTH_SHORT).show());
            btn_share.setOnClickListener(view -> onShareClickListner.onShareClicked(getBindingAdapterPosition()));
        }
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.rv_item, parent, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder holder, int position) {
        AllVideo response = dataList.get(position);
        Glide.with(context).load(response.getUser_image()).placeholder(R.drawable.person).into(holder.usr_image);
        holder.usr_name.setText(response.getUser_name());
        if (response.getVideo_title().isEmpty()) {
            holder.usr_videoTitle.setText("N/A");
        } else {
            holder.usr_videoTitle.setText(response.getVideo_title());
        }
        holder.usr_videoUploadTime.setText(response.getUpload_time());
        MediaController mediaController = new MediaController(context);
        holder.usr_video.setMediaController(mediaController);
        holder.usr_video.setVideoURI(Uri.parse(response.getVideo_url()));
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void clearItems() {
        int size = dataList.size();
        dataList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public interface OnShareClickListner {
        void onShareClicked(int position);
    }
}