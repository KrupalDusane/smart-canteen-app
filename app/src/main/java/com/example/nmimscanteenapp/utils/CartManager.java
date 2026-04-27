package com.example.nmimscanteenapp.utils;

import com.example.nmimscanteenapp.model.CartItem;
import com.example.nmimscanteenapp.model.FoodItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CartManager {
    private static CartManager instance;
    private final Map<String, Integer> cartMap;
    private final ArrayList<CartItem> cartList;

    private CartManager() {
        cartMap = new HashMap<>();
        cartList = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void updateCart(FoodItem item, int quantity) {
        if (quantity > 0) {
            cartMap.put(item.getName(), quantity);
        } else {
            cartMap.remove(item.getName());
        }
        updateCartList(item, quantity);
    }

    private void updateCartList(FoodItem item, int quantity) {
        // Find if item already exists in cartList
        int index = -1;
        for (int i = 0; i < cartList.size(); i++) {
            if (cartList.get(i).getName().equals(item.getName())) {
                index = i;
                break;
            }
        }

        if (quantity > 0) {
            if (index != -1) {
                cartList.get(index).setQuantity(quantity);
            } else {
                cartList.add(new CartItem(item.getName(), item.getPrice(), quantity, item.getImageResId()));
            }
        } else {
            if (index != -1) {
                cartList.remove(index);
            }
        }
    }

    public Map<String, Integer> getCartMap() {
        return cartMap;
    }

    public ArrayList<CartItem> getCartList() {
        return cartList;
    }

    public int getTotalItemsCount() {
        int count = 0;
        for (int qty : cartMap.values()) {
            count += qty;
        }
        return count;
    }

    public int getTotalPrice() {
        int total = 0;
        for (CartItem item : cartList) {
            total += (item.getPrice() * item.getQuantity());
        }
        return total;
    }

    public void clearCart() {
        cartMap.clear();
        cartList.clear();
    }
}
