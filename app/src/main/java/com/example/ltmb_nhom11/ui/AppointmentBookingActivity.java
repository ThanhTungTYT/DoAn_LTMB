package com.example.ltmb_nhom11.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ltmb_nhom11.R;

public class AppointmentBookingActivity extends AppCompatActivity {

    private TextView tvMainPackageName;
    private TextView tvPackagePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appointment_booking);

        tvMainPackageName = findViewById(R.id.tvMainPackageName);
        tvPackagePrice = findViewById(R.id.tvPackagePrice);

        String packageName = getIntent().getStringExtra("packageName");
        double price = getIntent().getDoubleExtra("price", 0);

        if (packageName != null) {
            tvMainPackageName.setText(packageName);
        }

        tvPackagePrice.setText((long) price + " VNĐ");
    }
}