package com.example.nmimscanteenapp.student;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.utils.NotificationHelper;

public class OrderConfirmationActivity extends AppCompatActivity {

    private String token, items;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_confirmation);

        // Initialize UI
        TextView tokenTv = findViewById(R.id.tokenTv);
        TextView totalTv = findViewById(R.id.totalTv);
        TextView estTimeTv = findViewById(R.id.estTimeTv);
        TextView orderStatusTv = findViewById(R.id.orderStatusTv);
        Button btnBackToHome = findViewById(R.id.btnBackToHome);
        Button btnViewOrder = findViewById(R.id.btnViewOrder);

        // Get data from intent
        token = getIntent().getStringExtra("token");
        items = getIntent().getStringExtra("items");
        int total = getIntent().getIntExtra("total", 0);
        String scheduledTime = getIntent().getStringExtra("scheduledTime");

        // Display Data
        if (tokenTv != null) tokenTv.setText(token);
        if (totalTv != null) totalTv.setText("₹" + total);
        
        if (orderStatusTv != null) {
            orderStatusTv.setText("Status: Pending Verification");
        }

        if (estTimeTv != null) {
            estTimeTv.setText("Waiting for Canteen...");
        }

        // Create Notification Channel
        NotificationHelper.createNotificationChannel(this);

        btnBackToHome.setOnClickListener(v -> {
            Intent intent = new Intent(OrderConfirmationActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        btnViewOrder.setOnClickListener(v -> {
            Intent intent = new Intent(OrderConfirmationActivity.this, OrderHistoryActivity.class);
            startActivity(intent);
        });
    }

    private void startOrderTimers(long totalTimeMillis, TextView estTimeTv) {
        countDownTimer = new CountDownTimer(totalTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long sec = (millisUntilFinished / 1000) % 60;
                long min = (millisUntilFinished / 1000) / 60;
                if (estTimeTv != null) {
                    estTimeTv.setText(String.format(java.util.Locale.getDefault(), "%02d:%02d", min, sec));
                }
            }

            @Override
            public void onFinish() {
                markOrderReady();
            }
        }.start();
    }

    private void markOrderReady() {
        TextView orderStatusTv = findViewById(R.id.orderStatusTv);
        TextView estTimeTv = findViewById(R.id.estTimeTv);
        
        if (orderStatusTv != null) {
            orderStatusTv.setText("Status: Ready to Collect!");
            orderStatusTv.setTextColor(Color.parseColor("#4CAF50"));
        }
        if (estTimeTv != null) {
            estTimeTv.setText("Ready!");
        }
        
        NotificationHelper.showOrderReadyNotification(this, token, items);
        Toast.makeText(OrderConfirmationActivity.this, "Order Ready!", Toast.LENGTH_LONG).show();

        // 4. Start Alert Timer (Not collected alert)
        startNotCollectedTimer();
    }

    private void startNotCollectedTimer() {
        // Alert after 5 minutes (demo: 30 seconds)
        countDownTimer = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                NotificationHelper.showOrderReadyNotification(OrderConfirmationActivity.this, token, items);
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
}
