package com.example.ltmb_nhom11.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
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
        // Trả tất cả chip về mặc định (nền trắng + viền xám + chữ tối)
        resetAllFilterChips();

        // Tô chip được chọn: nền xanh chủ đạo, bỏ viền, chữ trắng
        selectedChip.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryPrimary));
        selectedChip.setStrokeWidth(0);
        setChipTextColor(selectedChip, Color.WHITE);

        // TODO (PHA 5): lọc danh sách lịch hẹn theo filterName
    }

    private void resetAllFilterChips() {
        int white = getResources().getColor(android.R.color.white);
        int darkText = Color.parseColor("#3D4947");
        int strokeGray = Color.parseColor("#BCC9C6");
        int strokePx = Math.round(getResources().getDisplayMetrics().density); // 1dp

        for (MaterialCardView chip : new MaterialCardView[]{chipAll, chipUpcoming, chipDone}) {
            chip.setCardBackgroundColor(white);
            chip.setStrokeColor(strokeGray);
            chip.setStrokeWidth(strokePx);
            setChipTextColor(chip, darkText);
        }
    }

    /** Đổi màu chữ của TextView bên trong chip (con đầu tiên của MaterialCardView). */
    private void setChipTextColor(MaterialCardView chip, int color) {
        if (chip.getChildCount() > 0 && chip.getChildAt(0) instanceof TextView) {
            ((TextView) chip.getChildAt(0)).setTextColor(color);
        }
    }
}