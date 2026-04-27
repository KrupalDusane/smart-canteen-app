package com.example.nmimscanteenapp.model;

import java.io.Serializable;
import com.google.firebase.Timestamp;

public class Order implements Serializable {
    private String userId;
    private String userName;
    private String items;
    private int totalAmount;
    private String token;
    private String scheduledTime;
    private String estimatedReadyTime;
    private String status; // Pending, Accepted, Preparing, Ready, Completed, Cancelled
    private String rejectReason;
    private long timestamp;
    private Long createdAt;
    private long readyTimestamp;
    private long rejectedAt;

    public Order() {
        // Required for Firestore toObject()
    }

    public Order(String userId, String userName, String items, int totalAmount, String token, String scheduledTime, String status, long timestamp) {
        this.userId = userId;
        this.userName = userName;
        this.items = items;
        this.totalAmount = totalAmount;
        this.token = token;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.timestamp = timestamp;
        this.estimatedReadyTime = "Pending";
    }

    public void updateStatusBasedOnTime() {
        if ("Completed".equals(status) || "Cancelled".equals(status) || "Pending".equals(status) || "Rejected".equals(status)) return;

        long now = System.currentTimeMillis();
        long totalDuration = readyTimestamp - timestamp;
        
        if (now >= readyTimestamp) {
            status = "Ready";
        } else if (now > timestamp + (totalDuration / 2)) {
            status = "Cooking";
        } else {
            status = "Preparing";
        }
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getItems() { return items; }
    public int getTotalAmount() { return totalAmount; }
    public String getToken() { return token; }
    public String getScheduledTime() { return scheduledTime; }
    public String getEstimatedReadyTime() { return estimatedReadyTime; }
    public void setEstimatedReadyTime(String estimatedReadyTime) { this.estimatedReadyTime = estimatedReadyTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRejectReason() { return rejectReason; }
    public void setRejectReason(String rejectReason) { this.rejectReason = rejectReason; }
    public long getTimestamp() { return timestamp; }
    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Object createdAt) {
        if (createdAt instanceof Long) {
            this.createdAt = (Long) createdAt;
        } else if (createdAt instanceof Timestamp) {
            this.createdAt = ((Timestamp) createdAt).toDate().getTime();
        }
    }
    public long getReadyTimestamp() { return readyTimestamp; }
    public void setReadyTimestamp(long readyTimestamp) { this.readyTimestamp = readyTimestamp; }
    public long getRejectedAt() { return rejectedAt; }
    public void setRejectedAt(long rejectedAt) { this.rejectedAt = rejectedAt; }
}
