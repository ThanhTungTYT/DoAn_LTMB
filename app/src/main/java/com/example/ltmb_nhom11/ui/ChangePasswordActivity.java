package com.example.ltmb_nhom11.ui;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.ltmb_nhom11.R;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText edtOldPassword, edtNewPassword, edtConfirmPassword;
    private Button btnChangePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        ImageButton btnBack = findViewById(R.id.btnBackChangePw);
        edtOldPassword = findViewById(R.id.edtOldPassword);
        edtNewPassword = findViewById(R.id.edtNewPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);

        btnBack.setOnClickListener(v -> finish());
        btnChangePassword.setOnClickListener(v -> doChangePassword());

        setupPasswordToggle(edtOldPassword, findViewById(R.id.btnToggleOld));
        setupPasswordToggle(edtNewPassword, findViewById(R.id.btnToggleNew));
        setupPasswordToggle(edtConfirmPassword, findViewById(R.id.btnToggleConfirm));
    }

    /** Ẩn/hiện mật khẩu cho 1 ô (tái dùng cách dat làm ở Đăng nhập/Đăng ký). */
    private void setupPasswordToggle(EditText edt, ImageButton btn) {
        btn.setOnClickListener(v -> {
            boolean hidden = edt.getInputType()
                    == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            if (hidden) {
                edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btn.setImageResource(R.drawable.ic_visibility_off);
            } else {
                edt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btn.setImageResource(R.drawable.ic_visibility);
            }
            edt.setSelection(edt.getText().length());
        });
    }

    private void doChangePassword() {
        String oldPass = edtOldPassword.getText().toString().trim();
        String newPass = edtNewPassword.getText().toString().trim();
        String confirmPass = edtConfirmPassword.getText().toString().trim();

        // ===== Validation =====
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.length() < 6) {
            Toast.makeText(this, "Mật khẩu mới phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (newPass.equals(oldPass)) {
            Toast.makeText(this, "Mật khẩu mới phải khác mật khẩu hiện tại!", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        btnChangePassword.setEnabled(false);

        // ===== Xác thực lại mật khẩu cũ rồi mới đổi =====
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);
        user.reauthenticate(credential)
                .addOnSuccessListener(unused -> user.updatePassword(newPass)
                        .addOnSuccessListener(u -> {
                            Toast.makeText(this, "Đổi mật khẩu thành công!", Toast.LENGTH_LONG).show();
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            btnChangePassword.setEnabled(true);
                            Toast.makeText(this, "Lỗi đổi mật khẩu: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }))
                .addOnFailureListener(e -> {
                    btnChangePassword.setEnabled(true);
                    Toast.makeText(this, "Mật khẩu hiện tại không đúng!", Toast.LENGTH_SHORT).show();
                });
    }
}
