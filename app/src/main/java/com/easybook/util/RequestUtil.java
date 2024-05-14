package com.easybook.util;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.View;

import androidx.annotation.NonNull;

import com.easybook.entity.UserCredential;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestUtil {
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    public static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    public static final String BASE_AUTH_URL = "http://195.133.32.250:8080/auth";
    public static final String BASE_ORGANIZATION_URL = "http://195.133.32.250:8080/organization";
    public static final String BASE_SCHEDULE_URL = "http://195.133.32.250:8080/schedule";
    public static final String BASE_SERVICE_URL = "http://195.133.32.250:8080/service";
    public static final String BASE_APPOINTMENT_URL = "http://195.133.32.250:8080/appointment";
    public static final String BASE_SLOT_URL = "http://195.133.32.250:8080/slot";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());



    public static void refreshToken(SharedPreferences preferences) {
        UserCredential userCredential = new UserCredential();
        userCredential.setLogin(preferences.getString("login", ""));
        userCredential.setPassword(preferences.getString("password", ""));
        RequestBody body = null;
        try {
            body = RequestBody.create(
                    OBJECT_MAPPER.writeValueAsString(userCredential), RequestUtil.MEDIA_TYPE);
        } catch (JsonProcessingException e) {
        }
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_AUTH_URL + "/token")
                .post(body)
                .build();
        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException(response.message());
                    }
                    SharedPreferences.Editor prefEditor = preferences.edit();
                    prefEditor.putString("token", response.body().string());
                    prefEditor.apply();
                } catch (IOException e) {
                }
            }
        });
    }

    public static void makeSnackBar(Activity activity, View view, String message) {
        if (activity != null && view != null) {
            activity.runOnUiThread(() -> Snackbar.make(view, message, Snackbar.LENGTH_LONG).show());
        }
    }
}
