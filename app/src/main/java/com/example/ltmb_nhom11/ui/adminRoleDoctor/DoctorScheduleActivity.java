package com.example.ltmb_nhom11.ui.adminRoleDoctor;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Doctor;
import com.example.ltmb_nhom11.model.DoctorAdapter;

import java.util.ArrayList;
import java.util.List;

public class DoctorScheduleActivity extends AppCompatActivity {

    private RecyclerView rvDoctors;
    private DoctorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_schedule);

        rvDoctors = findViewById(R.id.rvDoctors);
        rvDoctors.setLayoutManager(new LinearLayoutManager(this));

        List<Doctor> doctorList = new ArrayList<>();
        doctorList.add(new Doctor("Dr. Sarah Jenkins", "Cardiology", "Available"));
        doctorList.add(new Doctor("Dr. Marcus Thorne", "Neurology", "On Leave"));
        doctorList.add(new Doctor("Dr. Kevin Zhang", "Pediatrics", "Available"));

        adapter = new DoctorAdapter(doctorList, null);
        rvDoctors.setAdapter(adapter);
    }
}