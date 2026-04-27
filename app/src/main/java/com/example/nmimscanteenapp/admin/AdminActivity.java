package com.example.nmimscanteenapp.admin;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.auth.MainActivity;
import com.example.nmimscanteenapp.model.Order;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView adminOrdersRv;
    private AdminOrderAdapter adapter;
    private List<DocumentSnapshot> orderDocs = new ArrayList<>();
    private FirebaseFirestore db;
    private TextView totalOrdersTv, pendingOrdersTv, readyOrdersTv, revenueText, mostOrderedItemTv, topRevenueItemTv;
    private LinearLayout emptyStateLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = findViewById(R.id.adminToolbar);
        setSupportActionBar(toolbar);

        db = FirebaseFirestore.getInstance();
        adminOrdersRv = findViewById(R.id.adminOrdersRv);
        adminOrdersRv.setLayoutManager(new LinearLayoutManager(this));

        totalOrdersTv = findViewById(R.id.totalOrdersTv);
        pendingOrdersTv = findViewById(R.id.pendingOrdersTv);
        readyOrdersTv = findViewById(R.id.readyOrdersTv);
        revenueText = findViewById(R.id.revenueText);
        mostOrderedItemTv = findViewById(R.id.mostOrderedItemTv);
        topRevenueItemTv = findViewById(R.id.topRevenueItemTv);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        findViewById(R.id.viewAnalyticsBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, AdminAnalyticsActivity.class));
        });

        findViewById(R.id.adminProfileCard).setOnClickListener(this::showAdminPopupMenu);

        adapter = new AdminOrderAdapter(orderDocs);
        adminOrdersRv.setAdapter(adapter);

        // Make cards clickable
        findViewById(R.id.totalOrdersCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderListActivity.class);
            intent.putExtra("type", "ALL");
            startActivity(intent);
        });

        findViewById(R.id.pendingOrdersCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderListActivity.class);
            intent.putExtra("type", "PENDING");
            startActivity(intent);
        });

        findViewById(R.id.revenueCard).setOnClickListener(v -> {
            Intent intent = new Intent(this, OrderListActivity.class);
            intent.putExtra("type", "COMPLETED");
            startActivity(intent);
        });

        listenToOrders();
    }

    private void showAdminPopupMenu(View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.admin_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_profile) {
                startActivity(new Intent(this, AdminProfileActivity.class));
                return true;
            } else if (id == R.id.action_all_orders) {
                // Refresh list or show all
                listenToOrders();
                Toast.makeText(this, "Refreshing orders...", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.action_manage_menu) {
                startActivity(new Intent(this, ManageMenuActivity.class));
                return true;
            } else if (id == R.id.action_change_password) {
                startActivity(new Intent(this, ChangeAdminPasswordActivity.class));
                return true;
            } else if (id == R.id.action_logout) {
                handleLogout();
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void listenToOrders() {
        db.collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e("ADMIN_DEBUG", "Firestore Error: " + error.getMessage());
                        return;
                    }
                    if (value != null) {
                        List<DocumentSnapshot> allDocs = value.getDocuments();
                        orderDocs.clear();

                        long todayStart = getStartOfDay();
                        long todayEnd = getEndOfDay();

                        // Filter for Active Orders: Show only Pending, Preparing, and Ready in the main dashboard list
                        for (DocumentSnapshot doc : allDocs) {
                            Order order = doc.toObject(Order.class);
                            if (order == null || order.getCreatedAt() == null) continue;

                            String status = order.getStatus();
                            if (status == null) status = "Pending";

                            long createdAt = order.getCreatedAt();
                            boolean isToday = createdAt >= todayStart && createdAt <= todayEnd;

                            if (isToday && ("Pending".equalsIgnoreCase(status) || "Preparing".equalsIgnoreCase(status) || "Ready".equalsIgnoreCase(status))) {
                                orderDocs.add(doc);
                            }
                        }
                        
                        updateStats(allDocs);
                        calculateMostOrdered(allDocs);
                        adapter.notifyDataSetChanged();
                        
                        if (orderDocs.isEmpty()) {
                            emptyStateLayout.setVisibility(View.VISIBLE);
                            adminOrdersRv.setVisibility(View.GONE);
                        } else {
                            emptyStateLayout.setVisibility(View.GONE);
                            adminOrdersRv.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private long getStartOfDay() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getEndOfDay() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
        cal.set(java.util.Calendar.MINUTE, 59);
        cal.set(java.util.Calendar.SECOND, 59);
        cal.set(java.util.Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    private void updateStats(List<DocumentSnapshot> docs) {
        int todayOrders = 0;
        int pending = 0;
        int todayRevenue = 0;

        long todayStart = getStartOfDay();
        long todayEnd = getEndOfDay();

        for (DocumentSnapshot doc : docs) {
            Order order = doc.toObject(Order.class);
            if (order == null || order.getCreatedAt() == null) continue;

            String status = order.getStatus();
            if (status == null) status = "Pending";

            long createdAt = order.getCreatedAt();
            boolean isToday = createdAt >= todayStart && createdAt <= todayEnd;

            if (isToday) {
                todayOrders++;
                if ("Pending".equalsIgnoreCase(status)) {
                    pending++;
                }

                // Calculate Today's Revenue for valid (non-rejected/cancelled) orders
                if (!"Rejected".equalsIgnoreCase(status) && !"Cancelled".equalsIgnoreCase(status)) {
                    todayRevenue += order.getTotalAmount();
                }
            }
        }

        totalOrdersTv.setText(String.valueOf(todayOrders));
        pendingOrdersTv.setText(String.valueOf(pending));
        revenueText.setText("₹" + todayRevenue);
    }

    private void calculateMostOrdered(List<DocumentSnapshot> docs) {
        if (docs.isEmpty()) {
            if (mostOrderedItemTv != null) mostOrderedItemTv.setText("N/A");
            if (topRevenueItemTv != null) topRevenueItemTv.setText("N/A");
            return;
        }

        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        java.util.Map<String, Integer> revenueMap = new java.util.HashMap<>();

        // We need prices to calculate revenue accurately if they are not in the order doc
        // For simplicity, we can try to estimate if amount per item is not stored.
        // But looking at OrderListActivity, totalAmount is for the whole order.
        
        for (DocumentSnapshot doc : docs) {
            String items = doc.getString("items");
            if (items == null) continue;

            String status = doc.getString("status");
            boolean isValid = !"Rejected".equalsIgnoreCase(status) && !"Cancelled".equalsIgnoreCase(status);

            // Simple parsing: "Item A (x2), Item B (x1)"
            String[] parts = items.split(",");
            
            // To calculate revenue per item from totalAmount, we'd need item prices.
            // Since we don't have a direct item -> price mapping here without another fetch,
            // and the user wants "which project is selling hot and generating more revenue",
            // I will implement a more robust parser that also tracks which items are in high-value orders.
            
            Object totalObj = doc.get("totalAmount");
            int totalAmount = 0;
            if (totalObj instanceof Long) totalAmount = ((Long) totalObj).intValue();
            else if (totalObj instanceof Integer) totalAmount = (Integer) totalObj;

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
                        
                        // If valid order, attribute revenue proportionally (best effort)
                        if (isValid) {
                            // Since we don't know individual prices, we count the number of items and divide
                            // This is an approximation. A better way would be to store item prices in the order.
                            revenueMap.put(name, revenueMap.getOrDefault(name, 0) + (totalAmount / parts.length));
                        }
                    } else {
                        counts.put(part, counts.getOrDefault(part, 0) + 1);
                        if (isValid) {
                            revenueMap.put(part, revenueMap.getOrDefault(part, 0) + (totalAmount / parts.length));
                        }
                    }
                } catch (Exception e) {
                    counts.put(part, counts.getOrDefault(part, 0) + 1);
                }
            }
        }

        String bestItem = "N/A";
        int maxCount = -1;
        for (java.util.Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                bestItem = entry.getKey();
            }
        }

        String topRevenueItem = "N/A";
        int maxRevenue = -1;
        for (java.util.Map.Entry<String, Integer> entry : revenueMap.entrySet()) {
            if (entry.getValue() > maxRevenue) {
                maxRevenue = entry.getValue();
                topRevenueItem = entry.getKey();
            }
        }

        if (mostOrderedItemTv != null) {
            mostOrderedItemTv.setText(maxCount > 0 ? bestItem + " (" + maxCount + ")" : "N/A");
        }
        if (topRevenueItemTv != null) {
            topRevenueItemTv.setText(maxRevenue > 0 ? topRevenueItem + " (₹" + maxRevenue + ")" : "N/A");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_logout) {
            handleLogout();
            return true;
        } else if (item.getItemId() == R.id.action_change_password) {
            startActivity(new Intent(AdminActivity.this, ChangeAdminPasswordActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleLogout() {
        SharedPreferences sharedPreferences = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
        
        Intent intent = new Intent(AdminActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    class AdminOrderAdapter extends RecyclerView.Adapter<AdminOrderAdapter.ViewHolder> {
        private List<DocumentSnapshot> docs;

        AdminOrderAdapter(List<DocumentSnapshot> docs) {
            this.docs = docs;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            DocumentSnapshot doc = docs.get(position);
            String token = doc.getString("token");
            String status = doc.getString("status");
            String items = doc.getString("items");
            String userId = doc.getString("userId");
            String userName = doc.getString("userName");
            String scheduledTime = doc.getString("scheduledTime");
            Long timestamp = doc.getLong("timestamp");
            
            // Fix ₹0 issue by using the correct field and casting properly
            Object totalObj = doc.get("totalAmount");
            long total = 0;
            if (totalObj instanceof Long) {
                total = (Long) totalObj;
            } else if (totalObj instanceof Integer) {
                total = (Integer) totalObj;
            } else if (totalObj instanceof Double) {
                total = ((Double) totalObj).longValue();
            }

            holder.tokenTv.setText("Order #" + token);
            holder.orderIdTv.setText("ID: " + doc.getId());
            holder.statusTv.setText(status);
            holder.itemsTv.setText(items);
            holder.totalTv.setText("₹" + total);
            holder.userNameTv.setText("Customer: " + (userName != null ? userName : (userId != null ? userId : "Unknown")));
            holder.scheduledTimeTv.setText("Sched: " + (scheduledTime != null ? scheduledTime : "ASAP"));
            
            if (timestamp != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                holder.orderTimeTv.setText(sdf.format(new Date(timestamp)));
            } else {
                holder.orderTimeTv.setText("--:--");
            }

            // Status UI Update
            updateStatusUI(holder, status);

            // Admin Actions - MUST reset visibility to avoid RecyclerView recycling issues
            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnReady.setVisibility(View.GONE);

            if ("Pending".equalsIgnoreCase(status)) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Accept & Prepare");
                holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50)); // Green

                holder.btnAccept.setOnClickListener(v -> updateOrderStatus(doc, "Preparing", "Order Accepted & Preparing"));
                holder.btnReject.setOnClickListener(v -> updateOrderStatus(doc, "Rejected", "Order Rejected"));
                
            } else if ("Preparing".equalsIgnoreCase(status)) {
                holder.btnReady.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReady.setText("Mark as Ready");
                holder.btnReady.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF2196F3)); // Blue

                holder.btnReady.setOnClickListener(v -> updateOrderStatus(doc, "Ready", "Order is Ready for Pickup!"));
                holder.btnReject.setOnClickListener(v -> updateOrderStatus(doc, "Cancelled", "Order Cancelled"));

            } else if ("Ready".equalsIgnoreCase(status)) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Complete Order");
                holder.btnAccept.setBackgroundTintList(android.content.res.ColorStateList.valueOf(0xFF4CAF50));

                holder.btnAccept.setOnClickListener(v -> updateOrderStatus(doc, "Completed", "Order Delivered Successfully"));
            }
        }

        private void updateStatusUI(ViewHolder holder, String status) {
            holder.statusTv.setText(status);
            int bgRes = R.drawable.bg_status_grey;
            if ("Accepted".equalsIgnoreCase(status)) bgRes = R.drawable.bg_status_blue;
            else if ("Preparing".equalsIgnoreCase(status)) bgRes = R.drawable.bg_status_orange;
            else if ("Ready".equalsIgnoreCase(status)) bgRes = R.drawable.bg_status_green;
            else if ("Rejected".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status)) bgRes = R.drawable.bg_status_red;
            else if ("Completed".equalsIgnoreCase(status)) bgRes = R.drawable.bg_status_green;
            
            holder.statusTv.setBackgroundResource(bgRes);
            holder.statusTv.setTextColor(0xFFFFFFFF);
        }

        private void updateOrderStatus(DocumentSnapshot doc, String status, String message) {
            java.util.Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("status", status);
            if ("Rejected".equals(status)) {
                updates.put("rejectedAt", System.currentTimeMillis());
                updates.put("rejectReason", "Item not available / Canteen busy");
            }

            doc.getReference().update(updates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(AdminActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return docs.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tokenTv, orderIdTv, statusTv, itemsTv, totalTv, userNameTv, orderTimeTv, scheduledTimeTv;
            Button btnAccept, btnReject, btnReady;

            ViewHolder(View itemView) {
                super(itemView);
                tokenTv = itemView.findViewById(R.id.orderTokenTv);
                orderIdTv = itemView.findViewById(R.id.orderIdTv);
                statusTv = itemView.findViewById(R.id.orderStatusChip);
                itemsTv = itemView.findViewById(R.id.orderItemsTv);
                totalTv = itemView.findViewById(R.id.orderTotalTv);
                userNameTv = itemView.findViewById(R.id.userNameTv);
                orderTimeTv = itemView.findViewById(R.id.orderTimeTv);
                scheduledTimeTv = itemView.findViewById(R.id.scheduledTimeTv);
                btnAccept = itemView.findViewById(R.id.btnAccept);
                btnReject = itemView.findViewById(R.id.btnReject);
                btnReady = itemView.findViewById(R.id.btnReady);
            }
        }
    }
}
