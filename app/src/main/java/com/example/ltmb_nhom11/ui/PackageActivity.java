package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.MainActivity;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Package;
import com.example.ltmb_nhom11.repository.PackageRepository;
import com.example.ltmb_nhom11.ui.adapter.PackageAdapter;

import java.util.List;

public class PackageActivity extends AppCompatActivity {

    private RecyclerView rvMedicalPackages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);


        rvMedicalPackages = findViewById(R.id.rvMedicalPackages);

        PackageRepository repository = new PackageRepository();
        List<Package> packages = repository.getPackages();

        rvMedicalPackages.setLayoutManager(new LinearLayoutManager(this));

        PackageAdapter adapter = new PackageAdapter(
                packages,
                medicalPackage -> {

                    Intent intent = new Intent(
                            PackageActivity.this,
                            AppointmentBookingActivity.class
                    );

                    intent.putExtra(
                            "packageName",
                            medicalPackage.getName()
                    );

                    intent.putExtra(
                            "price",
                            medicalPackage.getPrice()
                    );

                    startActivity(intent);
                });

        rvMedicalPackages.setAdapter(adapter);


        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navAppointments = findViewById(R.id.navAppointments);
        LinearLayout navPackages = findViewById(R.id.navPackages);
        LinearLayout navProfile = findViewById(R.id.navProfile);

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