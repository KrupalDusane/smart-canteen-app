package com.example.nmimscanteenapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.model.CartItem;
import com.example.nmimscanteenapp.utils.CartManager;

import java.util.ArrayList;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private ArrayList<CartItem> cartItems;
    private OnCartUpdateListener listener;

    public interface OnCartUpdateListener {
        void onCartChanged();
    }

    public CartAdapter(ArrayList<CartItem> cartItems, OnCartUpdateListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem currentItem = cartItems.get(position);

        holder.cartItemNameTv.setText(currentItem.getName());
        holder.cartItemPriceTv.setText("₹" + currentItem.getPrice());
        holder.cartQuantityTv.setText(String.valueOf(currentItem.getQuantity()));
        holder.cartItemImageView.setImageResource(currentItem.getImageResId());

        holder.btnCartPlus.setOnClickListener(v -> {
            int newQty = currentItem.getQuantity() + 1;
            currentItem.setQuantity(newQty);
            CartManager.getInstance().getCartMap().put(currentItem.getName(), newQty);
            notifyItemChanged(position);
            if (listener != null) listener.onCartChanged();
        });

        holder.btnCartMinus.setOnClickListener(v -> {
            if (currentItem.getQuantity() > 1) {
                int newQty = currentItem.getQuantity() - 1;
                currentItem.setQuantity(newQty);
                CartManager.getInstance().getCartMap().put(currentItem.getName(), newQty);
                notifyItemChanged(position);
            } else {
                CartManager.getInstance().getCartMap().remove(currentItem.getName());
                cartItems.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartItems.size());
            }
            if (listener != null) listener.onCartChanged();
        });

        holder.btnDeleteItem.setOnClickListener(v -> {
            CartManager.getInstance().getCartMap().remove(currentItem.getName());
            cartItems.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, cartItems.size());
            if (listener != null) listener.onCartChanged();
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        public ImageView cartItemImageView;
        public TextView cartItemNameTv, cartItemPriceTv, cartQuantityTv;
        public ImageButton btnCartMinus, btnCartPlus, btnDeleteItem;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImageView = itemView.findViewById(R.id.cartItemImageView);
            cartItemNameTv = itemView.findViewById(R.id.cartItemNameTv);
            cartItemPriceTv = itemView.findViewById(R.id.cartItemPriceTv);
            cartQuantityTv = itemView.findViewById(R.id.cartQuantityTv);
            btnCartMinus = itemView.findViewById(R.id.btnCartMinus);
            btnCartPlus = itemView.findViewById(R.id.btnCartPlus);
            btnDeleteItem = itemView.findViewById(R.id.btnDeleteItem);
        }
    }
}
