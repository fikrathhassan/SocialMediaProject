package com.fikrathhassan.socialmediaproject.util;

import static android.content.Context.MODE_PRIVATE;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Utils {

    private static SharedPreferences prefs;

    public static void getUserPrefs(Context context) {
        prefs = context.getSharedPreferences("USER", MODE_PRIVATE);
    }

    public static void setLogin(String name, String email, String image, String password, boolean isSignedIn) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("image", image);
        editor.putString("password", password);
        editor.putBoolean("isSignedIn", isSignedIn);
        editor.apply();
    }

    public static String getName() {
        return prefs.getString("name", "");
    }

    public static String getEmail() {
        return prefs.getString("email", "");
    }

    public static void setImage(String image) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("image", image);
        editor.apply();
    }
    public static String getImage() {
        return prefs.getString("image", "");
    }

    public static String getPassword() {
        return prefs.getString("password", "");
    }

    public static boolean isSignedIn() {
        return prefs.getBoolean("isSignedIn", false);
    }

    public static void showView(View view) {
        if (view.getVisibility() != View.VISIBLE) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void hideView(View view) {
        if (view.getVisibility() != View.GONE) {
            view.setVisibility(View.GONE);
        }
    }

    public static String getFileExtension(Context context, Uri file) {
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(file));
    }

}
