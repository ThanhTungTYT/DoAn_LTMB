package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ltmb_nhom11.MainActivity;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.util.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText edtPhone, edtPassword;
    private ImageButton btnTogglePassword;
    private CheckBox cbRememberMe;
    private Button btnLogin, btnGoogle, btnFacebook;
    private TextView tvRegisterLink, tvForgotPassword;
    private boolean isPasswordVisible = false;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (SessionManager.isLoggedIn() && mAuth.getCurrentUser().isEmailVerified()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogle = findViewById(R.id.btnGoogle);
        btnFacebook = findViewById(R.id.btnFacebook);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

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

        btnLogin.setOnClickListener(v -> doLogin());

        tvRegisterLink.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));

        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));

        btnGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập Google sẽ được tích hợp sau!", Toast.LENGTH_SHORT).show());
        btnFacebook.setOnClickListener(v ->
                Toast.makeText(this, "Đăng nhập Facebook sẽ được tích hợp sau!", Toast.LENGTH_SHORT).show());
    }

    private void doLogin() {
        String phone = edtPhone.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (phone.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đủ Số điện thoại và Mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnLogin.setEnabled(false);

        db.collection("users").whereEqualTo("phone", phone).limit(1).get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        btnLogin.setEnabled(true);
                        Toast.makeText(this, "Số điện thoại chưa được đăng ký!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    QueryDocumentSnapshot doc = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                    String email = doc.getString("email");
                    String role = doc.getString("role");

                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnSuccessListener(authResult -> {
                                btnLogin.setEnabled(true);
                                FirebaseUser user = authResult.getUser();

                                if (!user.isEmailVerified()) {
                                    showVerifyEmailPrompt(user);
                                    return;
                                }

                                Toast.makeText(this, "Đăng nhập thành công! Vai trò: " + role, Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                btnLogin.setEnabled(true);
                                Toast.makeText(this, "Sai mật khẩu hoặc tài khoản không hợp lệ!", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    btnLogin.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showVerifyEmailPrompt(FirebaseUser user) {
        Toast.makeText(this,
                "Email của bạn chưa được xác minh! Đang gửi lại email xác minh...",
                Toast.LENGTH_LONG).show();

        user.sendEmailVerification()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Đã gửi lại email xác minh, vui lòng kiểm tra hộp thư!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this, "Gửi lại email xác minh thất bại!", Toast.LENGTH_SHORT).show();
                    }
                    // Đăng xuất vì chưa được xác minh, không cho vào MainActivity
                    mAuth.signOut();
                });
    }
}