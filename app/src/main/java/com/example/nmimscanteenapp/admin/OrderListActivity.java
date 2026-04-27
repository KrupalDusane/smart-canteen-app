package com.example.nmimscanteenapp.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nmimscanteenapp.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrderListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private GroupedOrderAdapter adapter;
    private List<Object> listItems = new ArrayList<>();
    private String type;
    private FirebaseFirestore db;
    private LinearLayout emptyState;
    private TextView titleTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_list);

        db = FirebaseFirestore.getInstance();
        type = getIntent().getStringExtra("type");

        recyclerView = findViewById(R.id.orderListRv);
        emptyState = findViewById(R.id.emptyStateLayout);
        titleTv = findViewById(R.id.titleTv);
        ImageView backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(v -> finish());

        if ("PENDING".equals(type)) titleTv.setText("Pending Orders");
        else if ("CANCELLED".equals(type)) titleTv.setText("Cancelled Orders");
        else if ("COMPLETED".equals(type)) titleTv.setText("Completed Orders");
        else titleTv.setText("All Orders");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroupedOrderAdapter(listItems);
        recyclerView.setAdapter(adapter);

        listenToOrders();
    }

    private void listenToOrders() {
        Query query = db.collection("orders").orderBy("createdAt", Query.Direction.DESCENDING);

        query.addSnapshotListener((value, error) -> {
            if (value != null) {
                listItems.clear();
                
                if ("ALL".equals(type)) {
                    Map<String, List<DocumentSnapshot>> groupedOrders = new LinkedHashMap<>();
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        com.example.nmimscanteenapp.model.Order order = doc.toObject(com.example.nmimscanteenapp.model.Order.class);
                        if (order == null || order.getCreatedAt() == null) continue;
                        
                        long time = order.getCreatedAt();
                        String dateLabel = formatDate(time);
                        if (!groupedOrders.containsKey(dateLabel)) {
                            groupedOrders.put(dateLabel, new ArrayList<>());
                        }
                        groupedOrders.get(dateLabel).add(doc);
                    }

                    for (Map.Entry<String, List<DocumentSnapshot>> entry : groupedOrders.entrySet()) {
                        listItems.add(entry.getKey()); // Add date header
                        listItems.addAll(entry.getValue()); // Add orders for that date
                    }
                } else {
                    for (DocumentSnapshot doc : value.getDocuments()) {
                        String status = doc.getString("status");
                        if ("PENDING".equals(type) && "Pending".equalsIgnoreCase(status)) {
                            listItems.add(doc);
                        } else if ("COMPLETED".equals(type) && "Completed".equalsIgnoreCase(status)) {
                            listItems.add(doc);
                        } else if ("CANCELLED".equals(type) && ("Cancelled".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status))) {
                            listItems.add(doc);
                        }
                    }
                }
                
                adapter.notifyDataSetChanged();
                emptyState.setVisibility(listItems.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    private String formatDate(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DATE, -1);

        if (isSameDay(calendar, today)) {
            return "Today";
        } else if (isSameDay(calendar, yesterday)) {
            return "Yesterday";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            return sdf.format(new Date(time));
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    class GroupedOrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ORDER = 1;
        private List<Object> items;
        private AdminActivity.AdminOrderAdapter orderAdapterDelegate;

        GroupedOrderAdapter(List<Object> items) {
            this.items = items;
            // Use AdminOrderAdapter logic for order items to keep UI same
            this.orderAdapterDelegate = new AdminActivity().new AdminOrderAdapter(new ArrayList<>());
        }

        @Override
        public int getItemViewType(int position) {
            return (items.get(position) instanceof String) ? TYPE_HEADER : TYPE_ORDER;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_HEADER) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_date_header, parent, false);
                return new HeaderViewHolder(view);
            } else {
                return orderAdapterDelegate.onCreateViewHolder(parent, viewType);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_HEADER) {
                ((HeaderViewHolder) holder).dateTv.setText("📅 " + (String) items.get(position));
            } else {
                DocumentSnapshot doc = (DocumentSnapshot) items.get(position);
                // We need to pass the doc to the delegate. Since the delegate's onBindViewHolder 
                // uses its own list, we can temporarily swap or just use a custom bind.
                // However, AdminOrderAdapter is designed to use its internal 'docs' list.
                // A better way is to make AdminOrderAdapter's logic reusable.
                // For now, let's manually bind to avoid complex delegation issues.
                bindOrderViewHolder((AdminActivity.AdminOrderAdapter.ViewHolder) holder, doc);
            }
        }

        private void bindOrderViewHolder(AdminActivity.AdminOrderAdapter.ViewHolder holder, DocumentSnapshot doc) {
            com.example.nmimscanteenapp.model.Order order = doc.toObject(com.example.nmimscanteenapp.model.Order.class);
            if (order == null) return;

            String token = order.getToken();
            String status = order.getStatus();
            String items = order.getItems();
            String userName = order.getUserName();
            String scheduledTime = order.getScheduledTime();
            long total = order.getTotalAmount();
            Long timestamp = order.getCreatedAt();
            if (timestamp == null) timestamp = order.getTimestamp();

            holder.tokenTv.setText("Order #" + token);
            holder.orderIdTv.setText("ID: " + doc.getId());
            holder.statusTv.setText(status);
            holder.itemsTv.setText(items);
            holder.totalTv.setText("₹" + total);
            holder.userNameTv.setText("Customer: " + (userName != null ? userName : "Unknown"));
            holder.scheduledTimeTv.setText("Sched: " + (scheduledTime != null ? scheduledTime : "ASAP"));
            
            if (timestamp != 0) {
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                holder.orderTimeTv.setText(sdf.format(new Date(timestamp)));
            } else {
                holder.orderTimeTv.setText("--:--");
            }

            updateStatusUI(holder, status);

            holder.btnAccept.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnReady.setVisibility(View.GONE);

            if ("Pending".equalsIgnoreCase(status)) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Accept & Prepare");
                holder.btnAccept.setOnClickListener(v -> updateOrderStatus(doc, "Preparing", "Order Accepted & Preparing"));
                holder.btnReject.setOnClickListener(v -> updateOrderStatus(doc, "Rejected", "Order Rejected"));
            } else if ("Preparing".equalsIgnoreCase(status)) {
                holder.btnReady.setVisibility(View.VISIBLE);
                holder.btnReject.setVisibility(View.VISIBLE);
                holder.btnReady.setText("Mark as Ready");
                holder.btnReady.setOnClickListener(v -> updateOrderStatus(doc, "Ready", "Order is Ready for Pickup!"));
                holder.btnReject.setOnClickListener(v -> updateOrderStatus(doc, "Cancelled", "Order Cancelled"));
            } else if ("Ready".equalsIgnoreCase(status)) {
                holder.btnAccept.setVisibility(View.VISIBLE);
                holder.btnAccept.setText("Complete Order");
                holder.btnAccept.setOnClickListener(v -> updateOrderStatus(doc, "Completed", "Order Delivered Successfully"));
            }
        }

        private void updateStatusUI(AdminActivity.AdminOrderAdapter.ViewHolder holder, String status) {
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
            Map<String, Object> updates = new java.util.HashMap<>();
            updates.put("status", status);
            if ("Rejected".equals(status)) {
                updates.put("rejectedAt", System.currentTimeMillis());
                updates.put("rejectReason", "Item not available / Canteen busy");
            }

            doc.getReference().update(updates)
                    .addOnSuccessListener(aVoid -> Toast.makeText(OrderListActivity.this, message, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(OrderListActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {
            TextView dateTv;
            HeaderViewHolder(View itemView) {
                super(itemView);
                dateTv = itemView.findViewById(R.id.dateTv);
            }
        }
    }
}
