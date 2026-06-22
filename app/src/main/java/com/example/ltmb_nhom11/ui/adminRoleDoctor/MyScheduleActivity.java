package com.example.ltmb_nhom11.ui.adminRoleDoctor;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.ltmb_nhom11.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MyScheduleActivity extends AppCompatActivity {

    private SwitchCompat switch1, switch2;
    private Button btnSave;
    private ImageButton btnNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_schedule);

        // Khởi tạo views
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        btnSave = findViewById(R.id.btnSave);
        btnNotification = findViewById(R.id.btnNotification);

        // Sự kiện click lưu thay đổi
        btnSave.setOnClickListener(v -> {
            boolean isSlot1Open = switch1.isChecked();
            boolean isSlot2Open = switch2.isChecked();

            String logMsg = "Đã lưu! Slot 08:00: " + (isSlot1Open ? "Mở" : "Khóa")
                    + ", Slot 09:00: " + (isSlot2Open ? "Mở" : "Khóa");

            Toast.makeText(MyScheduleActivity.this, logMsg, Toast.LENGTH_SHORT).show();
        });

        // Toggle Switch Listeners
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "Đã mở lịch lúc 08:30", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Đã khóa khung giờ 08:30", Toast.LENGTH_SHORT).show();
            }
        });

        // Thiết lập Bottom Navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_schedule);
        bottomNav.setOnItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.nav_schedule) {
                return true;
            }

            if (itemId == R.id.nav_logout) {
                return true;
            }

            return false;
        });
    }
}
