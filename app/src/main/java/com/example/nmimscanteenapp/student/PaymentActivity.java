package com.example.nmimscanteenapp.student;

import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.model.CartItem;
import com.example.nmimscanteenapp.model.Order;
import com.example.nmimscanteenapp.utils.CartManager;
import com.example.nmimscanteenapp.utils.NotificationHelper;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class PaymentActivity extends AppCompatActivity {

    private String sapId;
    private int total;
    private ArrayList<CartItem> cartList;
    private FirebaseFirestore db;
    private TextInputEditText transactionIdInput;
    private String scheduledTime = null;
    private TextView scheduledTimeTv, paymentTimerTv;
    private CheckBox paymentPaidCb;
    private RadioButton radioCash;
    private boolean isCashSelected = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        db = FirebaseFirestore.getInstance();

        // Receive data from Intent
        sapId = getIntent().getStringExtra("sapId");
        total = getIntent().getIntExtra("total", 0);
        cartList = (ArrayList<CartItem>) getIntent().getSerializableExtra("cartList");

        // Initialize UI
        TextView paymentTotalTv = findViewById(R.id.paymentTotalTv);
        paymentTotalTv.setText("Total: ₹" + total);

        paymentTimerTv = findViewById(R.id.paymentTimerTv);
        scheduledTimeTv = findViewById(R.id.scheduledTimeTv);
        View btnSchedulePickup = findViewById(R.id.btnSchedulePickup);
        radioCash = findViewById(R.id.radioCash);
        View btnCashOnPickup = findViewById(R.id.btnCashOnPickup);

        btnSchedulePickup.setOnClickListener(v -> showTimePicker());

        btnCashOnPickup.setOnClickListener(v -> {
            isCashSelected = !isCashSelected;
            radioCash.setChecked(isCashSelected);
            if (isCashSelected) {
                Toast.makeText(this, "Cash on Pickup selected", Toast.LENGTH_SHORT).show();
            }
        });

        // UPI Intent Buttons
        findViewById(R.id.btnGPay).setOnClickListener(v -> launchUpiIntent("com.google.android.apps.ncore.membership"));
        findViewById(R.id.btnPhonePe).setOnClickListener(v -> launchUpiIntent("com.phonepe.app"));
        findViewById(R.id.btnNavi).setOnClickListener(v -> launchUpiIntent("com.naviapp"));

        Button btnProceedPayment = findViewById(R.id.btnProceedPayment);

        startPaymentTimer();

        btnProceedPayment.setOnClickListener(v -> {
            // Check if pickup time is selected
            if (scheduledTime == null) {
                Toast.makeText(this, "Please select pickup time", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check timing - Inform user but don't block DB write
            if (!isCanteenOpen()) {
                Toast.makeText(this, "Note: Canteen is currently closed (9 AM - 7 PM). Your order will be processed when we open.", Toast.LENGTH_LONG).show();
            }

            // Generate token
            int randomNum = new Random().nextInt(900) + 100;
            String token = "NM" + randomNum;
            
            String transactionId;
            if (isCashSelected) {
                transactionId = "CASH_ON_PICKUP";
            } else {
                // Transaction ID is now random for simulated flow since UTR input is removed
                transactionId = "TXN" + System.currentTimeMillis();
            }
            processOrder(token, transactionId);
        });
    }

    private void launchUpiIntent(String packageName) {
        String upiId = "nmims.canteen@okaxis"; // Fixed UPI ID
        String name = "NMIMS Canteen";
        String note = "Order Payment";
        String amount = String.valueOf(total);

        android.net.Uri uri = android.net.Uri.parse("upi://pay").buildUpon()
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", name)
                .appendQueryParameter("tn", note)
                .appendQueryParameter("am", amount)
                .appendQueryParameter("cu", "INR")
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.setPackage(packageName);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isCanteenOpen() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return hour >= 9 && hour < 19;
    }

    private void startPaymentTimer() {
        countDownTimer = new CountDownTimer(120000, 1000) {
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                paymentTimerTv.setText(String.format(Locale.getDefault(), "Complete payment within %02d:%02d", minutes, seconds));
            }

            public void onFinish() {
                paymentTimerTv.setText("Payment window expired!");
                paymentTimerTv.setTextColor(android.graphics.Color.RED);
                Toast.makeText(PaymentActivity.this, "Payment session expired. Please try again.", Toast.LENGTH_LONG).show();
                finish();
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }

    private void showTimePicker() {
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(PaymentActivity.this, (timePicker, selectedHour, selectedMinute) -> {
            // Time validation (9 AM - 8 PM)
            if (selectedHour < 9 || selectedHour >= 20) {
                Toast.makeText(this, "Canteen is open 9:00 AM - 8:00 PM", Toast.LENGTH_LONG).show();
                return;
            }

            // Past time validation
            Calendar selectedTimeCal = Calendar.getInstance();
            selectedTimeCal.set(Calendar.HOUR_OF_DAY, selectedHour);
            selectedTimeCal.set(Calendar.MINUTE, selectedMinute);
            
            if (selectedTimeCal.before(Calendar.getInstance())) {
                Toast.makeText(this, "Cannot select a past time", Toast.LENGTH_SHORT).show();
                return;
            }

            String am_pm = (selectedHour < 12) ? "AM" : "PM";
            int hourDisplay = (selectedHour > 12) ? selectedHour - 12 : (selectedHour == 0 ? 12 : selectedHour);
            scheduledTime = String.format(Locale.getDefault(), "%02d:%02d %s", hourDisplay, selectedMinute, am_pm);
            scheduledTimeTv.setText("⏰ Pickup at " + scheduledTime);
            scheduledTimeTv.setTextColor(getResources().getColor(R.color.nmims_red));
        }, hour, minute, false);
        mTimePicker.setTitle("Select Pickup Time");
        mTimePicker.show();
    }

    private void processOrder(String token, String transactionId) {
        SharedPreferences prefs = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE);
        String userName = prefs.getString("name", "User");

        // 1. Convert cart items to a detailed summary
        StringBuilder itemsBuilder = new StringBuilder();
        if (cartList != null) {
            for (int i = 0; i < cartList.size(); i++) {
                CartItem item = cartList.get(i);
                itemsBuilder.append(item.getName())
                        .append(" (x")
                        .append(item.getQuantity())
                        .append(")");
                if (i < cartList.size() - 1) {
                    itemsBuilder.append(", ");
                }
            }
        }
        String itemsString = itemsBuilder.toString();

        // 2. Save to Firestore
        Map<String, Object> orderMap = new HashMap<>();
        orderMap.put("userId", sapId);
        orderMap.put("userName", userName);
        orderMap.put("items", itemsString);
        orderMap.put("totalAmount", total);
        orderMap.put("token", token);
        orderMap.put("transactionId", transactionId);
        orderMap.put("status", "Pending");
        orderMap.put("scheduledTime", scheduledTime);
        orderMap.put("timestamp", System.currentTimeMillis());
        orderMap.put("createdAt", System.currentTimeMillis());

        setLoadingState(true);

        db.collection("orders").add(orderMap)
                .addOnSuccessListener(documentReference -> {
                    // Clear Cart
                    CartManager.getInstance().clearCart();

                    Toast.makeText(this, "Order Placed! Pending verification...", Toast.LENGTH_SHORT).show();
                    
                    // 3. Open OrderConfirmationActivity
                    Intent intent = new Intent(PaymentActivity.this, OrderConfirmationActivity.class);
                    intent.putExtra("token", token);
                    intent.putExtra("total", total);
                    intent.putExtra("items", itemsString);
                    intent.putExtra("scheduledTime", scheduledTime);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoadingState(false);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void saveOrderLocally(Order order) {
        SharedPreferences sharedPreferences = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("order_history", "");
        Gson gson = new Gson();
        List<Order> orderList;
        
        if (json.isEmpty()) {
            orderList = new ArrayList<>();
        } else {
            Type type = new TypeToken<ArrayList<Order>>() {}.getType();
            orderList = gson.fromJson(json, type);
        }
        
        orderList.add(order);
        String updatedJson = gson.toJson(orderList);
        sharedPreferences.edit().putString("order_history", updatedJson).apply();
    }

    private void simulateOrderProcessing(Order order, long delay) {
        // Step 1: Preparing to Cooking (halfway)
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            order.setStatus("Cooking");
            updateOrderInStorage(order);
        }, delay / 2);

        // Step 2: Cooking to Ready
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            order.setStatus("Ready");
            order.setEstimatedReadyTime("Now");
            updateOrderInStorage(order);
            NotificationHelper.showOrderReadyNotification(this, order.getToken(), order.getItems());
        }, delay);
    }

    private void updateOrderInStorage(Order updatedOrder) {
        SharedPreferences sharedPreferences = getSharedPreferences("NMIMS_Canteen", Context.MODE_PRIVATE);
        String json = sharedPreferences.getString("order_history", "");
        if (!json.isEmpty()) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Order>>() {}.getType();
            List<Order> orderList = gson.fromJson(json, type);
            
            for (int i = 0; i < orderList.size(); i++) {
                if (orderList.get(i).getToken().equals(updatedOrder.getToken())) {
                    orderList.set(i, updatedOrder);
                    break;
                }
            }
            
            String updatedJson = gson.toJson(orderList);
            sharedPreferences.edit().putString("order_history", updatedJson).apply();
        }
    }

    private void setLoadingState(boolean isLoading) {
        Button btn = findViewById(R.id.btnProceedPayment);
        if (btn != null) {
            btn.setEnabled(!isLoading);
            btn.setAlpha(isLoading ? 0.5f : 1.0f);
        }
    }
}
