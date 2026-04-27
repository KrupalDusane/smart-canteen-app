package com.example.nmimscanteenapp.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.admin.AdminActivity;
import com.example.nmimscanteenapp.admin.AdminLoginActivity;
import com.example.nmimscanteenapp.student.HomeActivity;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * MainActivity handles the custom SAP login system with auto-registration.
 * It uses Firestore for user management without Firebase Authentication.
 */
public class MainActivity extends AppCompatActivity {

    private EditText sapIdInput, passwordInput;
    private FirebaseFirestore db;
    private SharedPreferences sharedPreferences;
    private ProgressBar loginProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check for Auto-Login
        sharedPreferences = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE);
        String savedSapId = sharedPreferences.getString("sapId", null);
        String savedName = sharedPreferences.getString("name", null);
        String savedRole = sharedPreferences.getString("role", "student");

        if (savedSapId != null) {
            Intent intent;
            if ("admin".equals(savedRole)) {
                intent = new Intent(MainActivity.this, AdminActivity.class);
            } else {
                intent = new Intent(MainActivity.this, HomeActivity.class);
            }
            intent.putExtra("sapId", savedSapId);
            intent.putExtra("name", savedName);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        sapIdInput = findViewById(R.id.sapIdInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginProgress = findViewById(R.id.loginProgress);
        Button loginBtn = findViewById(R.id.loginBtn);
        TextView forgotPasswordTv = findViewById(R.id.forgotPasswordTv);
        TextView signUpTv = findViewById(R.id.signUpTv);
        Button adminLoginBtn = findViewById(R.id.adminLoginBtn);

        loginBtn.setOnClickListener(v -> handleLogin());

        // Redirect to AdminLoginActivity
        adminLoginBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AdminLoginActivity.class);
            startActivity(intent);
        });

        // Redirect to SignupActivity
        signUpTv.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        // Redirect to ForgotPasswordActivity
        forgotPasswordTv.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String sapId = sapIdInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // 1. Validate SAP ID (must be exactly 11 digits)
        if (TextUtils.isEmpty(sapId) || sapId.length() != 11 || !sapId.matches("\\d+")) {
            sapIdInput.setError("SAP ID must be exactly 11 digits");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            return;
        }

        setLoading(true);

        // 2. Check Firestore: db.collection("users").document(sapId).get()
        db.collection("users").document(sapId).get()
                .addOnCompleteListener(task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            // User EXISTS: Fetch password and isFirstLogin
                            String dbPassword = document.getString("password");
                            Boolean isFirstLogin = document.getBoolean("isFirstLogin");

                            // Match password
                            if (password.equals(dbPassword)) {
                                String name = document.getString("name");
                                String role = document.getString("role");
                                if (name == null) name = sapId;
                                if (role == null) role = "student";

                                // Save to SharedPreferences for Auto-Login
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putString("sapId", sapId);
                                editor.putString("name", name);
                                editor.putString("role", role);
                                editor.apply();

                                Toast.makeText(MainActivity.this, "Welcome " + name, Toast.LENGTH_SHORT).show();

                                Intent intent;
                                if ("admin".equals(role)) {
                                    intent = new Intent(MainActivity.this, AdminActivity.class);
                                } else if (isFirstLogin != null && isFirstLogin) {
                                    intent = new Intent(MainActivity.this, ChangePasswordActivity.class);
                                } else {
                                    intent = new Intent(MainActivity.this, HomeActivity.class);
                                }
                                intent.putExtra("sapId", sapId);
                                intent.putExtra("name", name);
                                startActivity(intent);
                                finish();
                            } else {
                                // If password does NOT match: Show Snackbar: "Invalid SAP ID or Password"
                                Snackbar.make(findViewById(android.R.id.content), "Invalid SAP ID or Password", Snackbar.LENGTH_LONG).show();
                            }
                        } else {
                            // User DOES NOT exist
                            Snackbar.make(findViewById(android.R.id.content), "Invalid SAP ID or Password", Snackbar.LENGTH_LONG).show();
                        }
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                        Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean isLoading) {
        if (loginProgress != null) {
            loginProgress.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        findViewById(R.id.loginBtn).setEnabled(!isLoading);
        sapIdInput.setEnabled(!isLoading);
        passwordInput.setEnabled(!isLoading);
    }

    private void createNewUser(String sapId) {
        // Create user with default values
        Map<String, Object> user = new HashMap<>();
        user.put("sapId", sapId);
        user.put("password", "default123");
        user.put("isFirstLogin", true);
        user.put("role", "student"); // Default role

        db.collection("users").document(sapId).set(user)
                .addOnSuccessListener(aVoid -> {
                    // Show Toast: "Account created. Use default password"
                    Toast.makeText(MainActivity.this, "Account created. Use default password", Toast.LENGTH_LONG).show();
                    
                    // Immediately redirect to ChangePasswordActivity and Pass SAP ID via Intent
                    Intent intent = new Intent(MainActivity.this, ChangePasswordActivity.class);
                    intent.putExtra("sapId", sapId);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(MainActivity.this, "Failed to create account: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
