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

    private void doRegister() {
        String fullName = edtFullName.getText().toString().trim();
        String phone = edtPhone.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ tất cả thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidVietnamesePhone(phone)) {
            Toast.makeText(this, "Số điện thoại không hợp lệ! (VD: 0912345678)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);

        // Kiểm tra SĐT đã được dùng chưa TRƯỚC khi tạo tài khoản Firebase Auth
        db.collection("users").whereEqualTo("phone", phone).limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        btnRegister.setEnabled(true);
                        Toast.makeText(this, "Số điện thoại này đã được sử dụng để đăng ký!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // SĐT chưa tồn tại -> tiến hành tạo tài khoản
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
                    Toast.makeText(this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
}