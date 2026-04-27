package com.example.nmimscanteenapp.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.nmimscanteenapp.R;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddMenuItemActivity extends AppCompatActivity {

    private TextInputEditText etItemName, etItemPrice, etItemImageName;
    private Spinner spinnerCategory;
    private SwitchMaterial switchAvailable;
    private Button btnAddItem;
    private FirebaseFirestore db;
    private String itemId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_menu_item);

        Toolbar toolbar = findViewById(R.id.addMenuToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();

        etItemName = findViewById(R.id.etItemName);
        etItemPrice = findViewById(R.id.etItemPrice);
        etItemImageName = findViewById(R.id.etItemImageName);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        switchAvailable = findViewById(R.id.switchAvailable);
        btnAddItem = findViewById(R.id.btnAddItem);

        String[] categories = {"Breakfast", "Lunch", "Snacks", "Beverages", "Main Course"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        // Check if we are editing
        if (getIntent().hasExtra("itemId")) {
            itemId = getIntent().getStringExtra("itemId");
            etItemName.setText(getIntent().getStringExtra("name"));
            etItemPrice.setText(String.valueOf(getIntent().getIntExtra("price", 0)));
            etItemImageName.setText(getIntent().getStringExtra("imageName"));
            switchAvailable.setChecked(getIntent().getBooleanExtra("available", true));
            
            String category = getIntent().getStringExtra("category");
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(category)) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
            btnAddItem.setText("Update Item");
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Menu Item");
            }
        }

        btnAddItem.setOnClickListener(v -> saveItem());
    }

    private void saveItem() {
        String name = etItemName.getText().toString().trim();
        String priceStr = etItemPrice.getText().toString().trim();
        String imageName = etItemImageName.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();
        boolean available = switchAvailable.isChecked();

        if (name.isEmpty() || priceStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int price = Integer.parseInt(priceStr);

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("name", name);
        itemData.put("price", price);
        itemData.put("category", category);
        itemData.put("imageName", imageName);
        itemData.put("available", available);

        if (itemId == null) {
            db.collection("menu").add(itemData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            db.collection("menu").document(itemId).set(itemData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Item updated successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
