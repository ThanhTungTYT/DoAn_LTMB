package com.example.ltmb_nhom11.ui.adminRoleDoctor;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.PatientSchedule;
import com.example.ltmb_nhom11.model.ScheduleAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdminOverviewActivity extends AppCompatActivity {

    private ImageButton btnMenu, btnNotification;
    private RecyclerView rvSchedule;
    private TextView tvEmptySchedule;
    private ScheduleAdapter scheduleAdapter;
    private final List<PatientSchedule> scheduleList = new ArrayList<>();

    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNavigation;

    private TextView tvAppointmentsValue, tvPatientsValue, tvRevenueValue, tvCapacityPercent;
    private ProgressBar progressCapacity;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_overview);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupToolbar();
        setupScheduleRecyclerView();
        loadTodaySchedule();
        loadStats();
        setupActions();
        setupBottomNavigation();
    }

    private void initViews() {
        btnMenu = findViewById(R.id.btnMenu);
        btnNotification = findViewById(R.id.btnNotification);

        rvSchedule = findViewById(R.id.rvSchedule);
        tvEmptySchedule = findViewById(R.id.tvEmptySchedule);

        fabAdd = findViewById(R.id.fabAdd);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        tvAppointmentsValue = findViewById(R.id.tvAppointmentsValue);
        tvPatientsValue = findViewById(R.id.tvPatientsValue);
        tvRevenueValue = findViewById(R.id.tvRevenueValue);
        tvCapacityPercent = findViewById(R.id.tvCapacityPercent);
        progressCapacity = findViewById(R.id.progressCapacity);
    }

    private void setupToolbar() {
        btnMenu.setOnClickListener(v ->
                Toast.makeText(this, "Đang mở Sidebar Menu", Toast.LENGTH_SHORT).show());
        btnNotification.setOnClickListener(v ->
                Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show());
    }

    private void setupScheduleRecyclerView() {
        scheduleAdapter = new ScheduleAdapter(scheduleList, (item, position) ->
                Toast.makeText(this, "Chi tiết lịch hẹn: " + item.getName(), Toast.LENGTH_SHORT).show());
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvSchedule.setAdapter(scheduleAdapter);
    }

    /**
     * Trả về chuỗi date đúng định dạng đang lưu trong Firestore: "T5, 02/07/2026"
     */
    private String getTodayDateString() {
        Calendar cal = Calendar.getInstance();
        String[] dayAbbr = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        String day = dayAbbr[cal.get(Calendar.DAY_OF_WEEK) - 1];
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        return day + ", " + sdf.format(cal.getTime());
    }

    private void loadTodaySchedule() {
        String todayDate = getTodayDateString();

        db.collection("appointments")
                .whereEqualTo("date", todayDate)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    scheduleList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String time = doc.getString("time");
                        String doctorName = doc.getString("doctorName");
                        String type = doc.getString("type");
                        String status = doc.getString("status");

                        if (time == null) time = "--:--";
                        String ampm = "AM";
                        String[] parts = time.split(":");
                        if (parts.length > 0) {
                            try {
                                int hour = Integer.parseInt(parts[0]);
                                ampm = hour >= 12 ? "PM" : "AM";
                            } catch (NumberFormatException ignored) {}
                        }

                        String name = (doctorName != null && !doctorName.isEmpty())
                                ? doctorName : "Bệnh nhân";
                        String detail = (type != null ? capitalize(type) : "Khám") + " • " + time;

                        int[] colors = getStatusColors(status);

                        scheduleList.add(new PatientSchedule(
                                id, time, ampm, name, detail,
                                getStatusLabel(status),
                                colors[0], colors[1]));
                    }

                    scheduleList.sort((a, b) -> a.getTime().compareTo(b.getTime()));

                    scheduleAdapter.notifyDataSetChanged();
                    tvAppointmentsValue.setText(String.valueOf(scheduleList.size()));

                    boolean isEmpty = scheduleList.isEmpty();
                    tvEmptySchedule.setVisibility(isEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
                    rvSchedule.setVisibility(isEmpty ? android.view.View.GONE : android.view.View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không tải được lịch trình hôm nay.", Toast.LENGTH_SHORT).show());
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String getStatusLabel(String status) {
        if (status == null) return "SẮP TỚI";
        switch (status.toLowerCase()) {
            case "checked-in":
            case "checkedin":
                return "ĐÃ CHECK-IN";
            case "completed":
                return "HOÀN THÀNH";
            case "waiting":
                return "CHỜ KHÁM";
            case "cancelled":
                return "ĐÃ HỦY";
            case "upcoming":
            default:
                return "SẮP TỚI";
        }
    }

    private int[] getStatusColors(String status) {
        if (status == null) status = "upcoming";
        switch (status.toLowerCase()) {
            case "checked-in":
            case "checkedin":
                return new int[]{Color.parseColor("#D1F2D9"), Color.parseColor("#1B5E20")};
            case "completed":
                return new int[]{Color.parseColor("#E5E7EB"), Color.parseColor("#374151")};
            case "waiting":
                return new int[]{Color.parseColor("#FFF1C2"), Color.parseColor("#8A6D00")};
            case "cancelled":
                return new int[]{Color.parseColor("#FBD5D5"), Color.parseColor("#9B1C1C")};
            case "upcoming":
            default:
                return new int[]{Color.parseColor("#E0E7FF"), Color.parseColor("#3730A3")};
        }
    }

    private void loadStats() {
        // TODO: New Patients / Doanh thu / Clinic Capacity -> nối collection tương ứng khi cần
        tvPatientsValue.setText("128");
        tvRevenueValue.setText("$3,840");

        int capacity = 84;
        tvCapacityPercent.setText(capacity + "%");
        progressCapacity.setProgress(capacity);
    }

    private void setupActions() {
        fabAdd.setOnClickListener(v ->
                Toast.makeText(this, "Thêm cuộc hẹn mới", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                return true;
            } else if (id == R.id.navAppointments) {
                Toast.makeText(this, "Chuyển tới Appointments", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.navPackages) {
                Toast.makeText(this, "Chuyển tới Packages", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Chuyển tới Profile", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
    }
}