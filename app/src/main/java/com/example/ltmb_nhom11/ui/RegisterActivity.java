package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterActivity extends AppCompatActivity {

    private EditText edtFullName, edtPhone, edtEmail, edtPassword;
    private TextView tvErrorFullName, tvErrorPhone, tvErrorEmail, tvErrorPassword;
    private ImageButton btnTogglePassword;
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

        tvErrorFullName = findViewById(R.id.tvErrorFullName);
        tvErrorPhone = findViewById(R.id.tvErrorPhone);
        tvErrorEmail = findViewById(R.id.tvErrorEmail);
        tvErrorPassword = findViewById(R.id.tvErrorPassword);

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

        btnRegister.setOnClickListener(v -> doRegister());

        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
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
    }

    private void doRegister() {
        clearAllErrors();

        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

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

        if (hasError) return;

        btnRegister.setEnabled(false);

        db.collection("users").whereEqualTo("phone", phone).limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        btnRegister.setEnabled(true);
                        showError(tvErrorPhone, "Số điện thoại này đã được sử dụng để đăng ký!");
                        return;
                    }
                    createAccount(fullName, phone, email, password);
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    Toast.makeText(this, "Lỗi kiểm tra số điện thoại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void createAccount(String fullName, String phone, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    User newUser = new User(uid, fullName, phone, email, "user");

                    db.collection("users").document(uid).set(newUser)
                            .addOnSuccessListener(unused -> sendVerificationAndRedirect(email))
                            .addOnFailureListener(e -> {
                                btnRegister.setEnabled(true);
                                Toast.makeText(this, "Lỗi lưu hồ sơ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnRegister.setEnabled(true);
                    // Lỗi từ Firebase (ví dụ email đã tồn tại) -> hiện ngay dưới ô Email
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
}