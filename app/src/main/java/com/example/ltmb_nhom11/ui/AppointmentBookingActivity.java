package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.MainActivity;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.CalendarDate;
import com.example.ltmb_nhom11.model.TimeSlot;
import com.example.ltmb_nhom11.ui.adapter.CalendarAdapter;
import com.example.ltmb_nhom11.ui.adapter.TimeSlotAdapter;
import com.example.ltmb_nhom11.util.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AppointmentBookingActivity extends AppCompatActivity {

    private TextView tvMainPackageName;
    private TextView tvPackagePrice;

    private RecyclerView rvCalendarHorizontal;
    private RecyclerView rvTimeSlotsGrid;

    private String selectedDate = "";
    private String selectedTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_booking);

        tvMainPackageName = findViewById(R.id.tvMainPackageName);
        tvPackagePrice = findViewById(R.id.tvPackagePrice);

        rvCalendarHorizontal = findViewById(R.id.rvCalendarHorizontal);
        rvTimeSlotsGrid = findViewById(R.id.rvTimeSlotsGrid);
        Button btnConfirmBooking = findViewById(R.id.btnConfirmBooking);

        String packageName = getIntent().getStringExtra("packageName");
        double price = getIntent().getDoubleExtra("price", 0);

        if (packageName != null) {
            tvMainPackageName.setText(packageName);
        }

        NumberFormat formatter =
                NumberFormat.getInstance(new Locale("vi", "VN"));

        tvPackagePrice.setText(
                formatter.format((long) price) + " VNĐ"
        );

        List<CalendarDate> dateList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat dayFormat =
                new SimpleDateFormat("dd", Locale.getDefault());

        SimpleDateFormat fullDateFormat =
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        String[] dayNames = {
                "CN","T2","T3","T4","T5","T6","T7"
        };

        for(int i = 0 ; i < 14 ; i++){

            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

            String dayName = dayNames[dayOfWeek-1];

            String dayValue =
                    dayFormat.format(calendar.getTime());

            String fullDate =
                    fullDateFormat.format(calendar.getTime());

            boolean selectable =
                    dayOfWeek != Calendar.SUNDAY;

            dateList.add(
                    new CalendarDate(
                            dayName,
                            dayValue,
                            fullDate,
                            selectable
                    )
            );

            calendar.add(Calendar.DATE,1);
        }

        for(CalendarDate date : dateList){

            if(date.isSelectable()){

                selectedDate = date.getFullDate();
                break;

            }

        }

        rvCalendarHorizontal.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        CalendarAdapter calendarAdapter =
                new CalendarAdapter(
                        dateList,
                        date -> selectedDate = date.getFullDate()
                );

        rvCalendarHorizontal.setAdapter(calendarAdapter);

        List<TimeSlot> timeList = new ArrayList<>();

        timeList.add(new TimeSlot("08:00"));
        timeList.add(new TimeSlot("09:00"));
        timeList.add(new TimeSlot("10:00"));
        timeList.add(new TimeSlot("14:00"));
        timeList.add(new TimeSlot("15:00"));
        timeList.add(new TimeSlot("16:00"));

        selectedTime = "08:00";

        rvTimeSlotsGrid.setLayoutManager(
                new GridLayoutManager(this,3)
        );

        TimeSlotAdapter adapter =
                new TimeSlotAdapter(
                        timeList,
                        slot -> selectedTime = slot.getTime()
                );

        rvTimeSlotsGrid.setAdapter(adapter);

        loadPatientInfo();

        findViewById(R.id.tvChangePatient).setOnClickListener(v -> {
            Intent intent = new Intent(this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

        Toolbar toolbar = findViewById(R.id.toolbarBooking);
        setSupportActionBar(toolbar);

        toolbar.setNavigationOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
        btnConfirmBooking.setOnClickListener(v -> {

            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                Toast.makeText(this,
                        "Vui lòng chọn ngày và giờ khám",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            com.google.firebase.auth.FirebaseUser user = SessionManager.getCurrentUser();

            if (user == null) {
                Toast.makeText(this,
                        "Vui lòng đăng nhập",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            java.util.Map<String, Object> data = new java.util.HashMap<>();

            data.put("userId", user.getUid());
            data.put("packageName", packageName);
            data.put("price", price);
            data.put("date", selectedDate);
            data.put("time", selectedTime);
            data.put("createdAt",
                    com.google.firebase.Timestamp.now());
            data.put("status", "pending");

            FirebaseFirestore.getInstance()
                    .collection("appointments")
                    .add(data)
                    .addOnSuccessListener(documentReference -> {

                        Intent intent = new Intent(
                                AppointmentBookingActivity.this,
                                MedicalSummaryActivity.class);

                        intent.putExtra("packageName", packageName);
                        intent.putExtra("price", price);
                        intent.putExtra("date", selectedDate);
                        intent.putExtra("time", selectedTime);
                        intent.putExtra("medicalId", documentReference.getId());

                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this,
                                "Lưu lịch hẹn thất bại: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    });
        });
    }
    private void loadPatientInfo() {

        com.google.firebase.auth.FirebaseUser user = SessionManager.getCurrentUser();

        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {

                    String name = doc.getString("fullName");
                    String phone = doc.getString("phone");

                    TextView txtName = findViewById(R.id.tvPatientName);
                    TextView txtInfo = findViewById(R.id.tvPatientSubInfo);

                    if (name != null && !name.isEmpty()) {
                        txtName.setText(name);
                    }

                    if (phone != null && !phone.isEmpty()) {
                        txtInfo.setText(phone);
                    }
                });
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadPatientInfo();
    }
}