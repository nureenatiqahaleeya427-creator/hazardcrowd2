package com.example.hazardcrowd;

import android.content.Intent;
import android.content.SharedPreferences; // Import tambahan untuk simpan session
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    EditText etName, etEmail, etPassword, etConfirmPassword;
    Button btnRegister, btnGoogleSignUp;
    TextView tvLogin;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    // Activity Result Launcher for Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                handleGoogleSignInResult(task);
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In
        // Pastikan R.string.default_web_client_id wujud dalam strings.xml
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize views
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        btnGoogleSignUp = findViewById(R.id.btnGoogleSignUp);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> attemptRegister());

        btnGoogleSignUp.setOnClickListener(v -> signUpWithGoogle());

        tvLogin.setOnClickListener(v -> {
            // Go back to login page
            finish();
        });
    }

    private void signUpWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "Google Sign-Up successful: " + account.getEmail());
            firebaseAuthWithGoogle(account.getIdToken());
        } catch (ApiException e) {
            Log.w(TAG, "Google Sign-Up failed code=" + e.getStatusCode(), e);
            Toast.makeText(this, "Google Sign-Up failed. Check Logcat.", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Toast.makeText(this, "Welcome, " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();
                            // Save session & Navigate
                            saveSession(user.getEmail());
                            navigateToDashboard();
                        }
                    } else {
                        Log.w(TAG, "signInWithCredential:failure", task.getException());
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void attemptRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validateInput(name, email, password, confirmPassword)) {
            return;
        }

        btnRegister.setEnabled(false);
        btnRegister.setText("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update display name
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                        // Save session & Navigate
                                        saveSession(user.getEmail());
                                        navigateToDashboard();
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        btnRegister.setEnabled(true);
                        btnRegister.setText("Create Account");
                    }
                });
    }

    private boolean validateInput(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            etConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    // Fungsi tambahan untuk konsisten dengan ProfileActivity
    private void saveSession(String email) {
        SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("is_logged_in", true);
        editor.putString("user_email", email);
        editor.apply();
    }

    // --- FIX UTAMA DI SINI ---
    private void navigateToDashboard() {
        // Kita mesti buka Activity (DashboardActivity), bukan Fragment (HomeFragment)
        Intent intent = new Intent(RegisterActivity.this, DashboardActivity.class);

        // Membersihkan stack supaya user tak boleh tekan Back ke halaman Register
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}