package com.example.nmimscanteenapp.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nmimscanteenapp.R;
import com.example.nmimscanteenapp.model.FoodItem;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ManageMenuActivity extends AppCompatActivity {

    private RecyclerView manageMenuRv;
    private MenuManagementAdapter adapter;
    private List<FoodItem> menuList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_menu);

        Toolbar toolbar = findViewById(R.id.manageMenuToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        manageMenuRv = findViewById(R.id.manageMenuRv);
        manageMenuRv.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MenuManagementAdapter(menuList);
        manageMenuRv.setAdapter(adapter);

        findViewById(R.id.fabAddMenuItem).setOnClickListener(v -> {
            startActivity(new Intent(this, AddMenuItemActivity.class));
        });

        listenToMenuChanges();
    }

    private void listenToMenuChanges() {
        db.collection("menu")
                .orderBy("category", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading menu", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (value != null) {
                        menuList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            FoodItem item = doc.toObject(FoodItem.class);
                            if (item != null) {
                                item.setItemId(doc.getId());
                                menuList.add(item);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    class MenuManagementAdapter extends RecyclerView.Adapter<MenuManagementAdapter.ViewHolder> {
        private List<FoodItem> items;

        MenuManagementAdapter(List<FoodItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_manage_menu, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FoodItem item = items.get(position);
            holder.nameTv.setText(item.getName());
            holder.priceTv.setText("₹" + item.getPrice());
            holder.categoryTv.setText(item.getCategory());
            holder.availabilitySwitch.setChecked(item.isAvailable());

            // Load image using drawable name
            if (item.getImageName() != null && !item.getImageName().isEmpty()) {
                int resId = holder.itemView.getContext().getResources().getIdentifier(
                        item.getImageName(), "drawable", holder.itemView.getContext().getPackageName());
                if (resId != 0) {
                    holder.itemIv.setImageResource(resId);
                } else {
                    holder.itemIv.setImageResource(R.drawable.canteen);
                }
            } else {
                holder.itemIv.setImageResource(R.drawable.canteen);
            }

            holder.availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                db.collection("menu").document(item.getItemId()).update("available", isChecked);
            });

            holder.editBtn.setOnClickListener(v -> {
                Intent intent = new Intent(ManageMenuActivity.this, AddMenuItemActivity.class);
                intent.putExtra("itemId", item.getItemId());
                intent.putExtra("name", item.getName());
                intent.putExtra("price", item.getPrice());
                intent.putExtra("category", item.getCategory());
                intent.putExtra("imageName", item.getImageName());
                intent.putExtra("available", item.isAvailable());
                startActivity(intent);
            });

            holder.deleteBtn.setOnClickListener(v -> {
                new AlertDialog.Builder(ManageMenuActivity.this)
                        .setTitle("Delete Item")
                        .setMessage("Are you sure you want to delete " + item.getName() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            db.collection("menu").document(item.getItemId()).delete();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView itemIv, editBtn, deleteBtn;
            TextView nameTv, priceTv, categoryTv;
            SwitchMaterial availabilitySwitch;

            ViewHolder(View itemView) {
                super(itemView);
                itemIv = itemView.findViewById(R.id.menuItemIv);
                editBtn = itemView.findViewById(R.id.editBtn);
                deleteBtn = itemView.findViewById(R.id.deleteBtn);
                nameTv = itemView.findViewById(R.id.menuItemNameTv);
                priceTv = itemView.findViewById(R.id.menuItemPriceTv);
                categoryTv = itemView.findViewById(R.id.menuItemCategoryTv);
                availabilitySwitch = itemView.findViewById(R.id.availabilitySwitch);
            }
        }
    }
}
