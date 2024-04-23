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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.easybook.R;
import com.easybook.entity.UserCredential;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.util.Objects;

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

            if (Objects.requireNonNull(password.getText()).toString().length() < 5) {
                Snackbar.make(root, "Введите пароль длинной 5 и более символов",
                        Snackbar.LENGTH_SHORT).show();
                return;
            }

            UserCredential userCredential = new UserCredential();
            userCredential.setLogin(login.getText().toString());
            userCredential.setPassword(password.getText().toString());

            try {
                String token = RequestUtil.getTokenByUserCredential(userCredential);

                SharedPreferences.Editor prefEditor = preferences.edit();
                prefEditor.putString("token", token);
                prefEditor.apply();

                startActivity(new Intent(AuthActivity.this, HomeActivity.class));
            } catch (IOException e) {
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
                Snackbar.make(root, "Введите почту", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(Objects.requireNonNull(login.getText()).toString())) {
                Snackbar.make(root, "Введите логин", Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (Objects.requireNonNull(password.getText()).toString().length() < 5) {
                Snackbar.make(root, "Введите пароль длинной 5 и более символов",
                        Snackbar.LENGTH_SHORT).show();
                return;
            }

            UserCredential userCredential = new UserCredential();
            userCredential.setLogin(login.getText().toString());
            userCredential.setPassword(password.getText().toString());

            try {
                RequestUtil.registerUser(userCredential);
                String token = RequestUtil.getTokenByUserCredential(userCredential);

                SharedPreferences.Editor prefEditor = preferences.edit();
                prefEditor.putString("token", token);
                prefEditor.apply();

                startActivity(new Intent(AuthActivity.this, HomeActivity.class));
            } catch (IOException e) {
                Snackbar.make(root, e.getMessage(), Snackbar.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }
}
