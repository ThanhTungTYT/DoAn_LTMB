package com.example.ltmb_nhom11.ui.adminRoleDoctor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
import com.example.ltmb_nhom11.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdminOverviewActivity extends AppCompatActivity {

    private static final String TAG = "AdminOverview";
    private static final TimeZone VN_TIMEZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
    private ImageButton btnMenu;
    private RecyclerView rvSchedule;
    private TextView tvEmptySchedule;
    private ScheduleAdapter scheduleAdapter;
    private final List<PatientSchedule> scheduleList = new ArrayList<>();
    private FloatingActionButton fabAdd;
    private BottomNavigationView bottomNavigation;
    private TextView tvAppointmentsValue, tvPatientsValue, tvRevenueValue;
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
        loadNewPatientsToday();
        loadMonthlyRevenue();
        setupActions();
        setupBottomNavigation();
    }

    private void initViews() {
        btnMenu = findViewById(R.id.btnMenu);

        rvSchedule = findViewById(R.id.rvSchedule);
        tvEmptySchedule = findViewById(R.id.tvEmptySchedule);

        fabAdd = findViewById(R.id.fabAdd);
        bottomNavigation = findViewById(R.id.bottom_navigation);

        tvAppointmentsValue = findViewById(R.id.tvAppointmentsValue);
        tvPatientsValue = findViewById(R.id.tvPatientsValue);
        tvRevenueValue = findViewById(R.id.tvRevenueValue);
    }

    private void setupToolbar() {
        btnMenu.setOnClickListener(v ->
                Toast.makeText(this, "Đang mở Sidebar Menu", Toast.LENGTH_SHORT).show());
    }

    private void setupScheduleRecyclerView() {
        scheduleAdapter = new ScheduleAdapter(scheduleList, (item, position) ->
                Toast.makeText(this, "Chi tiết: " + item.getName(), Toast.LENGTH_SHORT).show());
        rvSchedule.setLayoutManager(new LinearLayoutManager(this));
        rvSchedule.setAdapter(scheduleAdapter);
    }

    private String getTodayDateString() {
        Calendar cal = Calendar.getInstance(VN_TIMEZONE);

        String[] dayAbbr = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        String day = dayAbbr[cal.get(Calendar.DAY_OF_WEEK) - 1];

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(VN_TIMEZONE);

        String dateStr = day + ", " + sdf.format(cal.getTime());
        Log.d(TAG, "Today date string: " + dateStr);
        return dateStr;
    }

    private void loadTodaySchedule() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(VN_TIMEZONE);
        String todayDDMMYYYY = sdf.format(Calendar.getInstance(VN_TIMEZONE).getTime());

        Log.d(TAG, "Querying appointments with date containing: " + todayDDMMYYYY);
        Toast.makeText(this, "Tìm lịch ngày: " + todayDDMMYYYY, Toast.LENGTH_LONG).show();

        db.collection("appointments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    scheduleList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String date = doc.getString("date"); // "T5, 02/07/2026"

                        if (date == null || !date.contains(todayDDMMYYYY)) continue;

                        String id = doc.getId();
                        String time = doc.getString("time");
                        String doctorName = doc.getString("doctorName");
                        String type = doc.getString("type");
                        String status = doc.getString("status");

                        if (time == null) time = "--:--";
                        String ampm = "AM";
                        try {
                            int hour = Integer.parseInt(time.split(":")[0]);
                            ampm = hour >= 12 ? "PM" : "AM";
                        } catch (Exception ignored) {}

                        String name = (doctorName != null && !doctorName.isEmpty())
                                ? doctorName : "Bệnh nhân";
                        String detail = (type != null ? capitalize(type) : "Khám") + " • " + time;
                        int[] colors = getStatusColors(status);

                        scheduleList.add(new PatientSchedule(
                                id, time, ampm, name, detail,
                                getStatusLabel(status), colors[0], colors[1]));
                    }

                    scheduleList.sort((a, b) -> a.getTime().compareTo(b.getTime()));
                    scheduleAdapter.notifyDataSetChanged();

                    tvAppointmentsValue.setText(String.valueOf(scheduleList.size()));

                    boolean isEmpty = scheduleList.isEmpty();
                    tvEmptySchedule.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    rvSchedule.setVisibility(isEmpty ? View.GONE : View.VISIBLE);

                    Log.d(TAG, "Appointments found today: " + scheduleList.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadTodaySchedule error: " + e.getMessage());
                    Toast.makeText(this, "Không tải được lịch trình.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadNewPatientsToday() {
        Calendar startOfDay = Calendar.getInstance(VN_TIMEZONE);
        startOfDay.set(Calendar.HOUR_OF_DAY, 0);
        startOfDay.set(Calendar.MINUTE, 0);
        startOfDay.set(Calendar.SECOND, 0);
        startOfDay.set(Calendar.MILLISECOND, 0);

        Calendar endOfDay = Calendar.getInstance(VN_TIMEZONE);
        endOfDay.set(Calendar.HOUR_OF_DAY, 23);
        endOfDay.set(Calendar.MINUTE, 59);
        endOfDay.set(Calendar.SECOND, 59);
        endOfDay.set(Calendar.MILLISECOND, 999);

        long startMs = startOfDay.getTimeInMillis();
        long endMs = endOfDay.getTimeInMillis();

        Log.d(TAG, "New patients range: " + startMs + " → " + endMs);

        db.collection("users")
                .whereEqualTo("role", "user")
                .whereGreaterThanOrEqualTo("createdAt", startMs)
                .whereLessThanOrEqualTo("createdAt", endMs)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    Log.d(TAG, "New patients today: " + count);
                    tvPatientsValue.setText(String.valueOf(count));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadNewPatientsToday error: " + e.getMessage());
                    tvPatientsValue.setText("--");
                    Toast.makeText(this, "Không tải được số bệnh nhân mới.", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMonthlyRevenue() {
        Calendar cal = Calendar.getInstance(VN_TIMEZONE);

        SimpleDateFormat monthYearFmt = new SimpleDateFormat("MM/yyyy", Locale.getDefault());
        monthYearFmt.setTimeZone(VN_TIMEZONE);
        String currentMonthYear = monthYearFmt.format(cal.getTime());

        Log.d(TAG, "Current month/year: " + currentMonthYear);

        db.collection("appointments")
                .whereEqualTo("status", "upcoming")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    long totalRevenue = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String date = doc.getString("date"); // "T5, 02/07/2026"

                        if (date != null && date.contains(currentMonthYear)) {
                            Object priceObj = doc.get("price");
                            if (priceObj instanceof Number) {
                                totalRevenue += ((Number) priceObj).longValue();
                            }
                        }
                    }

                    Log.d(TAG, "Monthly revenue: " + totalRevenue);
                    tvRevenueValue.setText(formatCurrency(totalRevenue));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadMonthlyRevenue error: " + e.getMessage());
                    tvRevenueValue.setText("--");
                    Toast.makeText(this, "Không tải được doanh thu.", Toast.LENGTH_SHORT).show();
                });
    }

    private String formatCurrency(long amount) {
        NumberFormat nf = NumberFormat.getInstance(Locale.getDefault());
        return nf.format(amount) + " VNĐ";
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String getStatusLabel(String status) {
        if (status == null) return "SẮP TỚI";
        switch (status.toLowerCase()) {
            case "checked-in":
            case "checkedin":  return "ĐÃ CHECK-IN";
            case "completed":  return "HOÀN THÀNH";
            case "waiting":    return "CHỜ KHÁM";
            case "cancelled":  return "ĐÃ HỦY";
            default:           return "SẮP TỚI";
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
            default:
                return new int[]{Color.parseColor("#E0E7FF"), Color.parseColor("#3730A3")};
        }
    }

    private void setupActions() {
        fabAdd.setOnClickListener(v ->
                Toast.makeText(this, "Thêm cuộc hẹn mới", Toast.LENGTH_SHORT).show());
    }

    private void setupBottomNavigation() {
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                return true;
            }
            else if (id == R.id.nav_doctors) {
                Intent intent = new Intent(this, DoctorScheduleActivity.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}