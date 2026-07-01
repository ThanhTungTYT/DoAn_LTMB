package com.example.ltmb_nhom11.ui;
import com.example.ltmb_nhom11.MainActivity;

import android.content.Intent;
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
        double price = getIntent().getDoubleExtra("price", 0);
        String medicalId = getIntent().getStringExtra("medicalId");

        if (packageName == null) packageName = "Khám sức khỏe tổng quát";
        if (date == null) date = "Chưa chọn";
        if (time == null) time = "Chưa chọn";

        if (medicalId == null) {
            medicalId = "#MED-" + (1000 + (int)(Math.random() * 9000));
        }

        tvPackageName.setText(packageName);
        tvDateTime.setText(time + " - " + date);
        tvMedicalId.setText("Mã hồ sơ: " + medicalId);

        tvDoctorName.setText("BS. Nguyễn Thanh Tùng");
        tvDoctorMajor.setText("Chuyên khoa Nội tổng quát");

        tvBloodPressure.setText("Huyết áp: 120/80 mmHg");
        tvTemperature.setText("Nhiệt độ: 36.6°C");
        tvWeight.setText("Cân nặng: 68 kg");

        tvDiagnosis.setText(
                "Sức khỏe ổn định, chưa phát hiện bất thường đáng kể."
        );

        tvMedicine.setText(
                "• Vitamin tổng hợp\n" +
                        "• Không kê đơn thuốc đặc trị"
        );

        tvAdvice.setText(
                "• Duy trì chế độ ăn uống lành mạnh\n" +
                        "• Tập thể dục tối thiểu 30 phút mỗi ngày\n" +
                        "• Khám sức khỏe định kỳ mỗi 6 - 12 tháng"
        );

        tvFile1.setText("📄 Kết quả khám tổng quát.pdf");
        tvFile2.setText("📄 Phiếu xét nghiệm.pdf");
    }

    private void setupBottomNav() {

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        navAppointments.setOnClickListener(v -> {
            startActivity(new Intent(this, DoctorSearchActivity.class));
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