package com.example.ltmb_nhom11.ui;

import com.example.ltmb_nhom11.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ltmb_nhom11.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class MedicalSummaryActivity extends AppCompatActivity {

    private TextView tvPackageName, tvDateTime, tvMedicalId;
    private TextView tvDoctorName, tvDoctorMajor;
    private TextView tvBloodPressure, tvTemperature, tvWeight;
    private TextView tvDiagnosis, tvMedicine, tvAdvice;
    private TextView tvFile1, tvFile2;
    private ImageView imgDoctor;

    private LinearLayout navHome, navAppointments, navPackages, navProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_summary);

        initViews();
        setupToolbar();
        loadDataFromIntent();
        setupBottomNav();
    }

    private void initViews() {
        tvPackageName = findViewById(R.id.tvPackageName);
        tvDateTime = findViewById(R.id.tvDateTime);
        tvMedicalId = findViewById(R.id.tvMedicalId);

        tvDoctorName = findViewById(R.id.tvDoctorName);
        tvDoctorMajor = findViewById(R.id.tvDoctorMajor);
        imgDoctor = findViewById(R.id.imgDoctor);

        tvBloodPressure = findViewById(R.id.tvBloodPressure);
        tvTemperature = findViewById(R.id.tvTemperature);
        tvWeight = findViewById(R.id.tvWeight);

        tvDiagnosis = findViewById(R.id.tvDiagnosis);
        tvMedicine = findViewById(R.id.tvMedicine);
        tvAdvice = findViewById(R.id.tvAdvice);

        tvFile1 = findViewById(R.id.tvFile1);
        tvFile2 = findViewById(R.id.tvFile2);

        navHome = findViewById(R.id.navHome);
        navAppointments = findViewById(R.id.navAppointments);
        navPackages = findViewById(R.id.navPackages);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbarSummary);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadDataFromIntent() {
        // 1. Nhận ID lịch hẹn truyền từ trang Lịch sử sang
        String appointmentId = getIntent().getStringExtra("appointmentId");

        if (appointmentId == null || appointmentId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã hồ sơ khám!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvMedicalId.setText("Mã hồ sơ: #" + appointmentId);

        // 2. Gọi Firestore đổ dữ liệu động thực tế của ca khám
        FirebaseFirestore.getInstance()
                .collection("appointments")
                .document(appointmentId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {

                        String type = documentSnapshot.getString("type");
                        String date = documentSnapshot.getString("date");
                        String time = documentSnapshot.getString("time");

                        if ("package".equals(type)) {
                            tvPackageName.setText(documentSnapshot.getString("packageName"));
                        } else {
                            tvPackageName.setText("Khám với Bác sĩ: " + documentSnapshot.getString("doctorName"));
                        }
                        tvDateTime.setText(time + " - " + date);

                        // Đọc thông tin kết quả do bác sĩ cập nhật
                        String doctorName = documentSnapshot.getString("resultDoctorName");
                        String doctorMajor = documentSnapshot.getString("resultDoctorMajor");
                        String bloodPressure = documentSnapshot.getString("bloodPressure");
                        String temperature = documentSnapshot.getString("temperature");
                        String weight = documentSnapshot.getString("weight");
                        String diagnosis = documentSnapshot.getString("diagnosis");
                        String medicine = documentSnapshot.getString("medicine");
                        String advice = documentSnapshot.getString("advice");

                        // Xử lý logic dự phòng nếu thông tin bác sĩ phụ trách kết quả bị null
                        if (doctorName == null || doctorName.isEmpty()) {
                            doctorName = "doctor".equals(type) ? documentSnapshot.getString("doctorName") : "Bác sĩ phụ trách";
                        }
                        if (doctorMajor == null || doctorMajor.isEmpty()) {
                            doctorMajor = "Phòng khám tổng quát";
                        }

                        // Hiển thị dữ liệu lên giao diện
                        tvDoctorName.setText(doctorName);
                        tvDoctorMajor.setText(doctorMajor);
                        tvBloodPressure.setText("Huyết áp: " + (bloodPressure != null ? bloodPressure : "--- mmHg"));
                        tvTemperature.setText("Nhiệt độ: " + (temperature != null ? temperature : "--- °C"));
                        tvWeight.setText("Cân nặng: " + (weight != null ? weight : "--- kg"));

                        tvDiagnosis.setText(diagnosis != null ? diagnosis : "Chưa có chẩn đoán từ bác sĩ.");
                        tvMedicine.setText(medicine != null ? medicine : "Chưa có đơn thuốc kê khai.");
                        tvAdvice.setText(advice != null ? advice : "Chưa có dặn dò.");

                        tvFile1.setText("📄 Kết quả khám tổng quát.pdf");
                        tvFile2.setText("📄 Phiếu xét nghiệm.pdf");
                    } else {
                        Toast.makeText(this, "Hồ sơ không tồn tại trên hệ thống!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Lỗi tải dữ liệu: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupBottomNav() {
        // Đồng bộ hành vi chuyển đổi intent giống hệt như các màn hình chính khác
        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        navAppointments.setOnClickListener(v -> {
            startActivity(new Intent(this, HistoryActivity.class));
            finish();
        });

        navPackages.setOnClickListener(v -> {
            startActivity(new Intent(this, PackageActivity.class));
            finish();
        });

        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
}