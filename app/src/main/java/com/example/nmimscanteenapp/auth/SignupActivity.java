package com.example.nmimscanteenapp.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nmimscanteenapp.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText signupSapIdInput, signupNameInput, signupPasswordInput;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        db = FirebaseFirestore.getInstance();

        signupSapIdInput = findViewById(R.id.signupSapIdInput);
        signupNameInput = findViewById(R.id.signupNameInput);
        signupPasswordInput = findViewById(R.id.signupPasswordInput);
        Button createAccountBtn = findViewById(R.id.createAccountBtn);
        TextView backToLoginTv = findViewById(R.id.backToLoginTv);

        createAccountBtn.setOnClickListener(v -> handleSignup());

        backToLoginTv.setOnClickListener(v -> finish());
    }

    private void handleSignup() {
        String name = signupNameInput.getText().toString().trim();
        String sapId = signupSapIdInput.getText().toString().trim();
        String password = signupPasswordInput != null ? signupPasswordInput.getText().toString().trim() : "default123";

        // VALIDATION
        if (TextUtils.isEmpty(name)) {
            signupNameInput.setError("Name is required");
            return;
        }

        if (signupPasswordInput != null && TextUtils.isEmpty(password)) {
            signupPasswordInput.setError("Password is required");
            return;
        }

        if (TextUtils.isEmpty(sapId) || sapId.length() != 11 || !sapId.matches("\\d+") || !sapId.startsWith("700")) {
            Toast.makeText(this, "Enter valid SAP ID", Toast.LENGTH_SHORT).show();
            signupSapIdInput.setError("Invalid SAP ID");
            return;
        }

        // FIRESTORE CHECK
        DocumentReference docRef = db.collection("users").document(sapId);

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // USER ALREADY EXISTS
                Toast.makeText(SignupActivity.this, "User already exists. Please login.", Toast.LENGTH_SHORT).show();
            } else {
                // CREATE NEW USER
                Map<String, Object> user = new HashMap<>();
                user.put("name", name);
                user.put("sapId", sapId);
                user.put("password", password);
                user.put("isFirstLogin", false);
                user.put("role", "student"); // Default role

                docRef.set(user).addOnSuccessListener(aVoid -> {
                    Toast.makeText(SignupActivity.this, "Account created successfully", Toast.LENGTH_LONG).show();
                    
                    // IF Admin (Manual Hack for Dev)
                    if (name.toLowerCase().contains("admin_secret")) {
                        docRef.update("role", "admin");
                    }

                    finish(); // Go back to login
                }).addOnFailureListener(e -> {
                    Toast.makeText(SignupActivity.this, "Error creating account", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(SignupActivity.this, "Error checking user", Toast.LENGTH_SHORT).show();
        });
    }
}
