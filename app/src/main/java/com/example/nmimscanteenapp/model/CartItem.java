package com.example.nmimscanteenapp.model;

import java.io.Serializable;
import java.util.Objects;

public class CartItem implements Serializable {
    private String name;
    private int price;
    private int quantity;
    private int imageResId;

    public CartItem(String name, int price, int quantity, int imageResId) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getImageResId() {
        return imageResId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(name, cartItem.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
