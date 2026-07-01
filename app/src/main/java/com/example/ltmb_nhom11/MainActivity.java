package com.example.ltmb_nhom11;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.ui.DirectionsActivity;
import com.example.ltmb_nhom11.ui.DoctorSearchActivity;
import com.example.ltmb_nhom11.ui.HistoryActivity;
import com.example.ltmb_nhom11.ui.LoginActivity;
import com.example.ltmb_nhom11.ui.PackageActivity;
import com.example.ltmb_nhom11.ui.ProfileActivity;
import com.example.ltmb_nhom11.ui.ImageLoader;
import com.example.ltmb_nhom11.ui.adapter.HomeCalendarAdapter;
import com.example.ltmb_nhom11.util.SessionManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity {

    private static final String MAP_PREVIEW_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuCBM3GfSf4XeLhBw3yRGTsG8YAbkkWM31cdIpkiqmIhUO2x-nkApumb6TSklwSrT8lu4QV0Z4pGauJlJ8djhQiFoHk24CSFM-wdcTKddy9S_RAvi8JXIvX9pecvNBhQMtdExv3uvcfllzlylyiWWGxDZsgH879x3APmAEPEPT2UjXdcgSACMSmvG9phLaAFiqXTBW3FDo1WfntQUr481xUc5BKlFYbv8X6y23OrmEzhVogKo-WXlJeIggBBg1Hm2IbzzRjQKMh_P1Q2";
    private Calendar currentMonth;
    private HomeCalendarAdapter calendarAdapter;
    private List<String> daysInMonth;
    private List<String> bookedDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        TextView tvUserName = findViewById(R.id.tvUserName);
        tvUserName.setText("Đang tải... 👋");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String fullName = documentSnapshot.getString("fullName");
                        if (fullName != null && !fullName.isEmpty()) {
                            String[] nameParts = fullName.trim().split(" ");
                            String shortName = nameParts[nameParts.length - 1];
                            tvUserName.setText(shortName + " 👋");
                        } else {
                            String email = currentUser.getEmail();
                            if (email != null && email.contains("@")) {
                                tvUserName.setText(email.substring(0, email.indexOf('@')) + " 👋");
                            } else {
                                tvUserName.setText("Bệnh nhân 👋");
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> tvUserName.setText("Bệnh nhân 👋"));

        // Lấy lịch sắp tới đổ lên thẻ trên cùng
        loadUpcomingAppointment(currentUser.getUid());

        ImageView imgMapPreview = findViewById(R.id.imgMapPreview);
        ImageLoader.load(MAP_PREVIEW_URL, imgMapPreview);

        // Nạp lưới lịch ô vuông
        setupDynamicCalendar();

        // CHUYỂN SANG TÌM BÁC SĨ (ĐẶT LỊCH)
        LinearLayout btnQuickBook = findViewById(R.id.btnQuickBook);
        btnQuickBook.setOnClickListener(v -> navigateToAppointments());

        LinearLayout btnQuickCheckup = findViewById(R.id.btnQuickCheckup);
        btnQuickCheckup.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Khám sức khỏe tổng quát đã sẵn sàng!", Toast.LENGTH_SHORT).show();
            navigateToAppointments();
        });

        // CHUYỂN SANG LỊCH SỬ KHÁM (HISTORY)
        TextView btnViewAllAppointments = findViewById(R.id.btnViewAllAppointments);
        btnViewAllAppointments.setOnClickListener(v -> navigateToAppointmentHistory());

        LinearLayout navAppointments = findViewById(R.id.navAppointments);
        navAppointments.setOnClickListener(v -> navigateToAppointmentHistory());

        // CHUYỂN SANG CHỈ ĐƯỜNG (MAP)
        LinearLayout cardLocation = findViewById(R.id.cardLocation);
        cardLocation.setOnClickListener(v -> navigateToDirections());

        TextView btnOpenDirections = findViewById(R.id.btnOpenDirections);
        btnOpenDirections.setOnClickListener(v -> navigateToDirections());

        // CHUYỂN SANG CÁC TRANG KHÁC TỪ BOTTOM NAV
        LinearLayout navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(v -> {});

        LinearLayout navPackages = findViewById(R.id.navPackages);
        navPackages.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PackageActivity.class);
            startActivity(intent);
        });

        LinearLayout navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ProfileActivity.class)));
    }

    // --- CÁC HÀM ĐIỀU HƯỚNG ---
    private void navigateToDirections() {
        Intent intent = new Intent(MainActivity.this, DirectionsActivity.class);
        startActivity(intent);
    }

    private void navigateToAppointmentHistory() {
        Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    private void navigateToAppointments() {
        Intent intent = new Intent(MainActivity.this, DoctorSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }

    // --- LOGIC LỊCH Ô VUÔNG ---
    private void setupDynamicCalendar() {
        currentMonth = Calendar.getInstance();

        RecyclerView recyclerCalendar = findViewById(R.id.recyclerHomeCalendar);
        recyclerCalendar.setLayoutManager(new GridLayoutManager(this, 7));

        daysInMonth = new ArrayList<>();
        bookedDays = new ArrayList<>();
        calendarAdapter = new HomeCalendarAdapter(daysInMonth, bookedDays);
        recyclerCalendar.setAdapter(calendarAdapter);

        findViewById(R.id.btnPrevMonth).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadCalendarData();
        });

        findViewById(R.id.btnNextMonth).setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadCalendarData();
        });

        loadCalendarData();
    }

    private void loadCalendarData() {
        TextView tvMonthYear = findViewById(R.id.tvMonthYear);
        int month = currentMonth.get(Calendar.MONTH) + 1;
        int year = currentMonth.get(Calendar.YEAR);
        tvMonthYear.setText(String.format("Lịch tháng %02d/%d", month, year));

        daysInMonth.clear();
        bookedDays.clear();

        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);

        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = firstDayOfWeek - Calendar.MONDAY;
        if (offset < 0) offset += 7;

        for (int i = 0; i < offset; i++) {
            daysInMonth.add("");
        }

        int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= maxDays; i++) {
            daysInMonth.add(String.valueOf(i));
        }

        int currentDisplayedMonth = month;
        int currentDisplayedYear = year;

        FirebaseUser currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("appointments")
                    .whereEqualTo("userId", currentUser.getUid())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String fullDateStr = document.getString("date");

                            if (fullDateStr != null && fullDateStr.contains(",")) {
                                try {
                                    String datePart = fullDateStr.split(",")[1].trim();
                                    String[] parts = datePart.split("/");

                                    if (parts.length == 3) {
                                        int appointmentDay = Integer.parseInt(parts[0]);
                                        int appointmentMonth = Integer.parseInt(parts[1]);
                                        int appointmentYear = Integer.parseInt(parts[2]);

                                        if (appointmentMonth == currentDisplayedMonth && appointmentYear == currentDisplayedYear) {
                                            bookedDays.add(String.valueOf(appointmentDay));
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("Calendar", e.getMessage());
                                }
                            }
                        }
                        calendarAdapter.notifyDataSetChanged();
                    });
        } else {
            calendarAdapter.notifyDataSetChanged();
        }
    }

    // --- LOGIC TÌM LỊCH GẦN NHẤT ---
    private void loadUpcomingAppointment(String userId) {
        FirebaseFirestore.getInstance().collection("appointments")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "upcoming")
                .get() // Bỏ limit() để lấy toàn bộ về lọc bằng Java
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LinearLayout cardAppointment = findViewById(R.id.cardAppointment);

                    if (!queryDocumentSnapshots.isEmpty()) {
                        QueryDocumentSnapshot closestDoc = null;
                        long minDiff = Long.MAX_VALUE;
                        long currentTime = System.currentTimeMillis();
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            String dateStr = doc.getString("date");
                            String timeStr = doc.getString("time");

                            if (dateStr != null && timeStr != null && dateStr.contains(",")) {
                                try {
                                    String datePart = dateStr.split(",")[1].trim();
                                    Date apptDate = sdf.parse(datePart + " " + timeStr);
                                    long apptTime = apptDate.getTime();

                                    if (apptTime >= currentTime) {
                                        long diff = apptTime - currentTime;
                                        if (diff < minDiff) {
                                            minDiff = diff;
                                            closestDoc = doc;
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.e("LoadUpcoming", "Lỗi parse thời gian: " + e.getMessage());
                                }
                            }
                        }

                        if (closestDoc != null) {
                            String dateStr = closestDoc.getString("date");
                            String timeStr = closestDoc.getString("time");
                            String doctorName = closestDoc.getString("doctorName");

                            String[] dateParts = dateStr.split(",");
                            String dayName = dateParts[0].trim();
                            String dayValue = dateParts[1].trim().split("/")[0];

                            TextView tvApptDayName = findViewById(R.id.tvApptDayName);
                            TextView tvApptDayValue = findViewById(R.id.tvApptDayValue);
                            TextView tvApptDoctorName = findViewById(R.id.tvApptDoctorName);
                            TextView tvApptDeptTime = findViewById(R.id.tvApptDeptTime);

                            tvApptDayName.setText(dayName);
                            tvApptDayValue.setText(dayValue);
                            tvApptDoctorName.setText(doctorName != null ? doctorName : "Bác sĩ");
                            tvApptDeptTime.setText("Khám bệnh • " + (timeStr != null ? timeStr : ""));

                            cardAppointment.setVisibility(View.VISIBLE);
                            cardAppointment.setOnClickListener(v ->
                                    Toast.makeText(MainActivity.this, "Chi tiết cuộc hẹn: " + doctorName + " lúc " + timeStr, Toast.LENGTH_LONG).show()
                            );
                        } else {
                            cardAppointment.setVisibility(View.GONE);
                        }
                    } else {
                        cardAppointment.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> findViewById(R.id.cardAppointment).setVisibility(View.GONE));
    }
}