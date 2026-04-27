package com.example.nmimscanteenapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.model.FoodItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MenuAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private ArrayList<Object> items; // Can hold String (Header) or FoodItem
    private OnCartChangeListener listener;
    private Map<String, Integer> quantityMap;

    public interface OnCartChangeListener {
        void onQuantityChange(FoodItem foodItem, int quantity);
    }

    public MenuAdapter(ArrayList<Object> items, Map<String, Integer> quantityMap, OnCartChangeListener listener) {
        this.items = items;
        this.quantityMap = quantityMap;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food, parent, false);
            return new FoodViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).headerTitle.setText((String) items.get(position));
        } else {
            FoodViewHolder foodHolder = (FoodViewHolder) holder;
            FoodItem currentItem = (FoodItem) items.get(position);

            foodHolder.foodNameTv.setText(currentItem.getName());
            foodHolder.foodPriceTv.setText("₹" + currentItem.getPrice());
            
            // Load image by drawable name if available, else use resId
            if (currentItem.getImageName() != null && !currentItem.getImageName().isEmpty()) {
                int resId = foodHolder.itemView.getContext().getResources().getIdentifier(
                        currentItem.getImageName(), "drawable", foodHolder.itemView.getContext().getPackageName());
                if (resId != 0) {
                    foodHolder.foodImageView.setImageResource(resId);
                } else {
                    foodHolder.foodImageView.setImageResource(currentItem.getImageResId() != 0 ? currentItem.getImageResId() : R.drawable.canteen);
                }
            } else {
                foodHolder.foodImageView.setImageResource(currentItem.getImageResId() != 0 ? currentItem.getImageResId() : R.drawable.canteen);
            }

            foodHolder.foodCategoryTv.setText(currentItem.getCategory());

            if (currentItem.isPopular()) {
                foodHolder.popularTag.setVisibility(View.VISIBLE);
            } else {
                foodHolder.popularTag.setVisibility(View.GONE);
            }

            int quantity = quantityMap.getOrDefault(currentItem.getName(), 0);
            updateQuantityUI(foodHolder, quantity);

            foodHolder.btnAddToCart.setOnClickListener(v -> {
                int newQty = 1;
                quantityMap.put(currentItem.getName(), newQty);
                updateQuantityUI(foodHolder, newQty);
                if (listener != null) {
                    listener.onQuantityChange(currentItem, newQty);
                }
            });

            foodHolder.btnPlus.setOnClickListener(v -> {
                int currentQty = quantityMap.getOrDefault(currentItem.getName(), 0);
                int newQty = currentQty + 1;
                quantityMap.put(currentItem.getName(), newQty);
                updateQuantityUI(foodHolder, newQty);
                if (listener != null) {
                    listener.onQuantityChange(currentItem, newQty);
                }
            });

            foodHolder.btnMinus.setOnClickListener(v -> {
                int currentQty = quantityMap.getOrDefault(currentItem.getName(), 0);
                if (currentQty > 0) {
                    int newQty = currentQty - 1;
                    if (newQty == 0) {
                        quantityMap.remove(currentItem.getName());
                    } else {
                        quantityMap.put(currentItem.getName(), newQty);
                    }
                    updateQuantityUI(foodHolder, newQty);
                    if (listener != null) {
                        listener.onQuantityChange(currentItem, newQty);
                    }
                }
            });
        }
    }

    private void updateQuantityUI(FoodViewHolder holder, int quantity) {
        if (quantity > 0) {
            holder.btnAddToCart.setVisibility(View.GONE);
            holder.quantityControlLayout.setVisibility(View.VISIBLE);
            holder.quantityTv.setText(String.valueOf(quantity));
        } else {
            holder.btnAddToCart.setVisibility(View.VISIBLE);
            holder.quantityControlLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView headerTitle;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerTitle = itemView.findViewById(R.id.headerTitle);
        }
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        public ImageView foodImageView;
        public TextView foodNameTv, foodPriceTv, foodCategoryTv, quantityTv, popularTag;
        public Button btnAddToCart;
        public LinearLayout quantityControlLayout;
        public ImageButton btnMinus, btnPlus;

        public FoodViewHolder(@NonNull View itemView) {
            super(itemView);
            foodImageView = itemView.findViewById(R.id.foodImageView);
            foodNameTv = itemView.findViewById(R.id.foodNameTv);
            foodPriceTv = itemView.findViewById(R.id.foodPriceTv);
            foodCategoryTv = itemView.findViewById(R.id.foodCategoryTv);
            popularTag = itemView.findViewById(R.id.popularTag);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            quantityControlLayout = itemView.findViewById(R.id.quantityControlLayout);
            quantityTv = itemView.findViewById(R.id.quantityTv);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
        }
    }
}
