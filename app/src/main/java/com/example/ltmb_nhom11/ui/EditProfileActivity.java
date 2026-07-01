package com.example.ltmb_nhom11.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.util.SessionManager;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtEmail, edtDob;
    private RadioGroup rgGender;
    private ImageButton btnPickDob;
    private Button btnSaveProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ImageButton btnBack = findViewById(R.id.btnBackEditProfile);
        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtDob = findViewById(R.id.edtDob);
        rgGender = findViewById(R.id.rgGender);
        btnPickDob = findViewById(R.id.btnPickDob);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        btnBack.setOnClickListener(v -> finish());
        btnSaveProfile.setOnClickListener(v -> saveProfile());

        edtDob.setOnClickListener(v -> showDatePicker());
        btnPickDob.setOnClickListener(v -> showDatePicker());

        loadUserInfo();
    }

    private void loadUserInfo() {
        FirebaseUser user = SessionManager.getCurrentUser();
        if (user == null) return;
        FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    edtFullName.setText(doc.getString("fullName"));
                    edtPhone.setText(doc.getString("phone"));
                    edtEmail.setText(doc.getString("email"));
                    edtDob.setText(doc.getString("dob"));
                    applyGender(doc.getString("gender"));
                });
    }

    private void applyGender(String gender) {
        if (gender == null) return;
        if (gender.equals("Nam")) {
            rgGender.check(R.id.rbMale);
        } else if (gender.equals("Nữ")) {
            rgGender.check(R.id.rbFemale);
        } else if (gender.equals("Khác")) {
            rgGender.check(R.id.rbOther);
        }
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 18;
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        String current = edtDob.getText().toString().trim();
        if (current.matches("^\\d{2}/\\d{2}/\\d{4}$")) {
            String[] parts = current.split("/");
            day = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]) - 1;
            year = Integer.parseInt(parts[2]);
        }

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    edtDob.setText(formattedDate);
                }, year, month, day);

        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void saveProfile() {
        String fullName = edtFullName.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();

        if (fullName.isEmpty()) {
            Toast.makeText(this, "Họ tên không được để trống!", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            Toast.makeText(this, "Vui lòng chọn giới tính!", Toast.LENGTH_SHORT).show();
            return;
        }
        String gender = ((RadioButton) findViewById(selectedGenderId)).getText().toString();

        if (dob.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ngày sinh!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidDob(dob)) {
            Toast.makeText(this, "Ngày sinh không hợp lệ! (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = SessionManager.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn.", Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("gender", gender);
        updates.put("dob", dob);

        btnSaveProfile.setEnabled(false);
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Đã cập nhật thông tin!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private boolean isValidDob(String dob) {
        if (!dob.matches("^\\d{2}/\\d{2}/\\d{4}$")) return false;
        try {
            String[] parts = dob.split("/");
            int day = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int year = Integer.parseInt(parts[2]);

            Calendar cal = Calendar.getInstance();
            cal.setLenient(false);
            cal.set(year, month - 1, day);
            cal.getTime();
            return cal.before(Calendar.getInstance());
        } catch (Exception e) {
            return false;
        }
    }
}
