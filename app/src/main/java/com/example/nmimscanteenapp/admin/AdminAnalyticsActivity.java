package com.example.nmimscanteenapp.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.nmimscanteenapp.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AdminAnalyticsActivity extends AppCompatActivity {

    private TextView selectedDateTv, revenueAnalyticsTv, totalOrdersAnalyticsTv, cancelledOrdersAnalyticsTv, noDataTv;
    private TextView btnDayMode, btnMonthMode, datePickerLabel;
    private FirebaseFirestore db;
    private String selectedDate, selectedMonth;
    private String selectedMode = "DAY"; // "DAY" or "MONTH"
    
    private SimpleDateFormat dayFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private SimpleDateFormat monthFormat = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
    private SimpleDateFormat displayMonthFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());

    private ListenerRegistration registration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_analytics);

        Toolbar toolbar = findViewById(R.id.analyticsToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        db = FirebaseFirestore.getInstance();

        selectedDateTv = findViewById(R.id.selectedDateTv);
        revenueAnalyticsTv = findViewById(R.id.revenueAnalyticsTv);
        totalOrdersAnalyticsTv = findViewById(R.id.totalOrdersAnalyticsTv);
        cancelledOrdersAnalyticsTv = findViewById(R.id.cancelledOrdersAnalyticsTv);
        noDataTv = findViewById(R.id.noDataTv);
        btnDayMode = findViewById(R.id.btnDayMode);
        btnMonthMode = findViewById(R.id.btnMonthMode);
        datePickerLabel = findViewById(R.id.datePickerLabel);

        // Initialize dates
        Calendar calendar = Calendar.getInstance();
        selectedDate = dayFormat.format(calendar.getTime());
        selectedMonth = monthFormat.format(calendar.getTime());
        selectedDateTv.setText(selectedDate);

        setupToggles();
        findViewById(R.id.datePickerCard).setOnClickListener(v -> showDatePicker());

        fetchAnalyticsData();
    }

    private void setupToggles() {
        btnDayMode.setOnClickListener(v -> {
            selectedMode = "DAY";
            btnDayMode.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnDayMode.setTextColor(0xFFFFFFFF);
            btnMonthMode.setBackground(null);
            btnMonthMode.setTextColor(0xFF666666);
            datePickerLabel.setText("Select Date");
            selectedDateTv.setText(selectedDate);
            fetchAnalyticsData();
        });

        btnMonthMode.setOnClickListener(v -> {
            selectedMode = "MONTH";
            btnMonthMode.setBackgroundResource(R.drawable.bg_toggle_selected);
            btnMonthMode.setTextColor(0xFFFFFFFF);
            btnDayMode.setBackground(null);
            btnDayMode.setTextColor(0xFF666666);
            datePickerLabel.setText("Select Month");
            
            try {
                Date date = monthFormat.parse(selectedMonth);
                if (date != null) selectedDateTv.setText(displayMonthFormat.format(date));
            } catch (Exception e) {
                selectedDateTv.setText(selectedMonth);
            }
            
            fetchAnalyticsData();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            
            if (selectedMode.equals("DAY")) {
                selectedDate = dayFormat.format(selected.getTime());
                selectedDateTv.setText(selectedDate);
            } else {
                selectedMonth = monthFormat.format(selected.getTime());
                selectedDateTv.setText(displayMonthFormat.format(selected.getTime()));
            }
            fetchAnalyticsData();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void fetchAnalyticsData() {
        if (registration != null) {
            registration.remove();
        }

        registration = db.collection("orders").addSnapshotListener((value, error) -> {
            if (error != null) {
                Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }

            if (value != null) {
                int totalRevenue = 0;
                int totalOrders = 0;
                int cancelledOrders = 0;

                for (DocumentSnapshot doc : value.getDocuments()) {
                    com.example.nmimscanteenapp.model.Order order = doc.toObject(com.example.nmimscanteenapp.model.Order.class);
                    if (order == null || order.getCreatedAt() == null) continue;

                    long createdAtMillis = order.getCreatedAt();
                    String status = order.getStatus();
                    int amount = order.getTotalAmount();

                    Date date = new Date(createdAtMillis);
                    String orderDay = dayFormat.format(date);
                    String orderMonth = monthFormat.format(date);

                    boolean match = false;
                    if (selectedMode.equals("DAY") && orderDay.equals(selectedDate)) match = true;
                    else if (selectedMode.equals("MONTH") && orderMonth.equals(selectedMonth)) match = true;

                    if (match) {
                        totalOrders++;
                        if ("Cancelled".equalsIgnoreCase(status) || "Rejected".equalsIgnoreCase(status)) {
                            cancelledOrders++;
                        } else {
                            totalRevenue += amount;
                        }
                    }
                }
                updateUI(totalRevenue, totalOrders, cancelledOrders);
            }
        });
    }

    private void updateUI(int revenue, int orders, int cancelled) {
        revenueAnalyticsTv.setText("₹" + revenue);
        totalOrdersAnalyticsTv.setText(String.valueOf(orders));
        cancelledOrdersAnalyticsTv.setText(String.valueOf(cancelled));

        if (orders == 0) noDataTv.setVisibility(View.VISIBLE);
        else noDataTv.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registration != null) registration.remove();
    }
}