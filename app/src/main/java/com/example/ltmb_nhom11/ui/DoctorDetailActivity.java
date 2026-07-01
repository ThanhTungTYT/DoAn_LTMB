package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.CalendarDate;
import com.example.ltmb_nhom11.ui.adapter.CalendarAdapter;
import com.example.ltmb_nhom11.util.SessionManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DoctorDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvCalendarDates;
    private Button btnTime0800, btnTime0900, btnTime1000;
    private Button btnTime1400, btnTime1500, btnTime1600;
    private MaterialButton btnBook;
    private String selectedDate = "";
    private String selectedTime = "";
    private String doctorName = "";
    private String doctorId = "";
    private Button selectedTimeButton;

    private final Map<String, DocumentSnapshot> scheduleCacheMap = new HashMap<>();
    private boolean isDataLoaded = false;
    private String pendingTargetDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail);

        initViews();
        setupTimeSlotInteractions();

        btnBack.setOnClickListener(v -> finish());

        String incomingDoctor = getIntent().getStringExtra("doctorName");
        if (incomingDoctor != null) {
            doctorName = incomingDoctor;
            TextView txtDoctorName = findViewById(R.id.txtDoctorName);
            if (txtDoctorName != null) txtDoctorName.setText(doctorName);
        }

        doctorId = getIntent().getStringExtra("doctorUid");
        if (doctorId == null) doctorId = "";

        loadDoctorInfo();
        loadPatientInfo();
        prefetchAllSchedules();
        setupCalendarRecyclerView();

        findViewById(R.id.btnChangePatient).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));

        btnBook.setOnClickListener(v -> {
            if (selectedTime.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn giờ khám hợp lệ!", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(DoctorDetailActivity.this, PaymentActivity.class);
            intent.putExtra("doctorId", doctorId);
            intent.putExtra("doctorName", doctorName);
            intent.putExtra("selected_date", selectedDate);
            intent.putExtra("selected_time", selectedTime);
            intent.putExtra("price", 460000L);
            startActivity(intent);
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvCalendarDates = findViewById(R.id.rvCalendarDates);

        btnTime0800 = findViewById(R.id.btnTime0800);
        btnTime0900 = findViewById(R.id.btnTime0900);
        btnTime1000 = findViewById(R.id.btnTime1000);
        btnTime1400 = findViewById(R.id.btnTime1400);
        btnTime1500 = findViewById(R.id.btnTime1500);
        btnTime1600 = findViewById(R.id.btnTime1600);

        btnBook = findViewById(R.id.btnBook);
    }

    private void setupCalendarRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvCalendarDates.setLayoutManager(layoutManager);

        List<CalendarDate> dateList = new ArrayList<>();
        String[] vnDays = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, 1);
        for (int i = 0; i < 14; i++) {
            int dow = cal.get(Calendar.DAY_OF_WEEK);
            String dayName = vnDays[dow - 1];
            String dayValue = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
            String fullDate = sdf.format(cal.getTime());
            boolean selectable = dow != Calendar.SUNDAY;
            dateList.add(new CalendarDate(dayName, dayValue, fullDate, selectable));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        for (CalendarDate d : dateList) {
            if (d.isSelectable()) {
                selectedDate = d.getDayName() + ", " + d.getFullDate();
                pendingTargetDate = d.getFullDate();
                break;
            }
        }

        CalendarAdapter adapter = new CalendarAdapter(dateList, date -> {
            selectedDate = date.getDayName() + ", " + date.getFullDate();
            displayScheduleFromCache(date.getFullDate());
        });
        rvCalendarDates.setAdapter(adapter);
    }


    private void prefetchAllSchedules() {
        if (doctorId.isEmpty()) return;

        lockAllTimeSlots();

        FirebaseFirestore.getInstance()
                .collection("users").document(doctorId)
                .collection("schedules")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scheduleCacheMap.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        scheduleCacheMap.put(doc.getId(), doc);
                    }
                    isDataLoaded = true;

                    if (!pendingTargetDate.isEmpty()) {
                        displayScheduleFromCache(pendingTargetDate);
                    }
                })
                .addOnFailureListener(e -> {
                    isDataLoaded = true;
                    Toast.makeText(this, "Không tải được lịch trình bác sĩ trước!", Toast.LENGTH_SHORT).show();
                    lockAllTimeSlots();
                });
    }

    private void displayScheduleFromCache(String ddmmyyyy) {
        if (!isDataLoaded) {
            pendingTargetDate = ddmmyyyy;
            return;
        }

        String dateId = convertToDateId(ddmmyyyy);
        DocumentSnapshot doc = scheduleCacheMap.get(dateId);

        if (doc != null && doc.exists()) {
            setSlotState(btnTime0800, checkAvail(doc, "slot_08_00"));
            setSlotState(btnTime0900, checkAvail(doc, "slot_09_00"));
            setSlotState(btnTime1000, checkAvail(doc, "slot_10_00"));
            setSlotState(btnTime1400, checkAvail(doc, "slot_14_00"));
            setSlotState(btnTime1500, checkAvail(doc, "slot_15_00"));
            setSlotState(btnTime1600, checkAvail(doc, "slot_16_00"));
        } else {
            lockAllTimeSlots();
        }

        autoSelectFirstAvailableSlot();
    }

    private boolean checkAvail(DocumentSnapshot doc, String key) {
        Boolean isAvail = doc.getBoolean(key);
        return isAvail != null && isAvail;
    }

    private void lockAllTimeSlots() {
        Button[] allButtons = {btnTime0800, btnTime0900, btnTime1000, btnTime1400, btnTime1500, btnTime1600};
        for (Button btn : allButtons) {
            setSlotState(btn, false);
        }
    }

    private void setSlotState(Button btn, boolean isAvailable) {
        btn.setEnabled(isAvailable);
        if (!isAvailable) {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FFF0F0")));
            btn.setTextColor(Color.parseColor("#BA1A1A"));
            if (btn instanceof MaterialButton) {
                ((MaterialButton) btn).setStrokeColor(ColorStateList.valueOf(Color.parseColor("#BA1A1A")));
            }
            btn.setAlpha(0.6f);
        } else {
            btn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#F1F5F9")));
            btn.setTextColor(Color.parseColor("#0B1C30"));
            if (btn instanceof MaterialButton) {
                ((MaterialButton) btn).setStrokeColor(ColorStateList.valueOf(Color.parseColor("#E2E8F0")));
            }
            btn.setAlpha(1.0f);
        }
    }

    private void autoSelectFirstAvailableSlot() {
        Button[] btns = {btnTime0800, btnTime0900, btnTime1000, btnTime1400, btnTime1500, btnTime1600};
        boolean found = false;

        for (Button btn : btns) {
            if (btn.isEnabled()) {
                styleTimeSlot(btn, true);
                selectedTimeButton = btn;
                selectedTime = btn.getText().toString();
                found = true;
                break;
            }
        }

        if (!found) {
            selectedTime = "";
            selectedTimeButton = null;
            btnBook.setEnabled(false);
            btnBook.setText("Kín lịch ngày này");
            btnBook.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#94A3B8")));
        } else {
            btnBook.setEnabled(true);
            btnBook.setText("Xác nhận đặt lịch");
            btnBook.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#00685F")));
        }
    }

    private String convertToDateId(String ddmmyyyy) {
        try {
            SimpleDateFormat inSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            SimpleDateFormat outSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return outSdf.format(inSdf.parse(ddmmyyyy));
        } catch (Exception e) {
            return "";
        }
    }

    private void setupTimeSlotInteractions() {
        View.OnClickListener timeSlotListener = view -> {
            Button clickedBtn = (Button) view;
            if (!clickedBtn.isEnabled()) return;

            if (selectedTimeButton != null && selectedTimeButton != clickedBtn) {
                styleTimeSlot(selectedTimeButton, false);
            }
            styleTimeSlot(clickedBtn, true);
            selectedTimeButton = clickedBtn;
            selectedTime = clickedBtn.getText().toString();
        };

        btnTime0800.setOnClickListener(timeSlotListener);
        btnTime0900.setOnClickListener(timeSlotListener);
        btnTime1000.setOnClickListener(timeSlotListener);
        btnTime1400.setOnClickListener(timeSlotListener);
        btnTime1500.setOnClickListener(timeSlotListener);
        btnTime1600.setOnClickListener(timeSlotListener);
    }

    private void styleTimeSlot(Button button, boolean selected) {
        int bgColor = selected ? Color.parseColor("#00685F") : Color.parseColor("#F1F5F9");
        int textColor = selected ? Color.WHITE : Color.parseColor("#0B1C30");
        int strokeColor = selected ? Color.parseColor("#00685F") : Color.parseColor("#E2E8F0");

        button.setBackgroundTintList(ColorStateList.valueOf(bgColor));
        button.setTextColor(textColor);
        if (button instanceof MaterialButton) {
            ((MaterialButton) button).setStrokeColor(ColorStateList.valueOf(strokeColor));
        }
    }

    private void loadDoctorInfo() {
        if (doctorId.isEmpty()) return;
        FirebaseFirestore.getInstance().collection("users").document(doctorId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String name = doc.getString("fullName");
                    if (name == null || name.isEmpty()) name = doc.getString("name");
                    String dept = doc.getString("dept");
                    String avatarUrl = doc.getString("avatarUrl");

                    TextView txtDoctorName = findViewById(R.id.txtDoctorName);
                    TextView txtSpecialty = findViewById(R.id.txtSpecialty);
                    android.widget.ImageView imgDoctor = findViewById(R.id.imgDoctor);

                    if (name != null && !name.isEmpty()) {
                        doctorName = name;
                        txtDoctorName.setText(name);
                    }
                    if (dept != null && !dept.isEmpty()) txtSpecialty.setText("Chuyên khoa " + dept);
                    if (avatarUrl != null && !avatarUrl.isEmpty()) ImageLoader.load(avatarUrl, imgDoctor);
                });
    }

    private void loadPatientInfo() {
        com.google.firebase.auth.FirebaseUser user = SessionManager.getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("fullName");
                    String phone = doc.getString("phone");
                    TextView txtName = findViewById(R.id.txtPatientName);
                    TextView txtInfo = findViewById(R.id.txtPatientInfo);
                    if (name != null && !name.isEmpty()) txtName.setText(name);
                    if (phone != null && !phone.isEmpty()) txtInfo.setText("Hồ sơ cá nhân • " + phone);
                });
    }
}