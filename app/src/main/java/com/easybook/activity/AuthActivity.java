package com.easybook.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.easybook.R;
import com.easybook.entity.UserCredential;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthActivity extends AppCompatActivity {
    private SharedPreferences preferences;
    private ObjectMapper objectMapper;
    private Button buttonSignIn, buttonRegister;
    private RelativeLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        preferences = getSharedPreferences("auth", MODE_PRIVATE);

        objectMapper = new ObjectMapper();

        buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonRegister = findViewById(R.id.buttonRegister);

        root = findViewById(R.id.root_element_main);

        buttonSignIn.setOnClickListener(view -> showSignInWindows());

        buttonRegister.setOnClickListener(view -> showSignUpWindows());
    }

    private void showSignInWindows() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Вход");
        dialog.setMessage("Введите данные для входа");

        LayoutInflater inflater = LayoutInflater.from(this);
        View signInWindow = inflater.inflate(R.layout.sign_in, null);
        dialog.setView(signInWindow);

        final MaterialEditText login = signInWindow.findViewById(R.id.loginField);
        final MaterialEditText password = signInWindow.findViewById(R.id.passField);

        dialog.setNegativeButton("Отменить", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Войти", (dialogInterface, i) -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(login.getText()).toString())) {
                Snackbar.make(root, "Введите логин", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (Objects.requireNonNull(password.getText()).toString().length() < 3) {
                Snackbar.make(root, "Введите пароль длинной 3 и более символов",
                        Snackbar.LENGTH_SHORT).show();
                return;
            }

            UserCredential userCredential = new UserCredential();
            userCredential.setLogin(login.getText().toString());
            userCredential.setPassword(password.getText().toString());

            try {
                getTokenAndWriteItToPreferences(userCredential);
            } catch (JsonProcessingException e) {
                Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }

    private void showSignUpWindows() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Регистрация");
        dialog.setMessage("Введите данные для регистрации");

        LayoutInflater inflater = LayoutInflater.from(this);
        View registerWindow = inflater.inflate(R.layout.sign_up, null);
        dialog.setView(registerWindow);

        final MaterialEditText login = registerWindow.findViewById(R.id.loginField);
        final MaterialEditText email = registerWindow.findViewById(R.id.emailField);
        final MaterialEditText password = registerWindow.findViewById(R.id.passField);

        dialog.setNegativeButton("Отменить", (dialogInterface, i) -> dialogInterface.dismiss());

        dialog.setPositiveButton("Продолжить", (dialogInterface, i) -> {
            if (TextUtils.isEmpty(Objects.requireNonNull(email.getText()).toString())) {
                Snackbar.make(root, "Введите почту", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (TextUtils.isEmpty(Objects.requireNonNull(login.getText()).toString())) {
                Snackbar.make(root, "Введите логин", Snackbar.LENGTH_LONG).show();
                return;
            }

            if (Objects.requireNonNull(password.getText()).toString().length() < 3) {
                Snackbar.make(root, "Введите пароль длинной 3 и более символов",
                        Snackbar.LENGTH_SHORT).show();
                return;
            }

            UserCredential userCredential = new UserCredential();
            userCredential.setLogin(login.getText().toString());
            userCredential.setEmail(email.getText().toString());
            userCredential.setPassword(password.getText().toString());

            try {
                registerUser(userCredential);
                getTokenAndWriteItToPreferences(userCredential);
            } catch (JsonProcessingException e) {
                Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    public void getTokenAndWriteItToPreferences(UserCredential userCredential) throws JsonProcessingException {
        RequestBody body = RequestBody.create(
                RequestUtil.OBJECT_MAPPER.writeValueAsString(userCredential), RequestUtil.MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_AUTH_URL + "/token")
                .post(body)
                .build();
        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    if (!response.isSuccessful()) {
                        throw new IOException(response.message());
                    }

                    SharedPreferences.Editor prefEditor = preferences.edit();
                    prefEditor.putString("token", response.body().string());
                    prefEditor.putString("login", userCredential.getLogin());
                    prefEditor.putString("password", userCredential.getPassword());
                    prefEditor.putString("email", userCredential.getEmail());
                    prefEditor.apply();

                    startActivity(new Intent(AuthActivity.this, HomeActivity.class));
                } catch (IOException e) {
                    Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    public void registerUser(UserCredential userCredential) throws JsonProcessingException {
        RequestBody body = RequestBody.create(
                RequestUtil.OBJECT_MAPPER.writeValueAsString(userCredential), RequestUtil.MEDIA_TYPE);
        Request request = new Request.Builder()
                .url(RequestUtil.BASE_AUTH_URL + "/register")
                .post(body)
                .build();
        RequestUtil.HTTP_CLIENT.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                getTokenAndWriteItToPreferences(userCredential);
            }
        });
    }
}
