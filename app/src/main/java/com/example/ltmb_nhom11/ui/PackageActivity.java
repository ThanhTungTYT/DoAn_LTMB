package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.MainActivity;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Package;
import com.example.ltmb_nhom11.ui.adapter.PackageAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class PackageActivity extends AppCompatActivity {

    private RecyclerView rvMedicalPackages;
    private PackageAdapter adapter;
    private List<Package> packages = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);


        rvMedicalPackages = findViewById(R.id.rvMedicalPackages);

        rvMedicalPackages.setLayoutManager(new LinearLayoutManager(this));

        adapter = new PackageAdapter(
                packages,
                medicalPackage -> {

                    Intent intent = new Intent(
                            PackageActivity.this,
                            AppointmentBookingActivity.class
                    );

                    intent.putExtra("packageId", medicalPackage.getId());
                    intent.putExtra("packageName", medicalPackage.getName());
                    intent.putExtra("price", medicalPackage.getPrice());

                    startActivity(intent);
                });

        rvMedicalPackages.setAdapter(adapter);
        db = FirebaseFirestore.getInstance();
        loadPackages();

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
    private void loadPackages() {

        db.collection("packages")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    packages.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {

                        Package medicalPackage = doc.toObject(Package.class);

                        if (medicalPackage != null) {
                            medicalPackage.setId(doc.getId());
                            packages.add(medicalPackage);
                        }
                    }

                    adapter.notifyDataSetChanged();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            PackageActivity.this,
                            "Lỗi tải dữ liệu: " + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }
}