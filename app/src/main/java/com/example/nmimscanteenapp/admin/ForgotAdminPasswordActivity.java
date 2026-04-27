package com.example.nmimscanteenapp.admin;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nmimscanteenapp.R;

public class ForgotAdminPasswordActivity extends AppCompatActivity {

    private EditText adminIdVerifyInput, newAdminPasswordInput, confirmAdminPasswordInput;
    private Button resetAdminPasswordBtn;
    private ImageButton backBtn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_admin_password);

        sharedPreferences = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE);

        adminIdVerifyInput = findViewById(R.id.adminIdVerifyInput);
        newAdminPasswordInput = findViewById(R.id.newAdminPasswordInput);
        confirmAdminPasswordInput = findViewById(R.id.confirmAdminPasswordInput);
        resetAdminPasswordBtn = findViewById(R.id.resetAdminPasswordBtn);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        resetAdminPasswordBtn.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String adminId = adminIdVerifyInput.getText().toString().trim();
        String newPassword = newAdminPasswordInput.getText().toString().trim();
        String confirmPassword = confirmAdminPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(adminId) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!adminId.equals("admin123")) {
            Toast.makeText(this, "Invalid Admin ID", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 4) {
            Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update stored password
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("admin_password", newPassword);
        editor.apply();

        Toast.makeText(this, "Password Updated Successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
