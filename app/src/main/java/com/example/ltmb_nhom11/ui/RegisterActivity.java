package com.example.ltmb_nhom11.ui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Locale;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtEmail, edtPassword, edtDob;
    private TextView tvErrorFullName, tvErrorPhone, tvErrorEmail, tvErrorPassword, tvErrorGender, tvErrorDob;
    private RadioGroup rgGender;
    private ImageButton btnTogglePassword, btnPickDob;
    private Button btnRegister;
    private TextView tvLoginLink;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(0, 0, 0, imeBottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        edtFullName = findViewById(R.id.edtFullName);
        edtPhone = findViewById(R.id.edtPhone);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtDob = findViewById(R.id.edtDob);

        tvErrorFullName = findViewById(R.id.tvErrorFullName);
        tvErrorPhone = findViewById(R.id.tvErrorPhone);
        tvErrorEmail = findViewById(R.id.tvErrorEmail);
        tvErrorPassword = findViewById(R.id.tvErrorPassword);
        tvErrorGender = findViewById(R.id.tvErrorGender);
        tvErrorDob = findViewById(R.id.tvErrorDob);

        rgGender = findViewById(R.id.rgGender);
        btnPickDob = findViewById(R.id.btnPickDob);

        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            if (isPasswordVisible) {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
            } else {
                edtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePassword.setImageResource(R.drawable.ic_visibility);
            }
            edtPassword.setSelection(edtPassword.getText().length());
        });

        // Bấm icon lịch -> mở DatePickerDialog, nhưng vẫn có thể tự gõ tay vào edtDob
        btnPickDob.setOnClickListener(v -> showDatePicker());
        edtDob.setOnClickListener(v -> showDatePicker());

        btnRegister.setOnClickListener(v -> doRegister());

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR) - 18; // gợi ý mặc định 18 tuổi
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String formattedDate = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                    edtDob.setText(formattedDate);
                }, year, month, day);

        // Không cho chọn ngày trong tương lai
        datePicker.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePicker.show();
    }

    private void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void clearError(TextView errorView) {
        errorView.setVisibility(View.GONE);
    }

    private void clearAllErrors() {
        clearError(tvErrorFullName);
        clearError(tvErrorPhone);
        clearError(tvErrorEmail);
        clearError(tvErrorPassword);
        clearError(tvErrorGender);
        clearError(tvErrorDob);
    }

    private void doRegister() {
        clearAllErrors();

        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();

        boolean hasError = false;

        if (fullName.isEmpty()) {
            showError(tvErrorFullName, "Vui lòng nhập họ và tên!");
            hasError = true;
        }

        if (phone.isEmpty()) {
            showError(tvErrorPhone, "Vui lòng nhập số điện thoại!");
            hasError = true;
        } else if (!isValidVietnamesePhone(phone)) {
            showError(tvErrorPhone, "Số điện thoại không hợp lệ! (VD: 0912345678)");
            hasError = true;
        }

        if (email.isEmpty()) {
            showError(tvErrorEmail, "Vui lòng nhập email!");
            hasError = true;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError(tvErrorEmail, "Email không hợp lệ!");
            hasError = true;
        }

        if (password.isEmpty()) {
            showError(tvErrorPassword, "Vui lòng nhập mật khẩu!");
            hasError = true;
        } else if (!isStrongPassword(password)) {
            showError(tvErrorPassword, "Mật khẩu cần ít nhất 8 ký tự, gồm chữ in hoa, số và ký tự đặc biệt!");
            hasError = true;
        }

        int selectedGenderId = rgGender.getCheckedRadioButtonId();
        if (selectedGenderId == -1) {
            showError(tvErrorGender, "Vui lòng chọn giới tính!");
            hasError = true;
        }

        if (dob.isEmpty()) {
            showError(tvErrorDob, "Vui lòng chọn hoặc nhập ngày sinh!");
            hasError = true;
        } else if (!isValidDob(dob)) {
            showError(tvErrorDob, "Ngày sinh không hợp lệ! (Định dạng: dd/MM/yyyy)");
            hasError = true;
        }

        if (hasError) return;

        String gender = ((RadioButton) findViewById(selectedGenderId)).getText().toString();

        btnRegister.setEnabled(false);

        db.collection("users").whereEqualTo("phone", phone).limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        btnRegister.setEnabled(true);
                        showError(tvErrorPhone, "Số điện thoại này đã được sử dụng để đăng ký!");
                        return;
                    }
                    createAccount(fullName, phone, email, password, gender, dob);
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, "Lỗi kiểm tra số điện thoại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createAccount(String fullName, String phone, String email, String password,
                               String gender, String dob) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    User newUser = new User(uid, fullName, phone, email, gender, dob, "user");

                    db.collection("users").document(uid).set(newUser)
                            .addOnSuccessListener(unused -> sendVerificationAndRedirect(email))
                            .addOnFailureListener(e -> {
                                btnRegister.setEnabled(true);
                                Toast.makeText(this, "Lỗi lưu hồ sơ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    showError(tvErrorEmail, "Đăng ký thất bại: " + e.getMessage());
                });
    }

    private void sendVerificationAndRedirect(String email) {
        mAuth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(task -> {
                    btnRegister.setEnabled(true);
                    mAuth.signOut();

                    if (task.isSuccessful()) {
                        Toast.makeText(this,
                                "Đăng ký thành công! Vui lòng kiểm tra email " + email +
                                        " để xác minh tài khoản, sau đó đăng nhập.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                "Đăng ký thành công nhưng gửi email xác minh thất bại. Bạn vẫn có thể đăng nhập.",
                                Toast.LENGTH_LONG).show();
                    }

                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                });
    }
    private boolean isValidVietnamesePhone(String phone) {
        return phone.matches("^0[35789][0-9]{8}$");
    }

    private boolean isStrongPassword(String password) {
        String pattern = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>_\\-+=]).{8,}$";
        return password.matches(pattern);
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
            Calendar now = Calendar.getInstance();
            return cal.before(now);
        } catch (Exception e) {
            return false;
        }
    }
}