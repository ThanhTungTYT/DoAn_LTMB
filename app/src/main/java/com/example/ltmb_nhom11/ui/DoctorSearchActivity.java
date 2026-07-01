package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.MainActivity;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Doctor;
import com.example.ltmb_nhom11.model.DoctorAdapter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DoctorSearchActivity extends AppCompatActivity {

    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private DoctorAdapter doctorAdapter;
    private List<Doctor> doctorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_search);


        RecyclerView recyclerDoctors = findViewById(R.id.recyclerDoctors);
        recyclerDoctors.setLayoutManager(new LinearLayoutManager(this));

        doctorList = new ArrayList<>();
        doctorAdapter = new DoctorAdapter(doctorList, this::openDoctorDetail);
        recyclerDoctors.setAdapter(doctorAdapter);


        loadDoctorsFromFirestore();


        setupBottomNavigation();


        EditText etSearch = findViewById(R.id.etSearch);
        ImageView btnClearText = findViewById(R.id.btnClearText);

        btnClearText.setOnClickListener(v -> {
            etSearch.setText("");
            etSearch.clearFocus();
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                btnClearText.setVisibility(s.toString().trim().length() > 0 ? View.VISIBLE : View.GONE);


                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

                searchRunnable = () -> filterDoctors(s.toString());
                searchHandler.postDelayed(searchRunnable, 400);
            }
        });
    }


    private String removeAccent(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replaceAll("Đ", "D").replaceAll("đ", "d");
    }


    private void filterDoctors(String text) {
        List<Doctor> filteredList = new ArrayList<>();
        String queryClean = removeAccent(text).toLowerCase();

        for (Doctor doc : doctorList) {
            String nameClean = removeAccent(doc.getName()).toLowerCase();
            String deptClean = removeAccent(doc.getDept()).toLowerCase();

            if (nameClean.contains(queryClean) || deptClean.contains(queryClean)) {
                filteredList.add(doc);
            }
        }
        doctorAdapter.updateList(filteredList);
    }


    private void loadDoctorsFromFirestore() {
        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("role", "doctor")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    doctorList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String name = document.getString("fullName");
                            if (name == null || name.isEmpty()) {
                                name = document.getString("name");
                            }

                            String dept = document.getString("dept");
                            String status = document.getString("status");
                            String avatarUrl = document.getString("avatarUrl");
                            String uid = document.getString("uid");

                            Doctor doctor = new Doctor(name, dept, status);
                            doctor.setAvatarUrl(avatarUrl != null ? avatarUrl : "");
                            doctor.setUid(uid != null ? uid : document.getId());

                            doctorList.add(doctor);
                        } catch (Exception e) {
                            Log.e("DoctorSearch", "Lỗi parse dữ liệu: " + e.getMessage());
                        }
                    }
                    doctorAdapter.updateList(doctorList);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DoctorSearchActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
                    Log.e("DoctorSearch", "Firestore Error: ", e);
                });
    }


    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(v -> navigateToHome());

        LinearLayout navAppointments = findViewById(R.id.navAppointments);
        navAppointments.setOnClickListener(v -> { /* Đang ở màn hình này rồi */ });

        LinearLayout navPackages = findViewById(R.id.navPackages);
        navPackages.setOnClickListener(v -> {
            startActivity(new Intent(DoctorSearchActivity.this, PackageActivity.class));
            finish();
        });

        LinearLayout navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(v -> startActivity(new Intent(DoctorSearchActivity.this, ProfileActivity.class)));
    }

    private void navigateToHome() {
        Intent intent = new Intent(DoctorSearchActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    private void openDoctorDetail(Doctor doctor) {
        Intent intent = new Intent(DoctorSearchActivity.this, DoctorDetailActivity.class);
        intent.putExtra("doctorUid", doctor.getUid());
        intent.putExtra("doctorName", doctor.getName());
        startActivity(intent);
    }
}