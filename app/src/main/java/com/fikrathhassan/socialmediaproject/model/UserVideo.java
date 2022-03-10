package com.fikrathhassan.socialmediaproject.model;

import java.io.Serializable;

public class UserVideo implements Serializable {

    private String video_title, upload_time, video_url;

    public UserVideo(String video_title, String upload_time, String video_url) {
        this.video_title = video_title;
        this.upload_time = upload_time;
        this.video_url = video_url;
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
