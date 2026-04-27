package com.example.nmimscanteenapp.student;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nmimscanteenapp.R;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MessMenuActivity extends AppCompatActivity {

    private TextView breakfastItemsTv, lunchItemsTv, dinnerItemsTv;
    private ImageView breakfastIv, lunchIv, dinnerIv;
    private Button btnMon, btnTue, btnWed, btnThu, btnFri, btnSat, btnSun;
    private Map<String, DayMenu> weeklyMenu;
    private Button selectedButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mess_menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mess Menu");
        }

        // Initialize UI components
        breakfastItemsTv = findViewById(R.id.breakfastItemsTv);
        lunchItemsTv = findViewById(R.id.lunchItemsTv);
        dinnerItemsTv = findViewById(R.id.dinnerItemsTv);
        
        breakfastIv = findViewById(R.id.breakfastIv);
        lunchIv = findViewById(R.id.lunchIv);
        dinnerIv = findViewById(R.id.dinnerIv);

        btnMon = findViewById(R.id.btnMon);
        btnTue = findViewById(R.id.btnTue);
        btnWed = findViewById(R.id.btnWed);
        btnThu = findViewById(R.id.btnThu);
        btnFri = findViewById(R.id.btnFri);
        btnSat = findViewById(R.id.btnSat);
        btnSun = findViewById(R.id.btnSun);

        initMenuData();
        setupDaySelectors();

        // Default: Select current day
        selectCurrentDay();
    }

    private void initMenuData() {
        weeklyMenu = new HashMap<>();

        weeklyMenu.put("Mon", new DayMenu("Poha, Tea, Milk", "Veg Thali, Roti, Dal, Rice", "Paneer Masala, Roti, Dessert"));
        weeklyMenu.put("Tue", new DayMenu("Tea, Coffee", "Aloo Matar, Paratha, Curd", "Dal Tadka, Jeera Rice, Gulab Jamun"));
        weeklyMenu.put("Wed", new DayMenu("Idli Sambar, Coconut Chutney", "Mix Veg, Roti, Salad", "Veg Pulao, Raita, Manchurian"));
        weeklyMenu.put("Thu", new DayMenu("Aloo Paratha, Curd", "Chole Bhature, Lassi", "Matar Paneer, Butter Roti, Ice Cream"));
        weeklyMenu.put("Fri", new DayMenu("Puri Bhaji, Tea", "Rajma Chawal, Papads", "Veg Biryani, Salan, Kheer"));
        weeklyMenu.put("Sat", new DayMenu("Bread Butter, Jam, Milk", "Pav Bhaji, Salad", "Chinese Fried Rice, Noodles"));
        weeklyMenu.put("Sun", new DayMenu("Special Misal Pav, Tea", "Special Maharashtrian Thali", "South Indian Feast (Dosa/Uttapam)"));
    }

    private void setupDaySelectors() {
        btnMon.setOnClickListener(v -> updateMenu("Mon", btnMon));
        btnTue.setOnClickListener(v -> updateMenu("Tue", btnTue));
        btnWed.setOnClickListener(v -> updateMenu("Wed", btnWed));
        btnThu.setOnClickListener(v -> updateMenu("Thu", btnThu));
        btnFri.setOnClickListener(v -> updateMenu("Fri", btnFri));
        btnSat.setOnClickListener(v -> updateMenu("Sat", btnSat));
        btnSun.setOnClickListener(v -> updateMenu("Sun", btnSun));
    }

    private void updateMenu(String day, Button clickedButton) {
        // Highlight selected day
        if (selectedButton != null) {
            selectedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#F5F5F5")));
            selectedButton.setTextColor(Color.parseColor("#757575"));
        }
        clickedButton.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#C62828")));
        clickedButton.setTextColor(Color.WHITE);
        selectedButton = clickedButton;

        // Update Text
        DayMenu menu = weeklyMenu.get(day);
        if (menu != null) {
            breakfastItemsTv.setText(menu.breakfast);
            lunchItemsTv.setText(menu.lunch);
            dinnerItemsTv.setText(menu.dinner);
        }
    }

    private void selectCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.MONDAY: updateMenu("Mon", btnMon); break;
            case Calendar.TUESDAY: updateMenu("Tue", btnTue); break;
            case Calendar.WEDNESDAY: updateMenu("Wed", btnWed); break;
            case Calendar.THURSDAY: updateMenu("Thu", btnThu); break;
            case Calendar.FRIDAY: updateMenu("Fri", btnFri); break;
            case Calendar.SATURDAY: updateMenu("Sat", btnSat); break;
            case Calendar.SUNDAY: updateMenu("Sun", btnSun); break;
            default: updateMenu("Mon", btnMon);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static class DayMenu {
        String breakfast, lunch, dinner;
        DayMenu(String b, String l, String d) {
            this.breakfast = b;
            this.lunch = l;
            this.dinner = d;
        }
    }
}
