package com.example.ltmb_nhom11.ui;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.ltmb_nhom11.R;

public class HistoryActivity extends AppCompatActivity {

    private ImageButton btnMenuHistory, btnNotificationsHistory;
    private MaterialCardView chipAll, chipUpcoming, chipDone;
    private FloatingActionButton fabAddAppointment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        btnMenuHistory = findViewById(R.id.btnMenuHistory);
        btnNotificationsHistory = findViewById(R.id.btnNotificationsHistory);

        chipAll = findViewById(R.id.chipAll);
        chipUpcoming = findViewById(R.id.chipUpcoming);
        chipDone = findViewById(R.id.chipDone);

        fabAddAppointment = findViewById(R.id.fabAddAppointment);

        // Sự kiện lọc
        chipAll.setOnClickListener(v -> handleFilterTabChange("Tất cả", chipAll));
        chipUpcoming.setOnClickListener(v -> handleFilterTabChange("Sắp tới", chipUpcoming));
        chipDone.setOnClickListener(v -> handleFilterTabChange("Đã xong/Đã hoàn thành", chipDone));

        // Mở rộng đặt lịch mới thông qua FAB
        fabAddAppointment.setOnClickListener(v -> {
            Toast.makeText(HistoryActivity.this, "Đang khởi tạo lịch khám mới...", Toast.LENGTH_SHORT).show();
            // Điều chuyển sang DoctorDetail hoặc khám bệnh
        });

        btnNotificationsHistory.setOnClickListener(v -> {
            Toast.makeText(this, "Không có thông báo mới", Toast.LENGTH_SHORT).show();
        });
    }

    private void handleFilterTabChange(String filterName, MaterialCardView selectedChip) {
        // Reset styles các tabs khác về màu mặc định (Trắng viền nhạt)
        resetAllFilterChips();

        // Highlight tab được chọn
        selectedChip.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryPrimary));
        // Đổi màu text của chip thông qua FindView, v.v.

        Toast.makeText(this, "Lọc danh sách: " + filterName, Toast.LENGTH_SHORT).show();
    }

    private void resetAllFilterChips() {
        int whiteColor = getResources().getColor(android.R.color.white);

        chipAll.setCardBackgroundColor(whiteColor);
        chipUpcoming.setCardBackgroundColor(whiteColor);
        chipDone.setCardBackgroundColor(whiteColor);
    }
}