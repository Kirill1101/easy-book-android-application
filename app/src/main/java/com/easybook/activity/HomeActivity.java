package com.easybook.activity;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.easybook.R;
import com.easybook.entity.UserCredential;
import com.easybook.fragment.AppointmentListFragment;
import com.easybook.fragment.OrganizationListFragment;
import com.easybook.fragment.ScheduleListFragment;
import com.easybook.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread refreshTokenThread = new Thread(() -> {
            while(true) {
                try {
                    RequestUtil.refreshToken(getSharedPreferences("auth", MODE_PRIVATE));
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                }
            }
        });
        refreshTokenThread.setDaemon(true);
        refreshTokenThread.start();

        setContentView(R.layout.activity_home);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.action_sign_in, R.string.action_sign_in);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (menuItem.getItemId()) {
            case R.id.my_organization_side_menu: {
                fragmentTransaction.replace(R.id.fragment_container_view, OrganizationListFragment.class, null);
                break;
            }
            case R.id.my_schedule_side_menu: {
                fragmentTransaction.replace(R.id.fragment_container_view, ScheduleListFragment.class, null);
                break;
            }
            case R.id.my_appointment_side_menu: {
                fragmentTransaction.replace(R.id.fragment_container_view, AppointmentListFragment.class, null);
                break;
            }
            case R.id.logout_of_account_side_menu: {
                logoutOfAccount();
                break;
            }
        }
        fragmentTransaction.commit();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void logoutOfAccount() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Вы уверены что хотите выйти?");
        dialog.setNegativeButton("Нет", (dialogInterface, i) -> dialogInterface.dismiss());
        dialog.setPositiveButton("Да", (dialogInterface, i) -> {
            SharedPreferences preferences = getSharedPreferences("auth", MODE_PRIVATE);
            SharedPreferences.Editor prefEditor = preferences.edit();
            prefEditor.putString("token", null);
            prefEditor.apply();
            startActivity(new Intent(HomeActivity.this, AuthActivity.class));
        });
        dialog.show();
    }
}
