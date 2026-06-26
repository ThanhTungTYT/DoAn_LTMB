package com.example.ltmb_nhom11.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.ltmb_nhom11.model.TimeSlot;
import com.example.ltmb_nhom11.ui.adapter.TimeSlotAdapter;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.CalendarDate;
import com.example.ltmb_nhom11.ui.adapter.CalendarAdapter;

import java.util.ArrayList;
import java.util.List;

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

        // Ánh xạ View
        tvMainPackageName = findViewById(R.id.tvMainPackageName);
        tvPackagePrice = findViewById(R.id.tvPackagePrice);
        rvCalendarHorizontal = findViewById(R.id.rvCalendarHorizontal);
        rvTimeSlotsGrid = findViewById(R.id.rvTimeSlotsGrid);

        // Nhận dữ liệu từ PackageActivity
        String packageName = getIntent().getStringExtra("packageName");
        double price = getIntent().getDoubleExtra("price", 0);

        if (packageName != null) {
            tvMainPackageName.setText(packageName);
        }

        tvPackagePrice.setText((long) price + " VNĐ");

        // Danh sách ngày khám
        List<CalendarDate> dateList = new ArrayList<>();

        dateList.add(new CalendarDate("T2", "23", "23/06/2026", true));
        dateList.add(new CalendarDate("T3", "24", "24/06/2026", true));
        dateList.add(new CalendarDate("T4", "25", "25/06/2026", true));
        dateList.add(new CalendarDate("T5", "26", "26/06/2026", true));
        dateList.add(new CalendarDate("T6", "27", "27/06/2026", true));
        dateList.add(new CalendarDate("T7", "28", "28/06/2026", true));
        dateList.add(new CalendarDate("CN", "29", "29/06/2026", false));

        rvCalendarHorizontal.setLayoutManager(
                new LinearLayoutManager(
                        this,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        );

        CalendarAdapter adapter = new CalendarAdapter(
                dateList,
                date -> selectedDate = date.getFullDate()
        );

        rvCalendarHorizontal.setAdapter(adapter);
        List<TimeSlot> timeList = new ArrayList<>();

        timeList.add(new TimeSlot("08:00"));
        timeList.add(new TimeSlot("08:30"));
        timeList.add(new TimeSlot("09:00"));
        timeList.add(new TimeSlot("09:30"));
        timeList.add(new TimeSlot("10:00"));
        timeList.add(new TimeSlot("10:30"));
        timeList.add(new TimeSlot("14:00"));
        timeList.add(new TimeSlot("14:30"));
        timeList.add(new TimeSlot("15:00"));

        rvTimeSlotsGrid.setLayoutManager(new GridLayoutManager(this, 3));

        TimeSlotAdapter timeAdapter = new TimeSlotAdapter(
                timeList,
                slot -> selectedTime = slot.getTime()
        );

        rvTimeSlotsGrid.setAdapter(timeAdapter);
    }
}