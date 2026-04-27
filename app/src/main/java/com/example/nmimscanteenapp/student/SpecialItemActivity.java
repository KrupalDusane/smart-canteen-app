package com.example.nmimscanteenapp.student;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.model.FoodItem;
import com.example.nmimscanteenapp.utils.CartManager;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;

public class SpecialItemActivity extends AppCompatActivity {

    private FoodItem specialItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special_item);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        specialItem = new FoodItem("Paneer Special Combo", 120, R.drawable.paneercrispy, "Today's Special");

        CollapsingToolbarLayout collapsingToolbarLayout = findViewById(R.id.collapsingToolbarLayout);
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(specialItem.getName());
        }

        Button btnAddToCart = findViewById(R.id.btnAddToCartSpecial);
        Button btnOrderNow = findViewById(R.id.btnOrderNowSpecial);

        btnAddToCart.setOnClickListener(v -> {
            CartManager.getInstance().updateCart(specialItem, 1);
            Snackbar.make(findViewById(android.R.id.content), 
                specialItem.getName() + " added to cart", Snackbar.LENGTH_LONG)
                .setAction("View Cart", view -> {
                    android.content.Intent intent = new android.content.Intent(this, CartActivity.class);
                    intent.putExtra("sapId", getIntent().getStringExtra("sapId"));
                    startActivity(intent);
                }).show();
        });

        btnOrderNow.setOnClickListener(v -> {
            CartManager.getInstance().updateCart(specialItem, 1);
            
            String sapId = getIntent().getStringExtra("sapId");
            android.content.Intent intent = new android.content.Intent(this, CartActivity.class);
            intent.putExtra("sapId", sapId);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
