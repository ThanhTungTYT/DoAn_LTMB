package com.example.ltmb_nhom11.ui.adminRoleDoctor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Doctor;
import com.example.ltmb_nhom11.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MyScheduleActivity extends AppCompatActivity {

    private static final String TAG = "MyScheduleActivity";

    // Danh sách 17 khung giờ
    private final String[] timeSlots = {
            "08:00", "08:30", "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30", "17:00"
    };

    // Danh sách Key lưu trên Firestore tương ứng
    private final String[] slotKeys = {
            "slot_08_00", "slot_08_30", "slot_09_00", "slot_09_30", "slot_10_00", "slot_10_30", "slot_11_00", "slot_11_30",
            "slot_13_00", "slot_13_30", "slot_14_00", "slot_14_30", "slot_15_00", "slot_15_30", "slot_16_00", "slot_16_30", "slot_17_00"
    };

    private LinearLayout containerSlots;
    private final Map<String, SwitchCompat> switchMap = new HashMap<>();

    private Button btnSave;
    private ImageButton btnNotification, btnPrevMonth, btnNextMonth;
    private TextView tvDoctorName, tvTitle, tvViewAll, tvScheduleDate;
    private ImageView imgAvatar;
    private CoordinatorLayout rootLayout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentDoctorUid;

    private final List<Calendar> clickableDays = new ArrayList<>();
    private int selectedDayIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_schedule);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentDoctorUid = currentUser.getUid();

        rootLayout = findViewById(R.id.rootLayout);
        containerSlots = findViewById(R.id.containerSlots);
        btnSave = findViewById(R.id.btnSave);
        btnNotification = findViewById(R.id.btnNotification);
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvTitle = findViewById(R.id.tvTitle);
        tvViewAll = findViewById(R.id.tvViewAll);
        tvScheduleDate = findViewById(R.id.tvScheduleDate);
        imgAvatar = findViewById(R.id.imgAvatar);

        generateClickableDays();
        updateScheduleDateLabel();
        generateSlotViews();

        loadDoctorProfile();
        loadScheduleSlots();

        btnSave.setOnClickListener(v -> saveScheduleSlots());

        btnPrevMonth.setOnClickListener(v -> {
            if (selectedDayIndex > 0) {
                selectedDayIndex--;
                updateScheduleDateLabel();
                loadScheduleSlots();
            } else {
                Toast.makeText(this, "Không thể xem lịch của quá khứ!", Toast.LENGTH_SHORT).show();
            }
        });

        btnNextMonth.setOnClickListener(v -> {
            if (selectedDayIndex < clickableDays.size() - 1) {
                selectedDayIndex++;
                updateScheduleDateLabel();
                loadScheduleSlots();
            } else {
                Toast.makeText(this, "Chỉ có thể cấu hình lịch trước 6 ngày!", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    // HÀM HELPER ĐỂ ĐỒNG BỘ CHỮ VÀ MÀU SẮC (Dùng chung để tránh lặp code)
    private void updateSlotUI(boolean isChecked, TextView tvStatus, TextView tvDesc) {
        if (isChecked) {
            tvStatus.setText("Mở lịch");
            tvStatus.setTextColor(getResources().getColor(android.R.color.black));
            tvDesc.setText("Sẵn sàng");
            tvDesc.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        } else {
            tvStatus.setText("Đã khóa");
            tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
            tvDesc.setText("Đang bận");
            tvDesc.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        }
    }

    private void generateSlotViews() {
        containerSlots.removeAllViews();
        switchMap.clear();

        for (int i = 0; i < timeSlots.length; i++) {
            String timeLabel = timeSlots[i];
            String slotKey = slotKeys[i];

            View slotView = getLayoutInflater().inflate(R.layout.item_slot, containerSlots, false);

            TextView tvTime = slotView.findViewById(R.id.tvTime);
            TextView tvStatus = slotView.findViewById(R.id.tvStatus);
            TextView tvDesc = slotView.findViewById(R.id.tvDesc);
            SwitchCompat switchSlot = slotView.findViewById(R.id.switchSlot);

            tvTime.setText(timeLabel);

            // GỌI NGAY HÀM NÀY ĐỂ ÉP ĐỔI CHỮ MẶC ĐỊNH TRONG XML VỀ ĐÚNG TRẠNG THÁI HIỆN TẠI
            updateSlotUI(switchSlot.isChecked(), tvStatus, tvDesc);

            switchSlot.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateSlotUI(isChecked, tvStatus, tvDesc);
            });

            switchMap.put(slotKey, switchSlot);
            containerSlots.addView(slotView);
        }
    }

    private void generateClickableDays() {
        clickableDays.clear();
        for (int i = 0; i < 7; i++) {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.DAY_OF_YEAR, i);
            clickableDays.add(cal);
        }
    }

    private String getTargetDateId() {
        Calendar selectedCal = clickableDays.get(selectedDayIndex);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(selectedCal.getTime());
    }

    private void updateScheduleDateLabel() {
        if (tvScheduleDate == null || clickableDays.isEmpty()) return;
        Calendar selectedCal = clickableDays.get(selectedDayIndex);
        String[] dayNames = {"Chủ Nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"};
        String dayName = dayNames[selectedCal.get(Calendar.DAY_OF_WEEK) - 1];
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        String suffix = (selectedDayIndex == 0) ? " (Hôm nay)" : "";
        tvScheduleDate.setText("— " + dayName + ", " + sdf.format(selectedCal.getTime()) + suffix);
        if (btnSave != null) btnSave.setText("Lưu thay đổi ngày " + sdf.format(selectedCal.getTime()));
    }

    private void loadDoctorProfile() {
        db.collection("users").document(currentDoctorUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Doctor doctor = documentSnapshot.toObject(Doctor.class);
                        if (doctor != null) {
                            String displayName = doctor.getFullName() != null ? doctor.getFullName() : doctor.getName();
                            tvDoctorName.setText(displayName != null ? displayName : "Bác sĩ");
                            tvTitle.setText("Lịch trình của " + tvDoctorName.getText());
                            if (doctor.getAvatarUrl() != null && !doctor.getAvatarUrl().isEmpty()) {
                                Glide.with(MyScheduleActivity.this)
                                        .load(doctor.getAvatarUrl()).circleCrop().into(imgAvatar);
                            }
                        }
                    }
                });
    }

    private void loadScheduleSlots() {
        String dateId = getTargetDateId();
        db.collection("users").document(currentDoctorUid).collection("schedules").document(dateId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        for (String key : slotKeys) {
                            boolean isAvailable = parseBooleanValue(documentSnapshot.get(key));
                            SwitchCompat sw = switchMap.get(key);
                            if (sw != null) {
                                sw.setChecked(isAvailable);
                                // ÉP ĐỒNG BỘ LẠI UI ĐỂ TRÁNH LỖI LỆCH TEXT
                                View parentView = (View) sw.getParent();
                                TextView tvStatus = parentView.findViewById(R.id.tvStatus);
                                TextView tvDesc = parentView.findViewById(R.id.tvDesc);
                                updateSlotUI(isAvailable, tvStatus, tvDesc);
                            }
                        }
                    } else {
                        // Ngày mới hoàn toàn -> Tắt hết toàn bộ
                        for (SwitchCompat sw : switchMap.values()) {
                            sw.setChecked(false);
                            View parentView = (View) sw.getParent();
                            TextView tvStatus = parentView.findViewById(R.id.tvStatus);
                            TextView tvDesc = parentView.findViewById(R.id.tvDesc);
                            updateSlotUI(false, tvStatus, tvDesc);
                        }
                    }
                })
                .addOnFailureListener(e -> showRetrySnackbar("Không tải được lịch khám.", this::loadScheduleSlots));
    }

    private void saveScheduleSlots() {
        btnSave.setEnabled(false);
        String dateId = getTargetDateId();
        Map<String, Object> slotsData = new HashMap<>();

        for (String key : slotKeys) {
            slotsData.put(key, switchMap.get(key).isChecked());
        }

        db.collection("users").document(currentDoctorUid).collection("schedules").document(dateId)
                .set(slotsData)
                .addOnSuccessListener(aVoid -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(MyScheduleActivity.this, "Lưu thành công 17 ca khám!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    showRetrySnackbar("Lưu thất bại. Vui lòng thử lại.", this::saveScheduleSlots);
                });
    }

    private boolean parseBooleanValue(Object value) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) return "true".equalsIgnoreCase((String) value);
        return false;
    }

    private void showRetrySnackbar(String message, Runnable retryAction) {
        if (rootLayout != null) {
            Snackbar.make(rootLayout, message, Snackbar.LENGTH_INDEFINITE)
                    .setAction("Thử lại", v -> retryAction.run()).show();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_schedule);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_schedule) return true;
            if (item.getItemId() == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(MyScheduleActivity.this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                return true;
            }
            return false;
        });
    }
}