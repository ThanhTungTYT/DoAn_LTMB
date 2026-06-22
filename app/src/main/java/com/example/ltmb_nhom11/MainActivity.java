package com.example.ltmb_nhom11;

import com.example.ltmb_nhom11.ui.DirectionsActivity;
import com.example.ltmb_nhom11.ui.DoctorSearchActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ltmb_nhom11.ui.ImageLoader;

public class MainActivity extends AppCompatActivity {

    private static final String AVATAR_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuDKtHZNQvkPdeno_hqoa1P2yqfCVxfxJzkUxhzH68ujn0iBGAuZH5S6aQekLDTqFZZmvIlaWI9tlU9LYdX7b5OOpon1CaQSw8WG8kGtJ2Xo8clkqdBmIKS88h2NX1CRusDGwESHsE6khHIGyZdZqaPpdOH2rzMUodmchBkh4zMsKhblnE82rmPEHk4TE0nFJbykQAy08LpoqcRDC_9_q-YHZpPkbni-AueCe7Jf1b1rc8XTWBjUl3INntWeQ1tJ2DATvBgNuXj50l6t";
    private static final String MAP_PREVIEW_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuCBM3GfSf4XeLhBw3yRGTsG8YAbkkWM31cdIpkiqmIhUO2x-nkApumb6TSklwSrT8lu4QV0Z4pGauJlJ8djhQiFoHk24CSFM-wdcTKddy9S_RAvi8JXIvX9pecvNBhQMtdExv3uvcfllzlylyiWWGxDZsgH879x3APmAEPEPT2UjXdcgSACMSmvG9phLaAFiqXTBW3FDo1WfntQUr481xUc5BKlFYbv8X6y23OrmEzhVogKo-WXlJeIggBBg1Hm2IbzzRjQKMh_P1Q2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind and Load images
        ImageView imgUserAvatar = findViewById(R.id.imgUserAvatar);
        ImageView imgMapPreview = findViewById(R.id.imgMapPreview);

        ImageLoader.load(AVATAR_URL, imgUserAvatar);
        ImageLoader.load(MAP_PREVIEW_URL, imgMapPreview);

        // Bento quick action listeners
        LinearLayout btnQuickBook = findViewById(R.id.btnQuickBook);
        btnQuickBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAppointments();
            }
        });

        LinearLayout btnQuickCheckup = findViewById(R.id.btnQuickCheckup);
        btnQuickCheckup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Khám sức khỏe tổng quát đã sẵn sàng!", Toast.LENGTH_SHORT).show();
                navigateToAppointments();
            }
        });

        // "Xem tất cả" list filter action
        TextView btnViewAllAppointments = findViewById(R.id.btnViewAllAppointments);
        btnViewAllAppointments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAppointments();
            }
        });

        // Appointment Card interactions
        LinearLayout cardAppointment = findViewById(R.id.cardAppointment);
        cardAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Chi tiết cuộc hẹn: BS. Lê Hoàng Nam khoa Tim mạch, ngày 24 vào lúc 09:30", Toast.LENGTH_LONG).show();
            }
        });

        // Clinic Location interactions (Direct to Screen 2)
        LinearLayout cardLocation = findViewById(R.id.cardLocation);
        cardLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToDirections();
            }
        });

        TextView btnOpenDirections = findViewById(R.id.btnOpenDirections);
        btnOpenDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToDirections();
            }
        });

        // Bottom navigation routing
        LinearLayout navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Alread on Home
            }
        });

        LinearLayout navAppointments = findViewById(R.id.navAppointments);
        navAppointments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateToAppointments();
            }
        });

        LinearLayout navPackages = findViewById(R.id.navPackages);
        navPackages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Chức năng Gói khám (Packages) đang được xây dựng!", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Chức năng Cá nhân (Profile) đang được xây dựng!", Toast.LENGTH_SHORT).show();
            }
        });

        ImageView btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Bạn không có thông báo mới!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navigateToDirections() {
        Intent intent = new Intent(MainActivity.this, DirectionsActivity.class);
        startActivity(intent);
    }

    private void navigateToAppointments() {
        Intent intent = new Intent(MainActivity.this, DoctorSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
    }
}
