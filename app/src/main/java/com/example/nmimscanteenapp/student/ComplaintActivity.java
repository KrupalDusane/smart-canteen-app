package com.example.nmimscanteenapp.student;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.nmimscanteenapp.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ComplaintActivity extends AppCompatActivity {

    private Spinner issueTypeSpinner;
    private EditText descriptionEt;
    private Button submitBtn;
    private FirebaseFirestore db;
    private String sapId, userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint);

        db = FirebaseFirestore.getInstance();
        sapId = getIntent().getStringExtra("sapId");
        userName = getIntent().getStringExtra("name");

        Toolbar toolbar = findViewById(R.id.complaintToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        issueTypeSpinner = findViewById(R.id.issueTypeSpinner);
        descriptionEt = findViewById(R.id.complaintDescriptionEt);
        submitBtn = findViewById(R.id.btnSubmitComplaint);

        String[] issues = {"Food Quality", "Delay", "Payment Issue", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, issues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        issueTypeSpinner.setAdapter(adapter);

        submitBtn.setOnClickListener(v -> submitComplaint());
    }

    private void submitComplaint() {
        String issueType = issueTypeSpinner.getSelectedItem().toString();
        String message = descriptionEt.getText().toString().trim();

        if (TextUtils.isEmpty(message)) {
            Toast.makeText(this, "Please describe your issue", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> complaint = new HashMap<>();
        complaint.put("userId", sapId != null ? sapId : "Unknown");
        complaint.put("userName", userName != null ? userName : "Unknown");
        complaint.put("issueType", issueType);
        complaint.put("message", message);
        complaint.put("status", "Pending");
        complaint.put("timestamp", System.currentTimeMillis());

        db.collection("complaints").add(complaint)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Complaint Submitted Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
