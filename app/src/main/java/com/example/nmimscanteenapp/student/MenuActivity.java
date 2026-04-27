package com.example.nmimscanteenapp.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.adapter.MenuAdapter;
import com.example.nmimscanteenapp.model.FoodItem;
import com.example.nmimscanteenapp.utils.CartManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class MenuActivity extends AppCompatActivity {

    private String sapId;
    private RecyclerView menuRecyclerView;
    private MenuAdapter adapter;
    private ArrayList<Object> masterList = new ArrayList<>();
    private ArrayList<Object> filteredList = new ArrayList<>();
    private FirebaseFirestore db;

    private CardView cartBottomBar;
    private TextView cartItemCountTv, cartTotalPriceTv;
    private EditText searchEditText;
    private BottomNavigationView bottomNavigationView;
    private LinearLayout categoryContainer;
    private String selectedCategory = "All";
    private String[] categories = {"All", "Breakfast", "Dosa", "Chinese", "Beverages", "Snacks", "Meals"};

    private CartManager cartManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        sapId = getIntent().getStringExtra("sapId");

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Canteen Menu");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        cartManager = CartManager.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI
        cartBottomBar = findViewById(R.id.cartBottomBar);
        cartItemCountTv = findViewById(R.id.cartItemCountTv);
        cartTotalPriceTv = findViewById(R.id.cartTotalPriceTv);
        searchEditText = findViewById(R.id.searchEditText);
        menuRecyclerView = findViewById(R.id.menuRecyclerView);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        categoryContainer = findViewById(R.id.categoryContainer);

        setupCategories();
        setupBottomNavigation();
        updateCartUI();

        // Setup RecyclerView
        menuRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        adapter = new MenuAdapter(filteredList, cartManager.getCartMap(), (foodItem, quantity) -> {
            cartManager.updateCart(foodItem, quantity);
            updateCartUI();
        });
        menuRecyclerView.setAdapter(adapter);

        // Load Static Menu
        initializeMenuData();
        filterMenu("");

        // Search Functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMenu(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        View btnGoToCart = findViewById(R.id.btnGoToCart);
        btnGoToCart.setOnClickListener(v -> {
            if (cartManager.getCartList().isEmpty()) {
                Toast.makeText(this, "Your cart is empty!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(MenuActivity.this, CartActivity.class);
                intent.putExtra("sapId", sapId);
                startActivity(intent);
            }
        });
    }

    private void updateCartUI() {
        int totalItemsCount = cartManager.getTotalItemsCount();
        int totalPrice = cartManager.getTotalPrice();

        if (totalItemsCount > 0) {
            cartBottomBar.setVisibility(View.VISIBLE);
            cartItemCountTv.setText(totalItemsCount + (totalItemsCount == 1 ? " Item" : " Items"));
            cartTotalPriceTv.setText("₹" + totalPrice);
        } else {
            cartBottomBar.setVisibility(View.GONE);
        }
    }

    private void setupCategories() {
        categoryContainer.removeAllViews();
        for (String category : categories) {
            View view = getLayoutInflater().inflate(R.layout.item_category_tab, categoryContainer, false);
            TextView tv = view.findViewById(R.id.categoryTv);
            tv.setText(category);

            if (selectedCategory.equals(category)) {
                tv.setBackgroundResource(R.drawable.bg_category_selected);
                tv.setTextColor(Color.WHITE);
            } else {
                tv.setBackgroundResource(R.drawable.bg_category_unselected);
                tv.setTextColor(Color.GRAY);
            }

            view.setOnClickListener(v -> {
                selectedCategory = category;
                setupCategories(); // Refresh UI
                filterMenu(searchEditText.getText().toString());
            });

            categoryContainer.addView(view);
        }
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_menu);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_menu) {
                return true;
            } else if (id == R.id.nav_orders) {
                Intent orderIntent = new Intent(MenuActivity.this, OrderHistoryActivity.class);
                orderIntent.putExtra("sapId", sapId);
                startActivity(orderIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (id == R.id.nav_profile) {
                Intent profileIntent = new Intent(MenuActivity.this, ProfileActivity.class);
                profileIntent.putExtra("sapId", sapId);
                startActivity(profileIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    private void initializeMenuData() {
        masterList.clear();

        // BREAKFAST
        masterList.add("Breakfast");
        masterList.add(new FoodItem("Poha", 30, R.drawable.poha, "Breakfast"));
        masterList.add(new FoodItem("Chai", 10, R.drawable.chai, "Breakfast"));

        // DOSA
        masterList.add("Dosa");
        masterList.add(new FoodItem("Plain Dosa", 50, R.drawable.dosa, "Dosa"));
        masterList.add(new FoodItem("Sada Dosa", 40, R.drawable.sadadosa, "Dosa"));
        masterList.add(new FoodItem("Mysore Dosa", 70, R.drawable.mysoredosa, "Dosa"));
        masterList.add(new FoodItem("Pav Bhaji Dosa", 90, R.drawable.pavbhajidosa, "Dosa"));

        // CHINESE
        masterList.add("Chinese");
        masterList.add(new FoodItem("Noodles", 80, R.drawable.noodles, "Chinese"));
        masterList.add(new FoodItem("Fried Rice", 90, R.drawable.friedrice, "Chinese"));
        masterList.add(new FoodItem("Manchurian", 90, R.drawable.manchurian, "Chinese"));
        masterList.add(new FoodItem("Manchurian Rice", 100, R.drawable.manchurianrice, "Chinese"));
        masterList.add(new FoodItem("Soya Bean Chilli", 110, R.drawable.soyabeanchilli, "Chinese"));

        // BEVERAGES
        masterList.add("Beverages");
        masterList.add(new FoodItem("Cold Coffee", 40, R.drawable.coldcoffee, "Beverages"));
        masterList.add(new FoodItem("Sugarcane Juice", 35, R.drawable.sugarcanejuice, "Beverages"));

        // SNACKS
        masterList.add("Snacks");
        masterList.add(new FoodItem("Sandwich", 60, R.drawable.sandwiches, "Snacks"));
        masterList.add(new FoodItem("Vada Pav", 20, R.drawable.vadapav, "Snacks", true));

        // MEALS
        masterList.add("Meals");
        masterList.add(new FoodItem("Thali", 120, R.drawable.thali, "Meals", true));
        masterList.add(new FoodItem("Dal Tadka", 100, R.drawable.daltadka, "Meals"));
        masterList.add(new FoodItem("Paneer Crispy", 120, R.drawable.paneercrispy, "Meals"));
    }

    private void filterMenu(String query) {
        filteredList.clear();
        for (Object obj : masterList) {
            if (obj instanceof FoodItem) {
                FoodItem item = (FoodItem) obj;
                boolean matchesSearch = query.isEmpty() || item.getName().toLowerCase().contains(query.toLowerCase());
                boolean matchesCategory = selectedCategory.equals("All") || item.getCategory().equalsIgnoreCase(selectedCategory);

                if (matchesSearch && matchesCategory) {
                    filteredList.add(item);
                }
            } else if (obj instanceof String && selectedCategory.equals("All") && query.isEmpty()) {
                // Only show headers in "All" view with no search
                filteredList.add(obj);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
