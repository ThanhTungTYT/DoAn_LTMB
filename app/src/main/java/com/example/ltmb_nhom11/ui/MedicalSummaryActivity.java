package com.example.ltmb_nhom11.ui;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.ltmb_nhom11.R;

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

        String packageName = getIntent().getStringExtra("packageName");
        String date = getIntent().getStringExtra("date");
        String time = getIntent().getStringExtra("time");
        String medicalId = getIntent().getStringExtra("medicalId");

        if (packageName == null) packageName = "Khám Nội Tổng Quát";
        if (date == null) date = "24/10/2026";
        if (time == null) time = "14:30";
        if (medicalId == null) medicalId = "#MED-001";

        tvPackageName.setText(packageName);
        tvDateTime.setText(time + " - " + date);
        tvMedicalId.setText("Mã hồ sơ: " + medicalId);

        tvDoctorName.setText("BS. Nguyễn Thanh Tùng");
        tvDoctorMajor.setText("Khoa Nội Tổng Quát");

        tvBloodPressure.setText("Huyết áp: 120/80 mmHg");
        tvTemperature.setText("Nhiệt độ: 36.6°C");
        tvWeight.setText("Cân nặng: 68 kg");

        tvDiagnosis.setText("Viêm dạ dày cấp nhẹ do thói quen ăn uống.");

        tvMedicine.setText(
                "• Nexium 40mg\n" +
                        "• Gaviscon Dual Action\n" +
                        "• Motilium M"
        );

        // Advice
        tvAdvice.setText(
                "• Ăn đúng giờ\n" +
                        "• Hạn chế đồ cay nóng\n" +
                        "• Tái khám sau 2 tuần"
        );

        // Files
        tvFile1.setText("📄 Phieu_Noi_Soi.pdf");
        tvFile2.setText("🖼️ Hinh_XQuang.jpg");
    }

    private void setupBottomNav() {

        navHome.setOnClickListener(v -> {
        });

        navAppointments.setOnClickListener(v -> {
        });

        navPackages.setOnClickListener(v -> {
        });

        navProfile.setOnClickListener(v -> {
        });
    }
}