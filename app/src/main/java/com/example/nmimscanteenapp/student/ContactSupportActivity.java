package com.example.nmimscanteenapp.student;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.nmimscanteenapp.R;

public class ContactSupportActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_support);

        Toolbar toolbar = findViewById(R.id.contactToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        Button btnEmail = findViewById(R.id.btnEmailSupport);
        btnEmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:krupaldusane12@gmail.com"));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - NMIMS Smart Canteen");
            startActivity(Intent.createChooser(intent, "Send Email"));
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
