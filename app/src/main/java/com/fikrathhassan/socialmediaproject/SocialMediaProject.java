package com.fikrathhassan.socialmediaproject;

import android.app.Application;

import com.fikrathhassan.socialmediaproject.util.Utils;

public class SocialMediaProject extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Utils.getUserPrefs(getApplicationContext());
    }

}
