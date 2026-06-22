package com.example.ltmb_nhom11.ui;
import com.example.ltmb_nhom11.MainActivity;


import com.example.ltmb_nhom11.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DoctorSearchActivity extends AppCompatActivity {

    private static final String DOC_LAN_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuD08wC_qL6hOm4NWjF-iBFy7uJzXvQDdk9FyfPDZVueVKIHaaUMv41vJduGXuqPN-CKfhf9ZODzcob2Lmm-U9bWSu224MTXp3uX0aeDiYbC_qijJNtUq-nvDal5GahLEPcN_yVmaxVpWvQzwQ6rv4FVplc3XOBqXLvesaVKMYo9VFoNppKlyUR9luluThNmGloEtCA1sm_UF_QZP1FHbHtwH8zAlGiwlveFzi4dLCAOcnA86e8sBu9TgG0QrmNBlt09yUw1azm8d8au";
    private static final String DOC_HUNG_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuDe12CPPIh3L-kFXLLJwkQ8QUOFmmLfSxauKAt8qNBAVe6y1kBcEKZj1Fd51KpT5Aoa6VneZ1DW4IPBt7COGy-0xU3ZPJqWfg5lelPOw9zO2NHgT1AHEDMNx15XhrrOOY8pE_NRsykVhXNaJpvtStQcodZmBU_URGO6GuuaisZP_rFJxdv-IotLr_1SZ-AHv4lbECCtLQZaCzGm_r6ZtyF0Tyis8LesHNk77fpO4Axd-aVpJqtVxnd0gsghJ2niKCq3Y3v07EF6ebzC";
    private static final String DOC_DUY_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuBUjlRrybqQgLPekTNjPNh5OygP0rKWH4DqDCcSNuLUl12OVeRF5niuuyRsgl2JkTGpMp3f_uOQs9fWNh-AeP3jrKO4sFVyLjAjkP_wRGvTsdXAAaF2HBP7OpYMqmkz9twVbWwBiWjvDxTsq8NbRrbZhxR-xSigMx4Qmn_SlPJEf86ZNns-qcNt2aeyntHQ-6I4WyukNmX-Lc_vPF9-cqO2ZkbZAs7kRHtyaBxBruDpzFl0MFuzT16gnRZs4QCVGmokJpQCNE5z9nun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_search);

        // Bind image holders and load real network imagery
        ImageView imgDocLan = findViewById(R.id.imgDocLan);
        ImageView imgDocHung = findViewById(R.id.imgDocHung);
        ImageView imgDocDuy = findViewById(R.id.imgDocDuy);

        ImageLoader.load(DOC_LAN_URL, imgDocLan);
        ImageLoader.load(DOC_HUNG_URL, imgDocHung);
        ImageLoader.load(DOC_DUY_URL, imgDocDuy);

        // Doctor booking quick listeners
        Button btnBookLan = findViewById(R.id.btnBookLan);
        btnBookLan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DoctorSearchActivity.this, "Đặt lịch thành công với BS. Nguyễn Thị Lan!", Toast.LENGTH_LONG).show();
            }
        });

        Button btnBookHung = findViewById(R.id.btnBookHung);
        btnBookHung.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DoctorSearchActivity.this, "Đặt lịch thành công với TS.BS. Trần Văn Hùng!", Toast.LENGTH_LONG).show();
            }
        });

        Button btnBookDuy = findViewById(R.id.btnBookDuy);
        btnBookDuy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DoctorSearchActivity.this, "Đặt lịch thành công với ThS.BS. Lê Minh Duy!", Toast.LENGTH_LONG).show();
            }
        });

        // Bottom navigation handlers
        LinearLayout navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToHome();
            }
        });

        LinearLayout navAppointments = findViewById(R.id.navAppointments);
        navAppointments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Already on appointments
            }
        });

        LinearLayout navPackages = findViewById(R.id.navPackages);
        navPackages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DoctorSearchActivity.this, "Chức năng Gói khám (Packages) đang được xây dựng!", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DoctorSearchActivity.this, "Chức năng Cá nhân (Profile) đang được xây dựng!", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DoctorSearchActivity.this, "Bạn không có thông báo mới!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(DoctorSearchActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}
