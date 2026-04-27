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

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText sapIdInput;
    private Button verifyBtn;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        db = FirebaseFirestore.getInstance();

        sapIdInput = findViewById(R.id.sapIdForgotInput);
        verifyBtn = findViewById(R.id.verifyBtn);

        verifyBtn.setOnClickListener(v -> verifyUser());
    }

    private void verifyUser() {
        String sapId = sapIdInput.getText().toString().trim();

        if (TextUtils.isEmpty(sapId) || sapId.length() != 11 || !sapId.matches("\\d+")) {
            sapIdInput.setError("SAP ID must be exactly 11 digits");
            return;
        }

        db.collection("users").document(sapId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        // User exists
                        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("sapId", sapId);
                        startActivity(intent);
                    } else {
                        // User does not exist
                        Toast.makeText(ForgotPasswordActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
