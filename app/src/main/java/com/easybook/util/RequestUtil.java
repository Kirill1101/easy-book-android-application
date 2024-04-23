package com.easybook.util;

import android.content.SharedPreferences;

import com.easybook.entity.UserCredential;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.Optional;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RequestUtil {
    public static final OkHttpClient HTTP_CLIENT = new OkHttpClient();
    public static final MediaType MEDIA_TYPE = MediaType.get("application/json; charset=utf-8");
    public static final String BASE_AUTH_URL = "http://195.133.32.250:8080/auth";
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String getTokenByUserCredential(UserCredential userCredential)
            throws IOException {
        RequestBody body = RequestBody.create(
                OBJECT_MAPPER.writeValueAsString(userCredential), MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(BASE_AUTH_URL + "/token")
                .post(body)
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(response.message());
            }
            return response.body().string();
        }
    }

    public static void registerUser(UserCredential userCredential) throws IOException {
        RequestBody body = RequestBody.create(
                OBJECT_MAPPER.writeValueAsString(userCredential), MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(BASE_AUTH_URL + "/register")
                .post(body)
                .build();
        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException(response.message());
            }
        }
    }
}
