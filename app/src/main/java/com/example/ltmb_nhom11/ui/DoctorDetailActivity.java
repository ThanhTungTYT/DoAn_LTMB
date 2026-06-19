package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.CalendarDate;
import com.example.ltmb_nhom11.ui.adapter.CalendarAdapter;

import java.util.ArrayList;
import java.util.List;

public class DoctorDetailActivity extends AppCompatActivity {

    private ImageButton btnBack, btnNotifications;
    private RecyclerView rvCalendarDates;
    private Button btnTime0800, btnTime0830, btnTime0900;
    private Button btnTime1400, btnTime1500, btnTime1630;
    private MaterialButton btnBook;

    private String selectedDate = "15";
    private String selectedTime = "08:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_detail);

        // Khởi tạo views
        initViews();

        // Cập nhật cấu hình lịch chọn ngày cuộn ngang
        setupCalendarRecyclerView();

        // Đăng ký tương tác sự kiện click các Slot Giờ & Đặt lịch
        setupTimeSlotInteractions();

        // Xử lý sự kiện nút back & đặt lịch chính thức
        btnBack.setOnClickListener(v -> finish());

        btnBook.setOnClickListener(v -> {
            // Chuyển hướng sang màn hình Thanh Toán (PaymentActivity)
            Intent intent = new Intent(DoctorDetailActivity.this, PaymentActivity.class);
            intent.putExtra("selected_date", "Thứ Hai, 15/10/2023");
            intent.putExtra("selected_time", selectedTime);
            startActivity(intent);
        });
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        btnNotifications = findViewById(R.id.btnNotifications);
        rvCalendarDates = findViewById(R.id.rvCalendarDates);

        // Buttons Khung giờ
        btnTime0800 = findViewById(R.id.btnTime0800);
        btnTime0830 = findViewById(R.id.btnTime0830);
        btnTime0900 = findViewById(R.id.btnTime0900);
        btnTime1400 = findViewById(R.id.btnTime1400);
        btnTime1500 = findViewById(R.id.btnTime1500);
        btnTime1630 = findViewById(R.id.btnTime1630);

        btnBook = findViewById(R.id.btnBook);
    }

    private void setupCalendarRecyclerView() {
        // Thiết kế Layout Manager dạng chiều ngang cuộn (Horizontal)
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rvCalendarDates.setLayoutManager(layoutManager);

        // Mock dates tự dựng
        List<CalendarDate> dateList = new ArrayList<>();
        dateList.add(new CalendarDate("Th2", "15", true));
        dateList.add(new CalendarDate("Th3", "16", false));
        dateList.add(new CalendarDate("Th4", "17", false));
        dateList.add(new CalendarDate("Th5", "18", false));
        dateList.add(new CalendarDate("Th6", "19", false));

        CalendarAdapter adapter = new CalendarAdapter(dateList, new CalendarAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(CalendarDate date) {
                selectedDate = date.getDayValue();
                Toast.makeText(DoctorDetailActivity.this, "Đã chọn ngày: " + date.getDayName() + " " + date.getDayValue(), Toast.LENGTH_SHORT).show();
            }
        });
        rvCalendarDates.setAdapter(adapter);
    }

    private void setupTimeSlotInteractions() {
        View.OnClickListener timeSlotListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Đặt lại màu sắc của tất cả buttons giờ về mặc định (Xám/Slate)
                resetAllTimeSlots();
                Button clickedBtn = (Button) view;
                selectedTime = clickedBtn.getText().toString();

                // Đổi nút bấm đang được chọn thành màu xanh chủ đạo
                clickedBtn.setTextColor(getResources().getColor(android.R.color.white));
                clickedBtn.setBackgroundColor(getResources().getColor(R.color.colorPrimaryPrimary));
                // Trong Android thực tế: có thể dùng drawable selector hoặc set stroke màu sắc phù hợp
            }
        };

        btnTime0800.setOnClickListener(timeSlotListener);
        btnTime0830.setOnClickListener(timeSlotListener);
        btnTime0900.setOnClickListener(timeSlotListener);
        btnTime1400.setOnClickListener(timeSlotListener);
        btnTime1500.setOnClickListener(timeSlotListener);
        btnTime1630.setOnClickListener(timeSlotListener);
    }

    private void resetAllTimeSlots() {
        int defaultTextColor = getResources().getColor(android.R.color.black);
        int defaultBgColor = getResources().getColor(android.R.color.transparent); // or customize

        // Reset state
        btnTime0800.setTextColor(defaultTextColor);
        btnTime0830.setTextColor(defaultTextColor);
        btnTime0900.setTextColor(defaultTextColor);
        btnTime1400.setTextColor(defaultTextColor);
        btnTime1500.setTextColor(defaultTextColor);
        btnTime1630.setTextColor(defaultTextColor);
    }
}