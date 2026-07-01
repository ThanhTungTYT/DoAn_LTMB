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
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DoctorDetailActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView rvCalendarDates;
    private Button btnTime0800, btnTime0830, btnTime0900;
    private Button btnTime1400, btnTime1500, btnTime1630;
    private MaterialButton btnBook;

    private String selectedDate = "";
    private String selectedTime = "08:00";
    private String doctorName = "BS. Nguyễn Văn An";
    private String doctorId = "";
    private Button selectedTimeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail);

        initViews();
        setupCalendarRecyclerView();
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

        btnBook.setOnClickListener(v -> {
            Intent intent = new Intent(DoctorDetailActivity.this, PaymentActivity.class);
            intent.putExtra("doctorId", doctorId);
            intent.putExtra("doctorName", doctorName);
            intent.putExtra("selected_date", selectedDate);
            intent.putExtra("selected_time", selectedTime);
            intent.putExtra("price", 460000L);
            startActivity(intent);
        });

        loadPatientInfo();
        findViewById(R.id.btnChangePatient).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
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
                    if (dept != null && !dept.isEmpty()) {
                        txtSpecialty.setText("Chuyên khoa " + dept);
                    }
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        ImageLoader.load(avatarUrl, imgDoctor);
                    }
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

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        rvCalendarDates = findViewById(R.id.rvCalendarDates);

        btnTime0800 = findViewById(R.id.btnTime0800);
        btnTime0830 = findViewById(R.id.btnTime0830);
        btnTime0900 = findViewById(R.id.btnTime0900);
        btnTime1400 = findViewById(R.id.btnTime1400);
        btnTime1500 = findViewById(R.id.btnTime1500);
        btnTime1630 = findViewById(R.id.btnTime1630);

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
                break;
            }
        }

        CalendarAdapter adapter = new CalendarAdapter(dateList,
                date -> selectedDate = date.getDayName() + ", " + date.getFullDate());
        rvCalendarDates.setAdapter(adapter);
    }

    private void setupTimeSlotInteractions() {
        View.OnClickListener timeSlotListener = view -> {
            Button clickedBtn = (Button) view;
            if (selectedTimeButton != null && selectedTimeButton != clickedBtn) {
                styleTimeSlot(selectedTimeButton, false);
            }
            styleTimeSlot(clickedBtn, true);
            selectedTimeButton = clickedBtn;
            selectedTime = clickedBtn.getText().toString();
        };

        btnTime0800.setOnClickListener(timeSlotListener);
        btnTime0830.setOnClickListener(timeSlotListener);
        btnTime0900.setOnClickListener(timeSlotListener);
        btnTime1400.setOnClickListener(timeSlotListener);
        btnTime1500.setOnClickListener(timeSlotListener);
        btnTime1630.setOnClickListener(timeSlotListener);

        styleTimeSlot(btnTime0800, true);
        selectedTimeButton = btnTime0800;
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
}