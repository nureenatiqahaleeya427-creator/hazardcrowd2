package com.example.hazardcrowd;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View; // Penting: Import View
import android.widget.LinearLayout; // Atau Import LinearLayout
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);


        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }


        View btnGithub = findViewById(R.id.tvGithub);


        if (btnGithub != null) {
            btnGithub.setOnClickListener(v -> {
                String url = "https://github.com/hazardcrowd/hazardcrowd-app";
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(AboutActivity.this, "Browser not found", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}