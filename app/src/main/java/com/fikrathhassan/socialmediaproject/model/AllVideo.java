package com.fikrathhassan.socialmediaproject.model;

import java.io.Serializable;

public class AllVideo implements Serializable {

    private String video_title, upload_time, video_url, user_email, user_image, user_name;

    public AllVideo(String video_title, String upload_time, String video_url, String user_email, String user_image, String user_name) {
        this.video_title = video_title;
        this.upload_time = upload_time;
        this.video_url = video_url;
        this.user_email = user_email;
        this.user_image = user_image;
        this.user_name = user_name;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUpload_time() {
        return upload_time;
    }

    public void setUpload_time(String upload_time) {
        this.upload_time = upload_time;
    }

    public String getVideo_title() {
        return video_title;
    }

    public void setVideo_title(String video_title) {
        this.video_title = video_title;
    }

    public String getVideo_url() {
        return video_url;
    }

    public void setVideo_url(String video_url) {
        this.video_url = video_url;
    }

}
