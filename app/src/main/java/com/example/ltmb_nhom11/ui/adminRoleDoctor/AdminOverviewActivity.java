package com.example.ltmb_nhom11.ui.adminRoleDoctor;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ltmb_nhom11.R;

public class AdminOverviewActivity extends AppCompatActivity {

    private ImageButton btnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.example.ltmb_nhom11.R.layout.activity_admin_overview);

        btnMenu = findViewById(R.id.btnMenu);
        btnMenu.setOnClickListener(v -> {
            Toast.makeText(AdminOverviewActivity.this, "Đang mở Sidebar Menu", Toast.LENGTH_SHORT).show();
        });
    }
}