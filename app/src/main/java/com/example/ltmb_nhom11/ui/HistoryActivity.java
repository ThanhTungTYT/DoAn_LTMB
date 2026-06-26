package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.ltmb_nhom11.MainActivity;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.util.SessionManager;
import com.example.ltmb_nhom11.model.Appointment;
import com.example.ltmb_nhom11.repository.AppointmentRepository;
import com.example.ltmb_nhom11.ui.adapter.AppointmentAdapter;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ImageButton btnMenuHistory, btnNotificationsHistory;
    private MaterialCardView chipAll, chipUpcoming, chipDone;
    private FloatingActionButton fabAddAppointment;
    private RecyclerView rvAppointments;
    private TextView tvEmpty;

    private AppointmentAdapter adapter;
    private final List<Appointment> allAppointments = new ArrayList<>();
    private String currentFilter = "Tất cả";

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
        rvAppointments = findViewById(R.id.rvAppointments);
        tvEmpty = findViewById(R.id.tvEmpty);

        // RecyclerView + adapter
        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter();
        rvAppointments.setAdapter(adapter);

        // Sự kiện lọc
        chipAll.setOnClickListener(v -> handleFilterTabChange("Tất cả", chipAll));
        chipUpcoming.setOnClickListener(v -> handleFilterTabChange("Sắp tới", chipUpcoming));
        chipDone.setOnClickListener(v -> handleFilterTabChange("Đã xong", chipDone));

        // FAB -> mở màn tìm bác sĩ để đặt lịch mới
        fabAddAppointment.setOnClickListener(v ->
                startActivity(new Intent(HistoryActivity.this, DoctorSearchActivity.class)));

        // Nút back (sau này màn này vào từ Profile)
        btnMenuHistory.setOnClickListener(v -> finish());

        btnNotificationsHistory.setOnClickListener(v ->
                Toast.makeText(this, "Không có thông báo mới", Toast.LENGTH_SHORT).show());

        setupBottomNav();
    }

    /** Thanh điều hướng dưới — đồng bộ hành vi với Trang chủ. */
    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navAppointments = findViewById(R.id.navAppointments);
        LinearLayout navPackages = findViewById(R.id.navPackages);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        navAppointments.setOnClickListener(v -> { /* đang ở màn lịch hẹn */ });
        navPackages.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng Gói khám đang được xây dựng!", Toast.LENGTH_SHORT).show());
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadAppointments(); // tải lại mỗi khi quay về để thấy lịch mới đặt
    }

    private void loadAppointments() {
        String userId = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser().getUid() : "test_user";
        new AppointmentRepository().getByUser(userId, new AppointmentRepository.OnList() {
            @Override
            public void onLoaded(List<Appointment> list) {
                allAppointments.clear();
                allAppointments.addAll(list);
                applyFilter();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HistoryActivity.this, "Lỗi tải lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Lọc danh sách theo chip đang chọn rồi đổ vào RecyclerView. */
    private void applyFilter() {
        List<Appointment> filtered = new ArrayList<>();
        for (Appointment a : allAppointments) {
            String status = a.getStatus() != null ? a.getStatus() : "";
            switch (currentFilter) {
                case "Sắp tới":
                    if ("upcoming".equals(status)) filtered.add(a);
                    break;
                case "Đã xong":
                    if ("done".equals(status)) filtered.add(a);
                    break;
                default: // "Tất cả"
                    filtered.add(a);
            }
        }
        adapter.setData(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void handleFilterTabChange(String filterName, MaterialCardView selectedChip) {
        resetAllFilterChips();
        selectedChip.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryPrimary));
        selectedChip.setStrokeWidth(0);
        setChipTextColor(selectedChip, Color.WHITE);

        currentFilter = filterName;
        applyFilter();
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

    private void setChipTextColor(MaterialCardView chip, int color) {
        if (chip.getChildCount() > 0 && chip.getChildAt(0) instanceof TextView) {
            ((TextView) chip.getChildAt(0)).setTextColor(color);
        }
    }
}
