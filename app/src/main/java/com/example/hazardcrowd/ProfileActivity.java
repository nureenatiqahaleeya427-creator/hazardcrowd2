package com.example.hazardcrowd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

// Import Firebase
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    // Deklarasi Variable untuk Text
    private TextView tvUserName, tvProfileEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // --- 1. INISIALISASI VIEW (Cari ID dari XML baru) ---
        tvUserName = findViewById(R.id.tvUserName);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);

        TextView btnEditProfile = findViewById(R.id.btnEditProfile);
        TextView btnNotifications = findViewById(R.id.btnNotifications);
        TextView btnMapSettings = findViewById(R.id.btnMapSettings);

        Button btnSignOut = findViewById(R.id.btnSignOut);
        ImageView btnBack = findViewById(R.id.btnBack);

        // --- 2. LOAD DATA PENGGUNA DARI FIREBASE ---
        loadUserProfile();

        // --- 3. SETUP BUTANG KLIK ---

        // Butang Back
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish()); // Tutup activity, balik dashboard
        }

        // Butang Logout
        if (btnSignOut != null) {
            btnSignOut.setOnClickListener(v -> performLogout());
        }

        // Menu: Edit Profile (Placeholder)
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v ->
                    Toast.makeText(this, "Edit Profile clicked", Toast.LENGTH_SHORT).show()
            );
        }

        // Menu: Notifications (Placeholder)
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v ->
                    Toast.makeText(this, "Notifications clicked", Toast.LENGTH_SHORT).show()
            );
        }

        // Menu: Map Settings (Placeholder)
        if (btnMapSettings != null) {
            btnMapSettings.setOnClickListener(v ->
                    Toast.makeText(this, "Map Settings clicked", Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void loadUserProfile() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Set Email
            String email = user.getEmail();
            if (email != null) {
                tvProfileEmail.setText(email);

                // Pilihan: Jika Nama tiada, kita ambil nama dari depan email (sebelum @)
                // Contoh: user@safecity.com -> Username: user
                String defaultName = email.split("@")[0];

                // Jika user ada set DisplayName dalam Firebase, guna itu. Kalau tak, guna email.
                String name = user.getDisplayName();
                if (name != null && !name.isEmpty()) {
                    tvUserName.setText(name);
                } else {
                    tvUserName.setText(defaultName);
                }
            }
        }
    }

    private void performLogout() {
        // --- 1. LOGOUT FIREBASE ---
        FirebaseAuth.getInstance().signOut();

        // --- 2. CLEAR SESSION (SharedPrefs) ---
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();

        // --- 3. BALIK KE LOGIN SCREEN ---
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }
}