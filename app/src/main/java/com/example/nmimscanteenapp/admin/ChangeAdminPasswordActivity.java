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

public class ChangeAdminPasswordActivity extends AppCompatActivity {

    private EditText oldAdminPasswordInput, newAdminPasswordInput, confirmAdminPasswordInput;
    private Button updateAdminPasswordBtn;
    private ImageButton backBtn;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_admin_password);

        sharedPreferences = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE);

        oldAdminPasswordInput = findViewById(R.id.oldAdminPasswordInput);
        newAdminPasswordInput = findViewById(R.id.newAdminPasswordInput);
        confirmAdminPasswordInput = findViewById(R.id.confirmAdminPasswordInput);
        updateAdminPasswordBtn = findViewById(R.id.updateAdminPasswordBtn);
        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        updateAdminPasswordBtn.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String oldPassword = oldAdminPasswordInput.getText().toString().trim();
        String newPassword = newAdminPasswordInput.getText().toString().trim();
        String confirmPassword = confirmAdminPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String storedPassword = sharedPreferences.getString("admin_password", "1234");

        if (!oldPassword.equals(storedPassword)) {
            Toast.makeText(this, "Incorrect old password", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(this, "Password Changed Successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
