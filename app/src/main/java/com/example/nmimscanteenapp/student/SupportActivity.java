package com.example.nmimscanteenapp.student;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.nmimscanteenapp.R;

public class SupportActivity extends AppCompatActivity {

    private String sapId, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support);

        sapId = getIntent().getStringExtra("sapId");
        name = getIntent().getStringExtra("name");

        Toolbar toolbar = findViewById(R.id.supportToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.cardComplaint).setOnClickListener(v -> {
            Intent intent = new Intent(this, ComplaintActivity.class);
            intent.putExtra("sapId", sapId);
            intent.putExtra("name", name);
            startActivity(intent);
        });

        findViewById(R.id.cardRules).setOnClickListener(v -> {
            startActivity(new Intent(this, RulesActivity.class));
        });

        findViewById(R.id.cardFAQ).setOnClickListener(v -> {
            startActivity(new Intent(this, FAQActivity.class));
        });

        findViewById(R.id.cardContact).setOnClickListener(v -> {
            startActivity(new Intent(this, ContactSupportActivity.class));
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
