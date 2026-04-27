package com.example.nmimscanteenapp.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nmimscanteenapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordInput, confirmPasswordInput;
    private Button resetBtn;
    private FirebaseFirestore db;
    private String sapId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        db = FirebaseFirestore.getInstance();
        sapId = getIntent().getStringExtra("sapId");

        newPasswordInput = findViewById(R.id.newPasswordResetInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordResetInput);
        resetBtn = findViewById(R.id.resetPasswordBtn);

        resetBtn.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String newPass = newPasswordInput.getText().toString().trim();
        String confirmPass = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(newPass) || TextUtils.isEmpty(confirmPass)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPass.length() < 6) {
            newPasswordInput.setError("Password must be at least 6 characters");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            confirmPasswordInput.setError("Passwords do not match");
            return;
        }

        // Update Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("password", newPass);
        updates.put("isFirstLogin", false);

        db.collection("users").document(sapId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ResetPasswordActivity.this, "Password Reset Successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(ResetPasswordActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ResetPasswordActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
