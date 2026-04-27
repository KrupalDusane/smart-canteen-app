package com.example.nmimscanteenapp.student;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.auth.ChangePasswordActivity;
import com.example.nmimscanteenapp.auth.MainActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ProfileActivity extends AppCompatActivity {

    private String sapId, name;
    private TextView profileSapTv, profileNameTv;
    private CardView btnProfileOrders, btnProfilePassword, btnProfileLogout, btnProfileSupport;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sapId = getIntent().getStringExtra("sapId");
        name = getIntent().getStringExtra("name");

        profileSapTv = findViewById(R.id.profileSapTv);
        profileNameTv = findViewById(R.id.profileNameTv);
        btnProfileOrders = findViewById(R.id.btnProfileOrders);
        btnProfilePassword = findViewById(R.id.btnProfilePassword);
        btnProfileLogout = findViewById(R.id.btnProfileLogout);
        btnProfileSupport = findViewById(R.id.btnProfileSupport);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        setupBottomNavigation();

        if (sapId != null) {
            profileSapTv.setText("SAP ID: " + sapId);
        }
        if (name != null) {
            profileNameTv.setText(name);
        }

        btnProfileOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
            intent.putExtra("sapId", sapId);
            startActivity(intent);
        });

        btnProfilePassword.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
            intent.putExtra("sapId", sapId);
            startActivity(intent);
        });

        btnProfileSupport.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SupportActivity.class);
            intent.putExtra("sapId", sapId);
            intent.putExtra("name", name);
            startActivity(intent);
        });

        btnProfileLogout.setOnClickListener(v -> {
            // Clear SharedPreferences for Logout
            SharedPreferences sharedPreferences = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_menu) {
                Intent menuIntent = new Intent(ProfileActivity.this, MenuActivity.class);
                menuIntent.putExtra("sapId", sapId);
                startActivity(menuIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                Intent orderIntent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
                orderIntent.putExtra("sapId", sapId);
                startActivity(orderIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                return true;
            }
            return false;
        });
    }
}
