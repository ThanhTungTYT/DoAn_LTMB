package com.example.ltmb_nhom11.ui;

import android.os.Bundle;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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


        rvMedicalPackages.setLayoutManager(
                new LinearLayoutManager(this)
        );

        PackageAdapter adapter =
                new PackageAdapter(packages, medicalPackage -> {

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
    }

}