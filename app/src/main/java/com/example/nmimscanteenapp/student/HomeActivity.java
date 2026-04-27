package com.example.nmimscanteenapp.student;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.adapter.SliderAdapter;
import com.example.nmimscanteenapp.auth.ChangePasswordActivity;
import com.example.nmimscanteenapp.auth.MainActivity;
import com.example.nmimscanteenapp.model.FoodItem;
import com.example.nmimscanteenapp.model.Order;
import com.example.nmimscanteenapp.utils.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import android.os.Handler;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    private TextView welcomeTv, subtitleTv;
    private CardView orderCanteenCard, messMenuCard, orderReadyBanner;
    private CardView quickOrderBtn, quickMessBtn, quickHistoryBtn;
    private ViewPager2 viewPagerSlider;
    private LinearLayout sliderDots;
    private Handler sliderHandler = new Handler();
    private TextView readyTokenTv, mostOrderedItemTv;
    private ImageView closeBannerBtn;
    private String sapId;
    private BottomNavigationView bottomNavigationView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Create Notification Channel
        NotificationHelper.createNotificationChannel(this);
        checkNotificationPermission();

        // Get SAP ID and Name from Intent
        sapId = getIntent().getStringExtra("sapId");
        String name = getIntent().getStringExtra("name");

        // Initialize UI
        welcomeTv = findViewById(R.id.welcomeTv);
        orderCanteenCard = findViewById(R.id.orderCanteenCard);
        messMenuCard = findViewById(R.id.messMenuCard);
        viewPagerSlider = findViewById(R.id.viewPagerSlider);
        sliderDots = findViewById(R.id.sliderDots);
        orderReadyBanner = findViewById(R.id.orderReadyBanner);
        readyTokenTv = findViewById(R.id.readyTokenTv);
        closeBannerBtn = findViewById(R.id.closeBannerBtn);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        mostOrderedItemTv = findViewById(R.id.mostOrderedItemTv);
        db = FirebaseFirestore.getInstance();
        
        quickOrderBtn = findViewById(R.id.quickOrderBtn);
        quickMessBtn = findViewById(R.id.quickMessBtn);
        quickHistoryBtn = findViewById(R.id.quickHistoryBtn);

        setupSlider();
        setupBottomNavigation();
        setupQuickActions();
        
        findViewById(R.id.profileIcon).setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            intent.putExtra("sapId", sapId);
            intent.putExtra("name", getIntent().getStringExtra("name"));
            startActivity(intent);
        });

        closeBannerBtn.setOnClickListener(v -> {
            orderReadyBanner.animate().alpha(0f).setDuration(300).withEndAction(() -> 
                orderReadyBanner.setVisibility(View.GONE));
        });
        
        orderReadyBanner.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });

        // Set welcome text
        String greeting = getGreeting();
        String displayName = name != null ? name : (sapId != null ? sapId : "User");
        welcomeTv.setText(greeting + ", " + displayName + " 👋");

        fetchMostOrdered();

        // Click listeners for navigation
        orderCanteenCard.setOnClickListener(v -> {
            if (isCanteenOpen()) {
                Intent intent = new Intent(HomeActivity.this, MenuActivity.class);
                intent.putExtra("sapId", sapId);
                intent.putExtra("name", name);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Canteen is closed. Timing: 9 AM - 7 PM", Toast.LENGTH_LONG).show();
            }
        });

        messMenuCard.setOnClickListener(v -> {
            if (isMessOpen()) {
                Intent intent = new Intent(HomeActivity.this, MessMenuActivity.class);
                intent.putExtra("sapId", sapId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Mess is closed. Timing: 8 AM - 10 PM", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != 
                android.content.pm.PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, 
                    new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 
                    NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Notifications permission denied. You won't receive order updates.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void setupSlider() {
        List<FoodItem> sliderItems = new ArrayList<>();
        sliderItems.add(new FoodItem("Paneer Crispy", 120, R.drawable.paneercrispy, "Special"));
        sliderItems.add(new FoodItem("Thali", 120, R.drawable.thali, "Special"));
        sliderItems.add(new FoodItem("Mysore Dosa", 70, R.drawable.mysoredosa, "Special"));
        sliderItems.add(new FoodItem("Cold Coffee", 40, R.drawable.coldcoffee, "Special"));

        viewPagerSlider.setAdapter(new SliderAdapter(sliderItems));
        setupSliderDots(sliderItems.size());
        
        viewPagerSlider.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDots(position);
                sliderHandler.removeCallbacks(sliderRunnable);
                sliderHandler.postDelayed(sliderRunnable, 3000);
            }
        });
    }

    private void setupSliderDots(int size) {
        ImageView[] dots = new ImageView[size];
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8, 0, 8, 0);

        for (int i = 0; i < size; i++) {
            dots[i] = new ImageView(this);
            dots[i].setImageResource(R.drawable.bg_status_grey); // Unselected dot
            dots[i].setLayoutParams(params);
            sliderDots.addView(dots[i]);
        }
    }

    private void updateDots(int position) {
        for (int i = 0; i < sliderDots.getChildCount(); i++) {
            ImageView dot = (ImageView) sliderDots.getChildAt(i);
            dot.setImageResource(i == position ? R.drawable.bg_status_red : R.drawable.bg_status_grey);
        }
    }

    private void setupQuickActions() {
        quickOrderBtn.setOnClickListener(v -> orderCanteenCard.performClick());
        quickMessBtn.setOnClickListener(v -> messMenuCard.performClick());
        quickHistoryBtn.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, OrderHistoryActivity.class);
            intent.putExtra("sapId", sapId);
            startActivity(intent);
        });
    }

    private Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            int nextItem = viewPagerSlider.getCurrentItem() + 1;
            if (nextItem >= 4) nextItem = 0;
            viewPagerSlider.setCurrentItem(nextItem);
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }
    private String getGreeting() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        if (hour >= 5 && hour < 12) return "Good Morning";
        if (hour >= 12 && hour < 17) return "Good Afternoon";
        if (hour >= 17 && hour < 21) return "Good Evening";
        return "Good Night";
    }

    private boolean isCanteenOpen() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        return hour >= 9 && hour < 19;
    }

    private boolean isMessOpen() {
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
        return hour >= 8 && hour < 22;
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.nav_menu) {
                Intent menuIntent = new Intent(HomeActivity.this, MenuActivity.class);
                menuIntent.putExtra("sapId", sapId);
                startActivity(menuIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (id == R.id.nav_orders) {
                Intent orderIntent = new Intent(HomeActivity.this, OrderHistoryActivity.class);
                orderIntent.putExtra("sapId", sapId);
                startActivity(orderIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (id == R.id.nav_profile) {
                Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                profileIntent.putExtra("sapId", sapId);
                profileIntent.putExtra("name", getIntent().getStringExtra("name"));
                startActivity(profileIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkReadyOrders();
    }

    private void checkReadyOrders() {
        android.content.SharedPreferences prefs = getSharedPreferences("NMIMS_Canteen", android.content.Context.MODE_PRIVATE);
        String json = prefs.getString("order_history", "");
        if (!json.isEmpty()) {
            com.google.gson.Gson gson = new com.google.gson.Gson();
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<java.util.ArrayList<Order>>() {}.getType();
            java.util.ArrayList<Order> orders = gson.fromJson(json, type);
            
            for (Order order : orders) {
                if ("Ready".equalsIgnoreCase(order.getStatus())) {
                    readyTokenTv.setText("Token #" + order.getToken() + " is ready for collection");
                    orderReadyBanner.setVisibility(View.VISIBLE);
                    return; // Show only one for now
                }
            }
        }
        orderReadyBanner.setVisibility(View.GONE);
    }

    private void fetchMostOrdered() {
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        if (mostOrderedItemTv != null) mostOrderedItemTv.setText("None yet");
                        return;
                    }

                    java.util.Map<String, Integer> counts = new java.util.HashMap<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String items = doc.getString("items");
                        if (items == null) continue;

                        String[] parts = items.split(",");
                        for (String part : parts) {
                            part = part.trim();
                            if (part.isEmpty()) continue;

                            try {
                                int startBracket = part.lastIndexOf("(x");
                                if (startBracket != -1) {
                                    String name = part.substring(0, startBracket).trim();
                                    String qtyStr = part.substring(startBracket + 2, part.length() - 1);
                                    int qty = Integer.parseInt(qtyStr);
                                    counts.put(name, counts.getOrDefault(name, 0) + qty);
                                } else {
                                    counts.put(part, counts.getOrDefault(part, 0) + 1);
                                }
                            } catch (Exception e) {
                                counts.put(part, counts.getOrDefault(part, 0) + 1);
                            }
                        }
                    }

                    String bestItem = "None";
                    int maxCount = -1;
                    for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
                        if (entry.getValue() > maxCount) {
                            maxCount = entry.getValue();
                            bestItem = entry.getKey();
                        }
                    }

                    if (mostOrderedItemTv != null) {
                        mostOrderedItemTv.setText(maxCount > 0 ? bestItem : "None yet");
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            showProfileMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProfileMenu() {
        View view = findViewById(R.id.action_profile);
        if (view == null) return;
        
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenu().add("My Profile");
        popup.getMenu().add("My Orders");
        popup.getMenu().add("Change Password");
        popup.getMenu().add("Logout");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            switch (title) {
                case "My Profile":
                    Intent profileIntent = new Intent(HomeActivity.this, ProfileActivity.class);
                    profileIntent.putExtra("sapId", sapId);
                    profileIntent.putExtra("name", getIntent().getStringExtra("name"));
                    startActivity(profileIntent);
                    break;
                case "My Orders":
                    Intent historyIntent = new Intent(HomeActivity.this, OrderHistoryActivity.class);
                    historyIntent.putExtra("sapId", sapId);
                    startActivity(historyIntent);
                    break;
                case "Change Password":
                    Intent cpIntent = new Intent(HomeActivity.this, ChangePasswordActivity.class);
                    cpIntent.putExtra("sapId", sapId);
                    startActivity(cpIntent);
                    break;
                case "Logout":
                    // Clear SharedPreferences for Logout
                    android.content.SharedPreferences prefs = getSharedPreferences("NMIMS_Canteen", android.content.Context.MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = prefs.edit();
                    editor.clear();
                    editor.apply();

                    Intent loginIntent = new Intent(HomeActivity.this, MainActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                    finish();
                    break;
            }
            return true;
        });
        popup.show();
    }
}
