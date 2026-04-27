package com.example.nmimscanteenapp.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.adapter.CartAdapter;
import com.example.nmimscanteenapp.model.CartItem;
import com.example.nmimscanteenapp.utils.CartManager;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private TextView totalAmountTv, itemsCountTv;
    private LinearLayout emptyCartLayout;
    private Button btnPlaceOrder;
    private ArrayList<CartItem> cartList;
    private int totalAmount = 0;
    private String sapId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Your Cart");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize UI
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalAmountTv = findViewById(R.id.totalAmountTv);
        itemsCountTv = findViewById(R.id.itemsCountTv);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);

        // Get SAP ID from Intent
        sapId = getIntent().getStringExtra("sapId");
        
        // Use CartManager for cartList
        cartList = CartManager.getInstance().getCartList();

        setupRecyclerView();
        updateCartSummary();

        btnPlaceOrder.setOnClickListener(v -> {
            if (!cartList.isEmpty()) {
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("sapId", sapId);
                intent.putExtra("total", totalAmount);
                intent.putExtra("cartList", cartList);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Cannot place order with empty cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartAdapter = new CartAdapter(cartList, this::updateCartSummary);
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void updateCartSummary() {
        totalAmount = 0;
        int totalItems = 0;

        if (cartList.isEmpty()) {
            emptyCartLayout.setVisibility(View.VISIBLE);
            cartRecyclerView.setVisibility(View.GONE);
            btnPlaceOrder.setEnabled(false);
            btnPlaceOrder.setAlpha(0.5f);
        } else {
            emptyCartLayout.setVisibility(View.GONE);
            cartRecyclerView.setVisibility(View.VISIBLE);
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setAlpha(1.0f);

            for (CartItem item : cartList) {
                totalAmount += (item.getPrice() * item.getQuantity());
                totalItems += item.getQuantity();
            }
        }

        totalAmountTv.setText("Total: ₹" + totalAmount);
        itemsCountTv.setText("Items (" + totalItems + ")");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
