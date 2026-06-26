package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.util.SessionManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvName, tvEmailHeader, tvFullName, tvPhone, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName = findViewById(R.id.tvName);
        tvEmailHeader = findViewById(R.id.tvEmailHeader);
        tvFullName = findViewById(R.id.tvFullName);
        tvPhone = findViewById(R.id.tvPhone);
        tvEmail = findViewById(R.id.tvEmail);

        loadUserInfo();

        findViewById(R.id.rowViewHistory).setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        findViewById(R.id.rowLogout).setOnClickListener(v -> logout());

        // Các chức năng làm dần sau
        findViewById(R.id.rowEditInfo).setOnClickListener(v ->
                Toast.makeText(this, "Đổi thông tin cá nhân — đang phát triển", Toast.LENGTH_SHORT).show());
        findViewById(R.id.rowChangePassword).setOnClickListener(v ->
                Toast.makeText(this, "Đổi mật khẩu — đang phát triển", Toast.LENGTH_SHORT).show());
        findViewById(R.id.btnChangeAvatar).setOnClickListener(v ->
                Toast.makeText(this, "Upload ảnh đại diện — sẽ làm sau", Toast.LENGTH_SHORT).show());

        setupBottomNav();
    }

    private void setupBottomNav() {
        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent i = new Intent(this, com.example.ltmb_nhom11.MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
            finish();
        });
        findViewById(R.id.navAppointments).setOnClickListener(v ->
                startActivity(new Intent(this, DoctorSearchActivity.class)));
        findViewById(R.id.navPackages).setOnClickListener(v ->
                Toast.makeText(this, "Chức năng Gói khám đang được xây dựng!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.navProfile).setOnClickListener(v -> { /* đang ở màn Cá nhân */ });
    }

    private void loadUserInfo() {
        FirebaseUser user = SessionManager.getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;
                    String name = doc.getString("fullName");
                    String phone = doc.getString("phone");
                    String email = doc.getString("email");
                    if (name != null && !name.isEmpty()) {
                        tvName.setText(name);
                        tvFullName.setText(name);
                    }
                    if (email != null && !email.isEmpty()) {
                        tvEmailHeader.setText(email);
                        tvEmail.setText(email);
                    }
                    if (phone != null && !phone.isEmpty()) {
                        tvPhone.setText(phone);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Không tải được thông tin: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        SessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
