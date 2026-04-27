package com.example.nmimscanteenapp.model;

public class FoodItem {
    private String itemId;
    private String name;
    private int price;
    private int imageResId;
    private String imageName; // For Firestore compatibility, storing drawable name as string
    private String category;
    private boolean isPopular;
    private boolean available = true;

    // No-arg constructor for Firestore
    public FoodItem() {
    }

    public FoodItem(String name, int price, int imageResId) {
        this(name, price, imageResId, "Special", false);
    }

    public FoodItem(String name, int price, int imageResId, String category) {
        this(name, price, imageResId, category, false);
    }

    public FoodItem(String name, int price, int imageResId, String category, boolean isPopular) {
        this.name = name;
        this.price = price;
        this.imageResId = imageResId;
        this.category = category;
        this.isPopular = isPopular;
    }

    // New constructor for Firestore sync
    public FoodItem(String itemId, String name, int price, String imageName, String category, boolean available) {
        this.itemId = itemId;
        this.name = name;
        this.price = price;
        this.imageName = imageName;
        this.category = category;
        this.available = available;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getImageResId() {
        return imageResId;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getCategory() {
        return category;
    }

    public boolean isPopular() {
        return isPopular;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
