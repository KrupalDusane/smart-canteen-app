package com.example.nmimscanteenapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.model.FoodItem;

import java.util.List;

public class SliderAdapter extends RecyclerView.Adapter<SliderAdapter.SliderViewHolder> {

    private List<FoodItem> sliderItems;

    public SliderAdapter(List<FoodItem> sliderItems) {
        this.sliderItems = sliderItems;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SliderViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_slider, parent, false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        FoodItem item = sliderItems.get(position);
        holder.imageView.setImageResource(item.getImageResId());
        holder.titleView.setText(item.getName());
        holder.priceView.setText("₹" + item.getPrice());
    }

    @Override
    public int getItemCount() {
        return sliderItems.size();
    }

    class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView, priceView;

        SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.sliderImage);
            titleView = itemView.findViewById(R.id.sliderTitle);
            priceView = itemView.findViewById(R.id.sliderPrice);
        }
    }
}
