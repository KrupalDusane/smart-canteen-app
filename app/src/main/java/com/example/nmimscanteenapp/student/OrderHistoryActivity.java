package com.example.nmimscanteenapp.student;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.model.Order;
import com.example.nmimscanteenapp.utils.NotificationHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.gson.Gson;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView historyRecyclerView;
    private OrderHistoryAdapter adapter;
    private List<Order> fullOrderList = new ArrayList<>();
    private List<Order> filteredOrderList = new ArrayList<>();
    private ImageView backBtn;
    private TabLayout tabLayout;
    private LinearLayout emptyStateLayout;
    private BottomNavigationView bottomNavigationView;
    private String sapId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        db = FirebaseFirestore.getInstance();
        sapId = getIntent().getStringExtra("sapId");
        if (sapId == null) {
            sapId = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE).getString("sapId", "");
        }

        initViews();
        setupTabs();
        loadOrderHistory();
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // loadOrderHistory() is already called in onCreate and uses a listener
    }

    private void initViews() {
        backBtn = findViewById(R.id.backBtnHistory);
        backBtn.setOnClickListener(v -> finish());

        tabLayout = findViewById(R.id.orderTabLayout);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bottomNavigationView = findViewById(R.id.bottomNavigation);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_orders);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                finish();
                return true;
            } else if (id == R.id.nav_menu) {
                Intent menuIntent = new Intent(OrderHistoryActivity.this, MenuActivity.class);
                menuIntent.putExtra("sapId", sapId);
                startActivity(menuIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            } else if (id == R.id.nav_orders) {
                return true;
            } else if (id == R.id.nav_profile) {
                Intent profileIntent = new Intent(OrderHistoryActivity.this, ProfileActivity.class);
                profileIntent.putExtra("sapId", sapId);
                startActivity(profileIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                finish();
                return true;
            }
            return false;
        });
    }

    private void setupTabs() {
        tabLayout.removeAllTabs();
        tabLayout.addTab(tabLayout.newTab().setText("Current"));
        tabLayout.addTab(tabLayout.newTab().setText("History"));
        tabLayout.addTab(tabLayout.newTab().setText("All"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterOrders(tab.getText().toString());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterOrders(String tab) {
        filteredOrderList.clear();
        if (tab.equalsIgnoreCase("Current")) {
            filteredOrderList.addAll(fullOrderList.stream()
                    .filter(o -> "Pending".equalsIgnoreCase(o.getStatus()) || 
                                "Accepted".equalsIgnoreCase(o.getStatus()) || 
                                "Preparing".equalsIgnoreCase(o.getStatus()) ||
                                "Cooking".equalsIgnoreCase(o.getStatus()))
                    .collect(Collectors.toList()));
        } else if (tab.equalsIgnoreCase("History")) {
            filteredOrderList.addAll(fullOrderList.stream()
                    .filter(o -> "Ready".equalsIgnoreCase(o.getStatus()) || 
                                "Completed".equalsIgnoreCase(o.getStatus()) || 
                                "Cancelled".equalsIgnoreCase(o.getStatus()) || 
                                "Rejected".equalsIgnoreCase(o.getStatus()))
                    .collect(Collectors.toList()));
        } else {
            filteredOrderList.addAll(fullOrderList);
        }
        
        if (adapter != null) {
            adapter.updateList(filteredOrderList);
        }
        updateEmptyState();
    }

    private void updateEmptyState() {
        if (filteredOrderList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            historyRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            historyRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private java.util.Map<String, String> lastKnownStatuses = new java.util.HashMap<>();

    private void loadOrderHistory() {
        if (sapId == null || sapId.isEmpty()) return;

        // Initialize adapter first to ensure it's ready for the listener
        adapter = new OrderHistoryAdapter(new ArrayList<>(), this::handleOrderAction);
        historyRecyclerView.setAdapter(adapter);

        db.collection("orders")
                .whereEqualTo("userId", sapId)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("FIRESTORE_ERROR", "Error: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        fullOrderList.clear();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                            Order order = doc.toObject(Order.class);
                            if (order != null) {
                                String orderId = doc.getId();
                                order.setEstimatedReadyTime(orderId);
                                fullOrderList.add(order);

                                String currentStatus = order.getStatus();
                                String lastStatus = lastKnownStatuses.get(orderId);

                                if (lastStatus != null && !lastStatus.equalsIgnoreCase(currentStatus)) {
                                    if ("Accepted".equalsIgnoreCase(currentStatus)) {
                                        NotificationHelper.showOrderAcceptedNotification(this, order.getToken());
                                    } else if ("Preparing".equalsIgnoreCase(currentStatus)) {
                                        NotificationHelper.showOrderPreparingNotification(this, order.getToken());
                                    } else if ("Ready".equalsIgnoreCase(currentStatus)) {
                                        NotificationHelper.showOrderReadyNotification(this, order.getToken(), order.getItems());
                                    } else if ("Rejected".equalsIgnoreCase(currentStatus)) {
                                        NotificationHelper.showOrderRejectedNotification(this, order.getToken(), order.getRejectReason());
                                    }
                                }
                                lastKnownStatuses.put(orderId, currentStatus);
                            }
                        }
                        
                        // Sort locally since we removed orderBy to avoid index requirement
                        fullOrderList.sort((o1, o2) -> Long.compare(o2.getTimestamp(), o1.getTimestamp()));
                        
                        int selectedTab = tabLayout.getSelectedTabPosition();
                        filterOrders(tabLayout.getTabAt(selectedTab).getText().toString());
                    }
                });
    }

    private void handleOrderAction(Order order, String action) {
        String docId = order.getEstimatedReadyTime();
        if ("CANCEL".equals(action)) {
            // Prevent cancellation if preparing or ready
            if ("Preparing".equalsIgnoreCase(order.getStatus()) || "Ready".equalsIgnoreCase(order.getStatus())) {
                showSnackbar("Cannot cancel order while it's " + order.getStatus());
                return;
            }

            long diff = System.currentTimeMillis() - order.getTimestamp();
            if (diff > 120000) {
                showSnackbar("Cannot cancel after 2 minutes of ordering");
                return;
            }

            db.collection("orders").document(docId).update("status", "Cancelled")
                    .addOnSuccessListener(aVoid -> showSnackbar("Order cancelled. Refund will be processed in 2-3 working days."))
                    .addOnFailureListener(e -> showSnackbar("Error: " + e.getMessage()));
        } else if ("COLLECTED".equals(action)) {
            db.collection("orders").document(docId).update("status", "Completed")
                    .addOnSuccessListener(aVoid -> showSnackbar("Order collected"));
        }
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }

    private static class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.ViewHolder> {
        private List<Order> orders;
        private final OnOrderActionListener actionListener;
        private final List<CountDownTimer> timers = new ArrayList<>();

        interface OnOrderActionListener {
            void onAction(Order order, String action);
        }

        OrderHistoryAdapter(List<Order> orders, OnOrderActionListener actionListener) {
            this.orders = orders;
            this.actionListener = actionListener;
        }

        void updateList(List<Order> newList) {
            cancelAllTimers();
            this.orders = newList;
            notifyDataSetChanged();
        }

        private void cancelAllTimers() {
            for (CountDownTimer timer : timers) {
                timer.cancel();
            }
            timers.clear();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_history, parent, false);
            return new ViewHolder(view);
        }

        private void updateStatusUI(ViewHolder holder, Order order) {
            String status = order.getStatus();
            holder.status.setText(status);
            holder.status.setVisibility(View.VISIBLE);

            long timePassed = System.currentTimeMillis() - order.getTimestamp();
            boolean isWithinTwoMinutes = timePassed < 120000;

            if ("Pending".equalsIgnoreCase(status)) {
                holder.status.setBackgroundResource(R.drawable.bg_category_unselected);
                holder.status.setTextColor(0xFF757575); 
                holder.cancelBtn.setVisibility(isWithinTwoMinutes ? View.VISIBLE : View.GONE);
                holder.collectBtn.setVisibility(View.GONE);
                holder.estimatedTime.setVisibility(View.VISIBLE);
                holder.estimatedTime.setText("Waiting for Canteen to accept...");
                holder.estimatedTime.setTextColor(0xFF757575);
            } else if ("Accepted".equalsIgnoreCase(status)) {
                holder.status.setBackgroundResource(R.drawable.bg_category_selected);
                holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE3F2FD));
                holder.status.setTextColor(0xFF2196F3); 
                holder.cancelBtn.setVisibility(isWithinTwoMinutes ? View.VISIBLE : View.GONE);
                holder.collectBtn.setVisibility(View.GONE);
                holder.estimatedTime.setVisibility(View.VISIBLE);
                holder.estimatedTime.setText("Accepted by Canteen");
                holder.estimatedTime.setTextColor(0xFF2196F3);
            } else if ("Preparing".equalsIgnoreCase(status) || "Cooking".equalsIgnoreCase(status)) {
                holder.status.setBackgroundResource(R.drawable.bg_category_selected);
                holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFF3E0));
                holder.status.setTextColor(0xFFFF9800); 
                holder.cancelBtn.setVisibility(View.GONE);
                holder.collectBtn.setVisibility(View.GONE);
                holder.estimatedTime.setVisibility(View.VISIBLE);
                holder.estimatedTime.setText("Canteen is preparing your meal");
                holder.estimatedTime.setTextColor(0xFFFF9800);
            } else if ("Ready".equalsIgnoreCase(status)) {
                holder.status.setBackgroundResource(R.drawable.bg_category_selected);
                holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE8F5E9));
                holder.status.setTextColor(0xFF4CAF50); 
                holder.cancelBtn.setVisibility(View.GONE);
                holder.collectBtn.setVisibility(View.VISIBLE);
                holder.estimatedTime.setVisibility(View.VISIBLE);
                holder.estimatedTime.setText("Ready for collection!");
                holder.estimatedTime.setTextColor(0xFF4CAF50);
            } else if ("Completed".equalsIgnoreCase(status)) {
                holder.status.setBackgroundResource(R.drawable.bg_category_selected);
                holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFE8F5E9));
                holder.status.setTextColor(0xFF1B5E20); // Dark Green
                holder.cancelBtn.setVisibility(View.GONE);
                holder.collectBtn.setVisibility(View.GONE);
                holder.estimatedTime.setVisibility(View.GONE);
            } else if ("Cancelled".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
                holder.status.setBackgroundResource(R.drawable.bg_category_selected);
                holder.status.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFFFFEBEE));
                holder.status.setTextColor(0xFFD32F2F);
                holder.cancelBtn.setVisibility(View.GONE);
                holder.collectBtn.setVisibility(View.GONE);
                holder.viewDetailsBtn.setVisibility(View.GONE);
                holder.estimatedTime.setVisibility(View.VISIBLE);
                
                if ("Rejected".equalsIgnoreCase(status)) {
                    String reason = order.getRejectReason();
                    holder.estimatedTime.setText("Rejected: " + (reason != null ? reason : "Canteen busy"));
                } else {
                    holder.estimatedTime.setText("Order Cancelled");
                }
            } else {
                holder.viewDetailsBtn.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Order order = orders.get(position);
            
            holder.token.setText("Order #" + order.getToken());
            holder.items.setText(order.getItems());
            holder.total.setText("₹" + order.getTotalAmount());
            holder.scheduledTime.setText("Pickup: " + order.getScheduledTime());

            updateStatusUI(holder, order);

            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            String timeStr = sdf.format(new Date(order.getTimestamp()));
            holder.orderedTime.setText("Ordered: " + timeStr);

            holder.cancelBtn.setOnClickListener(v -> actionListener.onAction(order, "CANCEL"));
            holder.collectBtn.setOnClickListener(v -> actionListener.onAction(order, "COLLECTED"));
            holder.viewDetailsBtn.setOnClickListener(v -> actionListener.onAction(order, "DETAILS"));
        }

        @Override
        public int getItemCount() {
            return orders.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView token, status, items, total, orderedTime, scheduledTime, estimatedTime;
            MaterialButton viewDetailsBtn, cancelBtn, collectBtn;

            ViewHolder(View itemView) {
                super(itemView);
                token = itemView.findViewById(R.id.historyTokenTv);
                status = itemView.findViewById(R.id.historyStatusTv);
                items = itemView.findViewById(R.id.historyItemsTv);
                total = itemView.findViewById(R.id.historyTotalTv);
                orderedTime = itemView.findViewById(R.id.historyOrderedTimeTv);
                scheduledTime = itemView.findViewById(R.id.historyScheduledTimeTv);
                estimatedTime = itemView.findViewById(R.id.historyEstimatedTimeTv);
                viewDetailsBtn = itemView.findViewById(R.id.viewDetailsBtn);
                cancelBtn = itemView.findViewById(R.id.cancelOrderBtn);
                collectBtn = itemView.findViewById(R.id.collectOrderBtn);
            }
        }
    }
}
