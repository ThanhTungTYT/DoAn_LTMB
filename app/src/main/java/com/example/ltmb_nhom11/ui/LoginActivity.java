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
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.ltmb_nhom11.MainActivity;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.ui.adminRoleDoctor.AdminOverviewActivity;
import com.example.ltmb_nhom11.util.SessionManager;
import com.example.ltmb_nhom11.ui.adminRoleDoctor.MyScheduleActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText edtPhone, edtPassword;
    private TextView tvErrorPhone, tvErrorPassword;
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

        android.util.Log.d("LOGIN_DEBUG", "FirebaseApp name=" + db.getApp().getName()
                + " projectId=" + db.getApp().getOptions().getProjectId()
                + " appId=" + db.getApp().getOptions().getApplicationId());

        db.collection("users").get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(snapshot -> {
                    android.util.Log.d("LOGIN_DEBUG", "SERVER Get-all SUCCESS, count=" + snapshot.size());
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("LOGIN_DEBUG", "SERVER Get-all FAILED: " + e.getMessage(), e);
                });

        setContentView(R.layout.activity_login);

        View rootView = findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            v.setPadding(0, 0, 0, imeBottom);
            return insets;
        });

        edtPhone = findViewById(R.id.edtPhone);
        edtPassword = findViewById(R.id.edtPassword);
        tvErrorPhone = findViewById(R.id.tvErrorPhone);
        tvErrorPassword = findViewById(R.id.tvErrorPassword);
        btnTogglePassword = findViewById(R.id.btnTogglePassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
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
        tvForgotPassword.setOnClickListener(v -> startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void showError(TextView errorView, String message) {
        errorView.setText(message);
        errorView.setVisibility(View.VISIBLE);
    }

    private void clearError(TextView errorView) {
        errorView.setVisibility(View.GONE);
    }

    private void clearAllErrors() {
        clearError(tvErrorPhone);
        clearError(tvErrorPassword);
    }

    private void doLogin() {
    clearAllErrors();

    String phoneRaw = edtPhone.getText().toString().trim();
    String password = edtPassword.getText().toString().trim();

    boolean hasError = false;

    if (phoneRaw.isEmpty()) {
        showError(tvErrorPhone, "Vui lòng nhập số điện thoại!");
        hasError = true;
    }

    if (password.isEmpty()) {
        showError(tvErrorPassword, "Vui lòng nhập mật khẩu!");
        hasError = true;
    }

    if (hasError) return;

    // Loại bỏ mọi ký tự không phải số
    String phoneInput = phoneRaw.replaceAll("[^0-9]", "");

    btnLogin.setEnabled(false);

    String phoneWithZero = phoneInput.startsWith("0") ? phoneInput : "0" + phoneInput;
    String phoneWithoutZero = phoneInput.startsWith("0") ? phoneInput.substring(1) : phoneInput;

    android.util.Log.d("LOGIN_DEBUG",
            "raw=[" + phoneRaw + "] cleaned=[" + phoneInput
                    + "] withZero=[" + phoneWithZero + "] withoutZero=[" + phoneWithoutZero + "]");

    db.collection("users")
            .whereIn("phone", java.util.Arrays.asList(phoneWithZero, phoneWithoutZero))
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {

                android.util.Log.d("LOGIN_DEBUG", "Query size=" + querySnapshot.size());

                if (querySnapshot.isEmpty()) {
                    btnLogin.setEnabled(true);
                    showError(tvErrorPhone, "Số điện thoại chưa được đăng ký!");
                    return;
                }

                QueryDocumentSnapshot doc =
                        (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);

                String email = doc.getString("email");
                String role = doc.getString("role");

                android.util.Log.d("LOGIN_DEBUG",
                        "Found doc id=" + doc.getId()
                                + " email=" + email
                                + " role=" + role);

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener(authResult -> {

                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null && !user.isEmailVerified()) {
                                btnLogin.setEnabled(true);
                                showVerifyEmailPrompt(user);
                                return;
                            }

                            btnLogin.setEnabled(true);
                            Toast.makeText(this,
                                    "Đăng nhập thành công!",
                                    Toast.LENGTH_SHORT).show();

                            navigateToDashboardBasedOnRole(role);
                        })
                        .addOnFailureListener(e -> {
                            android.util.Log.e("LOGIN_DEBUG",
                                    "Auth sign-in failed", e);

                            btnLogin.setEnabled(true);
                            showError(tvErrorPassword,
                                    "Sai mật khẩu hoặc tài khoản không hợp lệ!");
                        });

            })
            .addOnFailureListener(e -> {
                android.util.Log.e("LOGIN_DEBUG",
                        "Firestore query failed", e);

                btnLogin.setEnabled(true);
                Toast.makeText(this,
                        "Lỗi kết nối: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
}

    private void navigateToDashboardBasedOnRole(String role) {
        if (role == null) role = "user";

        Intent intent;
        switch (role.toLowerCase().trim()) {
            case "doctor":
                intent = new Intent(LoginActivity.this, MyScheduleActivity.class);
                break;
            case "admin":
                intent = new Intent(LoginActivity.this, AdminOverviewActivity.class);
                break;
            case "user":
            default:
                intent = new Intent(LoginActivity.this, MainActivity.class);
                break;
        }
        startActivity(intent);
        finish();
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
                    mAuth.signOut();
                });
    }
}