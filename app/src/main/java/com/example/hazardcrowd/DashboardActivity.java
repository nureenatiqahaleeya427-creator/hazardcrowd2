package com.example.hazardcrowd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView; // Import ini
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        ImageView btnProfile = findViewById(R.id.btnProfileTop);

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {

                Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }


        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }

        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();


                if (itemId == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.nav_maps) {
                    selectedFragment = new MapsFragment();
                } else if (itemId == R.id.nav_report) {
                    selectedFragment = new ReportFragment();
                }
                else if (itemId == R.id.nav_about) {
                    selectedFragment = new AboutFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            });
        }
    }
}