package com.easybook.activity;


import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentTransaction;

import com.easybook.R;
import com.easybook.fragment.AppointmentListFragment;
import com.easybook.fragment.OrganizationListFragment;
import com.easybook.fragment.ScheduleListFragment;
import com.easybook.util.RequestUtil;
import com.google.android.material.navigation.NavigationView;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Thread(() -> {
            while(true) {
                try {
                    RequestUtil.refreshToken(getSharedPreferences("auth", MODE_PRIVATE));
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                }
            }
        }).start();

        setContentView(R.layout.activity_home);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
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
            case R.id.my_organization_text_menu: {
                fragmentTransaction.replace(R.id.fragment_container_view, OrganizationListFragment.class, null);
                break;
            }
            case R.id.my_schedule_text_menu: {
                fragmentTransaction.replace(R.id.fragment_container_view, ScheduleListFragment.class, null);
                break;
            }
            case R.id.my_appointment_text_menu: {
                fragmentTransaction.replace(R.id.fragment_container_view, AppointmentListFragment.class, null);
                break;
            }
        }
        fragmentTransaction.commit();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
