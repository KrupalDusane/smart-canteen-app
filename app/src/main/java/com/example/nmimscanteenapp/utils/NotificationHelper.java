package com.example.nmimscanteenapp.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.student.OrderHistoryActivity;

import java.util.Random;

public class NotificationHelper {
    private static final String CHANNEL_ID = "order_status_channel";
    private static final String CHANNEL_NAME = "Order Status Updates";
    private static final String CHANNEL_DESC = "Notifications for your food order status";

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    public static void showOrderNotification(Context context, String title, String message) {
        Intent intent = new Intent(context, OrderHistoryActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500})
                .setDefaults(NotificationCompat.DEFAULT_SOUND)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(new Random().nextInt(), builder.build());
    }

    public static void showOrderReadyNotification(Context context, String token, String items) {
        showOrderNotification(context, "Order Ready", "Your order #" + token + " is ready! Please collect it");
    }

    public static void showOrderReadyNotification(Context context, String token) {
        showOrderReadyNotification(context, token, null);
    }

    public static void showOrderRejectedNotification(Context context, String token, String reason) {
        showOrderNotification(context, "Order Rejected", "Your order #" + token + " was rejected: " + (reason != null ? reason : "Canteen busy"));
    }

    public static void showOrderPreparingNotification(Context context, String token) {
        showOrderNotification(context, "Order Preparing", "Your order #" + token + " is being prepared");
    }

    public static void showOrderAcceptedNotification(Context context, String token) {
        showOrderNotification(context, "Order Accepted", "Your order #" + token + " has been accepted by the canteen");
    }
}
