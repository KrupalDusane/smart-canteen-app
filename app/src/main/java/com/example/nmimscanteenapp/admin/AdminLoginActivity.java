package com.example.nmimscanteenapp.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nmimscanteenapp.R;

public class AdminLoginActivity extends AppCompatActivity {

    private EditText adminNameInput, adminIdInput, adminPasswordInput;
    private Button loginBtn;
    private ImageButton backBtn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        sharedPreferences = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE);

        adminNameInput = findViewById(R.id.adminNameInput);
        adminIdInput = findViewById(R.id.adminIdInput);
        adminPasswordInput = findViewById(R.id.adminPasswordInput);
        loginBtn = findViewById(R.id.adminLoginSubmitBtn);
        backBtn = findViewById(R.id.backBtn);
        TextView forgotPasswordTv = findViewById(R.id.adminForgotPasswordTv);

        backBtn.setOnClickListener(v -> finish());

        loginBtn.setOnClickListener(v -> validateLogin());

        forgotPasswordTv.setOnClickListener(v -> {
            Intent intent = new Intent(AdminLoginActivity.this, ForgotAdminPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void validateLogin() {
        String name = adminNameInput.getText().toString().trim();
        String adminId = adminIdInput.getText().toString().trim();
        String password = adminPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(adminId) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get stored password (default 1234)
        String storedPassword = sharedPreferences.getString("admin_password", "1234");

        if (adminId.equalsIgnoreCase("admin123") && password.equals(storedPassword)) {
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

            // Save admin session
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("sapId", adminId);
            editor.putString("name", name);
            editor.putString("role", "admin");
            editor.apply();

            Intent intent = new Intent(AdminLoginActivity.this, AdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
