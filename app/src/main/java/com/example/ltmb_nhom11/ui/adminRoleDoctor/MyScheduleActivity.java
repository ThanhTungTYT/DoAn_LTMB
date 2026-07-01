package com.example.ltmb_nhom11.ui.adminRoleDoctor;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Doctor;
import com.example.ltmb_nhom11.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MyScheduleActivity extends AppCompatActivity {

    private static final String TAG = "MyScheduleActivity";
    private static final TimeZone VN_TIMEZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");

    private final String[] timeSlots = {
            "08:00", "09:00", "10:00",
            "14:00", "15:00", "16:00"
    };

    private final String[] slotKeys = {
            "slot_08_00", "slot_09_00", "slot_10_00",
            "slot_14_00", "slot_15_00", "slot_16_00"
    };

    // ===== Views =====
    private LinearLayout containerSlots;
    private final Map<String, SwitchCompat> switchMap = new HashMap<>();
    private Button btnSave;
    private ImageButton btnNotification, btnPrevMonth, btnNextMonth;
    private TextView tvDoctorName, tvTitle, tvViewAll, tvScheduleDate;
    private ImageView imgAvatar;
    private CoordinatorLayout rootLayout;

    // ===== Upcoming Appointments =====
    private RecyclerView rvUpcomingAppointments;
    private TextView tvNoAppointments;
    private final List<AppointmentItem> appointmentList = new ArrayList<>();
    private UpcomingAdapter upcomingAdapter;

    // ===== Firebase =====
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String currentDoctorUid;

    private final List<Calendar> clickableDays = new ArrayList<>();
    private int selectedDayIndex = 0;

    // ==================== MODEL NỘI BỘ ====================

    static class AppointmentItem {
        String time, ampm, userId, type, status, appointmentId;
    }

    // ==================== ADAPTER NỘI BỘ ====================

    class UpcomingAdapter extends RecyclerView.Adapter<UpcomingAdapter.VH> {
        private final List<AppointmentItem> items;

        UpcomingAdapter(List<AppointmentItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_upcoming_appointment, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int position) {
            AppointmentItem item = items.get(position);

            h.tvTime.setText(item.time);
            h.tvAmPm.setText(item.ampm);
            h.tvType.setText(item.type != null ? capitalize(item.type) : "Khám");

            // Load tên bệnh nhân từ userId
            if (item.userId != null) {
                db.collection("users").document(item.userId).get()
                        .addOnSuccessListener(doc -> {
                            if (doc.exists()) {
                                String fullName = doc.getString("fullName");
                                String name = doc.getString("name");
                                String display = fullName != null ? fullName : name;
                                h.tvPatientName.setText(display != null ? display : "Bệnh nhân");
                            } else {
                                h.tvPatientName.setText("Bệnh nhân");
                            }
                        })
                        .addOnFailureListener(e -> h.tvPatientName.setText("Bệnh nhân"));
            } else {
                h.tvPatientName.setText("Bệnh nhân");
            }

            // Badge trạng thái
            h.tvStatus.setText(getStatusLabel(item.status));
            int[] colors = getStatusColors(item.status);
            GradientDrawable bg = new GradientDrawable();
            bg.setColor(colors[0]);
            bg.setCornerRadius(20f);
            h.tvStatus.setBackground(bg);
            h.tvStatus.setTextColor(colors[1]);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class VH extends RecyclerView.ViewHolder {
            TextView tvTime, tvAmPm, tvPatientName, tvType, tvStatus;

            VH(@NonNull View v) {
                super(v);
                tvTime        = v.findViewById(R.id.tvApptTime);
                tvAmPm        = v.findViewById(R.id.tvApptAmPm);
                tvPatientName = v.findViewById(R.id.tvApptPatientName);
                tvType        = v.findViewById(R.id.tvApptType);
                tvStatus      = v.findViewById(R.id.tvApptStatus);
            }
        }
    }

    // ==================== LIFECYCLE ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_schedule);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        currentDoctorUid = currentUser.getUid();

        initViews();
        setupUpcomingRecyclerView();

        generateClickableDays();
        updateScheduleDateLabel();
        generateSlotViews();

        loadDoctorProfile();
        loadUpcomingAppointments();
        loadScheduleSlots();

        btnSave.setOnClickListener(v -> saveScheduleSlots());

        btnPrevMonth.setOnClickListener(v -> {
            if (selectedDayIndex > 0) {
                selectedDayIndex--;
                updateScheduleDateLabel();
                generateSlotViews();   // Rebuild để áp dụng trạng thái CN
                loadScheduleSlots();
            } else {
                Toast.makeText(this, "Không thể xem lịch của quá khứ!", Toast.LENGTH_SHORT).show();
            }
        });

        btnNextMonth.setOnClickListener(v -> {
            if (selectedDayIndex < clickableDays.size() - 1) {
                selectedDayIndex++;
                updateScheduleDateLabel();
                generateSlotViews();   // Rebuild để áp dụng trạng thái CN
                loadScheduleSlots();
            } else {
                Toast.makeText(this, "Chỉ có thể cấu hình lịch trước 6 ngày!", Toast.LENGTH_SHORT).show();
            }
        });

        setupBottomNavigation();
    }

    // ==================== INIT ====================

    private void initViews() {
        rootLayout             = findViewById(R.id.rootLayout);
        containerSlots         = findViewById(R.id.containerSlots);
        btnSave                = findViewById(R.id.btnSave);
        btnNotification        = findViewById(R.id.btnNotification);
        btnPrevMonth           = findViewById(R.id.btnPrevMonth);
        btnNextMonth           = findViewById(R.id.btnNextMonth);
        tvDoctorName           = findViewById(R.id.tvDoctorName);
        tvTitle                = findViewById(R.id.tvTitle);
        tvViewAll              = findViewById(R.id.tvViewAll);
        tvScheduleDate         = findViewById(R.id.tvScheduleDate);
        imgAvatar              = findViewById(R.id.imgAvatar);
        rvUpcomingAppointments = findViewById(R.id.rvUpcomingAppointments);
        tvNoAppointments       = findViewById(R.id.tvNoAppointments);
    }

    private void setupUpcomingRecyclerView() {
        upcomingAdapter = new UpcomingAdapter(appointmentList);
        rvUpcomingAppointments.setLayoutManager(new LinearLayoutManager(this));
        rvUpcomingAppointments.setAdapter(upcomingAdapter);
    }

    // ==================== KIỂM TRA CHỦ NHẬT ====================

    private boolean isSelectedDaySunday() {
        Calendar selectedCal = clickableDays.get(selectedDayIndex);
        return selectedCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
    }

    // ==================== UPCOMING APPOINTMENTS ====================

    private void loadUpcomingAppointments() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(VN_TIMEZONE);
        String todayStr = sdf.format(Calendar.getInstance(VN_TIMEZONE).getTime());

        SimpleDateFormat timeFmt = new SimpleDateFormat("HH:mm", Locale.getDefault());
        timeFmt.setTimeZone(VN_TIMEZONE);
        String currentTime = timeFmt.format(Calendar.getInstance(VN_TIMEZONE).getTime());

        db.collection("appointments")
                .whereEqualTo("doctorId", currentDoctorUid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    appointmentList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String date   = doc.getString("date");
                        String time   = doc.getString("time");
                        String status = doc.getString("status");

                        // Lọc: đúng ngày hôm nay
                        if (date == null || !date.contains(todayStr)) continue;

                        // Lọc: sau giờ hiện tại
                        if (time == null || time.compareTo(currentTime) <= 0) continue;

                        // Lọc: bỏ cancelled và completed
                        if ("cancelled".equalsIgnoreCase(status)
                                || "completed".equalsIgnoreCase(status)) continue;

                        AppointmentItem item = new AppointmentItem();
                        item.appointmentId = doc.getId();
                        item.time   = time;
                        item.userId = doc.getString("userId");
                        item.type   = doc.getString("type");
                        item.status = status;

                        try {
                            int hour = Integer.parseInt(time.split(":")[0]);
                            item.ampm = hour >= 12 ? "PM" : "AM";
                        } catch (Exception e) {
                            item.ampm = "AM";
                        }

                        appointmentList.add(item);
                    }

                    appointmentList.sort((a, b) -> a.time.compareTo(b.time));
                    upcomingAdapter.notifyDataSetChanged();

                    boolean isEmpty = appointmentList.isEmpty();
                    tvNoAppointments.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                    rvUpcomingAppointments.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không tải được lịch hẹn.", Toast.LENGTH_SHORT).show());
    }

    // ==================== SLOT VIEWS ====================

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

        boolean isSunday = isSelectedDaySunday();

        for (int i = 0; i < timeSlots.length; i++) {
            String timeLabel = timeSlots[i];
            String slotKey   = slotKeys[i];

            View slotView = getLayoutInflater().inflate(R.layout.item_slot, containerSlots, false);

            TextView tvTime   = slotView.findViewById(R.id.tvTime);
            TextView tvStatus = slotView.findViewById(R.id.tvStatus);
            TextView tvDesc   = slotView.findViewById(R.id.tvDesc);
            SwitchCompat sw   = slotView.findViewById(R.id.switchSlot);

            tvTime.setText(timeLabel);

            if (isSunday) {
                // Chủ nhật: luôn tắt, không cho bật
                sw.setChecked(false);
                sw.setEnabled(false);
                sw.setAlpha(0.4f);
                tvStatus.setText("Đã khóa");
                tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                tvDesc.setText("Nghỉ CN");
                tvDesc.setTextColor(getResources().getColor(android.R.color.darker_gray));
            } else {
                sw.setEnabled(true);
                sw.setAlpha(1.0f);
                updateSlotUI(sw.isChecked(), tvStatus, tvDesc);
                sw.setOnCheckedChangeListener((btn, isChecked) ->
                        updateSlotUI(isChecked, tvStatus, tvDesc));
            }

            switchMap.put(slotKey, sw);
            containerSlots.addView(slotView);
        }
    }

    // ==================== CALENDAR ====================

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

        boolean isSunday = selectedCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY;
        String sundayNote = isSunday ? " 🔴 Nghỉ" : "";

        tvScheduleDate.setText("— " + dayName + ", "
                + sdf.format(selectedCal.getTime()) + suffix + sundayNote);

        if (btnSave != null) {
            if (isSunday) {
                btnSave.setEnabled(false);
                btnSave.setText("Không làm việc ngày Chủ Nhật");
                btnSave.setAlpha(0.5f);
            } else {
                btnSave.setEnabled(true);
                btnSave.setAlpha(1.0f);
                btnSave.setText("Lưu thay đổi ngày " + sdf.format(selectedCal.getTime()));
            }
        }
    }

    // ==================== FIREBASE ====================

    private void loadDoctorProfile() {
        db.collection("users").document(currentDoctorUid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Doctor doctor = documentSnapshot.toObject(Doctor.class);
                        if (doctor != null) {
                            String displayName = doctor.getFullName() != null
                                    ? doctor.getFullName() : doctor.getName();

                            // Null check trước khi dùng
                            if (tvDoctorName != null) {
                                tvDoctorName.setText(displayName != null ? displayName : "Bác sĩ");
                            }
                            if (tvTitle != null) {
                                String name = displayName != null ? displayName : "Bác sĩ";
                                tvTitle.setText("Lịch trình của " + name);
                            }
                            if (imgAvatar != null
                                    && doctor.getAvatarUrl() != null
                                    && !doctor.getAvatarUrl().isEmpty()) {
                                Glide.with(MyScheduleActivity.this)
                                        .load(doctor.getAvatarUrl())
                                        .circleCrop()
                                        .into(imgAvatar);
                            }
                        }
                    }
                });
    }

    private void loadScheduleSlots() {
        // Chủ nhật: không load, giữ trạng thái đã khóa
        if (isSelectedDaySunday()) return;

        String dateId = getTargetDateId();
        db.collection("users").document(currentDoctorUid)
                .collection("schedules").document(dateId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        for (String key : slotKeys) {
                            boolean isAvailable = parseBooleanValue(documentSnapshot.get(key));
                            SwitchCompat sw = switchMap.get(key);
                            if (sw != null) {
                                sw.setChecked(isAvailable);
                                View parentView = (View) sw.getParent();
                                updateSlotUI(isAvailable,
                                        parentView.findViewById(R.id.tvStatus),
                                        parentView.findViewById(R.id.tvDesc));
                            }
                        }
                    } else {
                        for (SwitchCompat sw : switchMap.values()) {
                            sw.setChecked(false);
                            View parentView = (View) sw.getParent();
                            updateSlotUI(false,
                                    parentView.findViewById(R.id.tvStatus),
                                    parentView.findViewById(R.id.tvDesc));
                        }
                    }
                })
                .addOnFailureListener(e ->
                        showRetrySnackbar("Không tải được lịch khám.", this::loadScheduleSlots));
    }

    private void saveScheduleSlots() {
        // Chặn lưu ngày Chủ Nhật
        if (isSelectedDaySunday()) {
            Toast.makeText(this, "Không thể lưu lịch ngày Chủ Nhật!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSave.setEnabled(false);
        String dateId = getTargetDateId();
        Map<String, Object> slotsData = new HashMap<>();
        for (String key : slotKeys) {
            slotsData.put(key, switchMap.get(key).isChecked());
        }
        db.collection("users").document(currentDoctorUid)
                .collection("schedules").document(dateId)
                .set(slotsData)
                .addOnSuccessListener(aVoid -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Lưu thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    btnSave.setEnabled(true);
                    showRetrySnackbar("Lưu thất bại.", this::saveScheduleSlots);
                });
    }

    // ==================== HELPERS ====================

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

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private String getStatusLabel(String status) {
        if (status == null) return "SẮP TỚI";
        switch (status.toLowerCase()) {
            case "checked-in":
            case "checkedin": return "ĐÃ CHECK-IN";
            case "waiting":   return "CHỜ KHÁM";
            default:          return "SẮP TỚI";
        }
    }

    private int[] getStatusColors(String status) {
        if (status == null) status = "upcoming";
        switch (status.toLowerCase()) {
            case "checked-in":
            case "checkedin":
                return new int[]{Color.parseColor("#D1F2D9"), Color.parseColor("#1B5E20")};
            case "waiting":
                return new int[]{Color.parseColor("#FFF1C2"), Color.parseColor("#8A6D00")};
            default:
                return new int[]{Color.parseColor("#D1E4FF"), Color.parseColor("#1565C0")};
        }
    }

    // ==================== BOTTOM NAVIGATION ====================

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_schedule);
        bottomNav.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_schedule) return true;
            if (item.getItemId() == R.id.nav_logout) {
                mAuth.signOut();
                startActivity(new Intent(this, LoginActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                finish();
                return true;
            }
            return false;
        });
    }
}