package com.example.nmimscanteenapp.admin;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.nmimscanteenapp.R;

public class AdminProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Profile");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TextView nameTv = findViewById(R.id.adminNameTv);
        TextView roleTv = findViewById(R.id.adminRoleTv);
        TextView emailTv = findViewById(R.id.adminEmailTv);

        nameTv.setText("NMIMS Admin");
        roleTv.setText("Canteen Administrator");
        emailTv.setText("admin@nmims.edu");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
