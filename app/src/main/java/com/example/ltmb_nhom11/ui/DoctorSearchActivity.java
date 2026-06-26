package com.example.ltmb_nhom11.ui;

import com.example.ltmb_nhom11.MainActivity;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Doctor;
import com.example.ltmb_nhom11.model.DoctorAdapter;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DoctorSearchActivity extends AppCompatActivity {

    private RecyclerView recyclerDoctors;
    private DoctorAdapter doctorAdapter;
    private List<Doctor> doctorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_search);


        recyclerDoctors = findViewById(R.id.recyclerDoctors);
        recyclerDoctors.setLayoutManager(new LinearLayoutManager(this));


        initMockData();


        doctorAdapter = new DoctorAdapter(doctorList, doctor -> openDoctorDetail(doctor.getName()));
        recyclerDoctors.setAdapter(doctorAdapter);


        setupBottomNavigation();

        android.widget.EditText etSearch = findViewById(R.id.etSearch);


        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filterDoctors(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    private void filterDoctors(String text) {
        List<Doctor> filteredList = new ArrayList<>();


        for (Doctor doc : doctorList) {

            if (doc.getName().toLowerCase().contains(text.toLowerCase()) ||
                    doc.getDept().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(doc);
            }
        }


        doctorAdapter.updateList(filteredList);
    }

    private void initMockData() {
        doctorList = new ArrayList<>();

        Doctor doc1 = new Doctor("BS. Nguyễn Thị Lan", "Khoa Sản", "12 năm KN");
        doc1.setAvatarUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuD08wC_qL6hOm4NWjF-iBFy7uJzXvQDdk9FyfPDZVueVKIHaaUMv41vJduGXuqPN-CKfhf9ZODzcob2Lmm-U9bWSu224MTXp3uX0aeDiYbC_qijJNtUq-nvDal5GahLEPcN_yVmaxVpWvQzwQ6rv4FVplc3XOBqXLvesaVKMYo9VFoNppKlyUR9luluThNmGloEtCA1sm_UF_QZP1FHbHtwH8zAlGiwlveFzi4dLCAOcnA86e8sBu9TgG0QrmNBlt09yUw1azm8d8au");

        Doctor doc2 = new Doctor("TS.BS. Trần Văn Hùng", "Khoa Tim Mạch", "20 năm KN");
        doc2.setAvatarUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuDe12CPPIh3L-kFXLLJwkQ8QUOFmmLfSxauKAt8qNBAVe6y1kBcEKZj1Fd51KpT5Aoa6VneZ1DW4IPBt7COGy-0xU3ZPJqWfg5lelPOw9zO2NHgT1AHEDMNx15XhrrOOY8pE_NRsykVhXNaJpvtStQcodZmBU_URGO6GuuaisZP_rFJxdv-IotLr_1SZ-AHv4lbECCtLQZaCzGm_r6ZtyF0Tyis8LesHNk77fpO4Axd-aVpJqtVxnd0gsghJ2niKCq3Y3v07EF6ebzC");

        Doctor doc3 = new Doctor("ThS.BS. Lê Minh Duy", "Khoa Nhi", "8 năm KN");
        doc3.setAvatarUrl("https://lh3.googleusercontent.com/aida-public/AB6AXuBUjlRrybqQgLPekTNjPNh5OygP0rKWH4DqDCcSNuLUl12OVeRF5niuuyRsgl2JkTGpMp3f_uOQs9fWNh-AeP3jrKO4sFVyLjAjkP_wRGvTsdXAAaF2HBP7OpYMqmkz9twVbWwBiWjvDxTsq8NbRrbZhxR-xSigMx4Qmn_SlPJEf86ZNns-qcNt2aeyntHQ-6I4WyukNmX-Lc_vPF9-cqO2ZkbZAs7kRHtyaBxBruDpzFl0MFuzT16gnRZs4QCVGmokJpQCNE5z9nun");

        doctorList.add(doc1);
        doctorList.add(doc2);
        doctorList.add(doc3);
    }

    private void setupBottomNavigation() {
        LinearLayout navHome = findViewById(R.id.navHome);
        navHome.setOnClickListener(v -> navigateToHome());

        LinearLayout navAppointments = findViewById(R.id.navAppointments);
        navAppointments.setOnClickListener(v -> { /* Đang ở màn hình này */ });

        LinearLayout navPackages = findViewById(R.id.navPackages);
        navPackages.setOnClickListener(v ->
                Toast.makeText(DoctorSearchActivity.this, "Chức năng Gói khám (Packages) đang được xây dựng!", Toast.LENGTH_SHORT).show()
        );

        LinearLayout navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(DoctorSearchActivity.this, ProfileActivity.class)));

        ImageView btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(v ->
                Toast.makeText(DoctorSearchActivity.this, "Bạn không có thông báo mới!", Toast.LENGTH_SHORT).show()
        );
    }

    private void navigateToHome() {
        Intent intent = new Intent(DoctorSearchActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }

    private void openDoctorDetail(String doctorName) {
        Intent intent = new Intent(DoctorSearchActivity.this, DoctorDetailActivity.class);
        intent.putExtra("doctorName", doctorName);
        startActivity(intent);
    }
}