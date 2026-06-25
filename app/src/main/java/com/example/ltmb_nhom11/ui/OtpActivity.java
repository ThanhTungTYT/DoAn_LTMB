package com.example.ltmb_nhom11.ui;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ltmb_nhom11.R;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class OtpActivity extends AppCompatActivity {

    private EditText[] otpFields = new EditText[6];
    private TextView tvOtpEmail, tvTimer;
    private Button btnResend, btnVerify;
    private ImageButton btnBack;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 59000;
    private String userEmail;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        db = FirebaseFirestore.getInstance();

        tvOtpEmail = findViewById(R.id.tvOtpEmail);
        tvTimer = findViewById(R.id.tvTimer);
        btnResend = findViewById(R.id.btnResend);
        btnVerify = findViewById(R.id.btnVerify);
        btnBack = findViewById(R.id.btnBack);

        otpFields[0] = findViewById(R.id.otpDigit1);
        otpFields[1] = findViewById(R.id.otpDigit2);
        otpFields[2] = findViewById(R.id.otpDigit3);
        otpFields[3] = findViewById(R.id.otpDigit4);
        otpFields[4] = findViewById(R.id.otpDigit5);
        otpFields[5] = findViewById(R.id.otpDigit1_6);

        userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail != null && !userEmail.isEmpty()) {
            tvOtpEmail.setText(userEmail);
        }

        setupOtpFocusTraversal();
        startResendTimer();

        btnResend.setOnClickListener(v -> resendOtp());
        btnVerify.setOnClickListener(v -> verifyOtp());
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void verifyOtp() {
        StringBuilder otpCode = new StringBuilder();
        for (EditText field : otpFields) {
            otpCode.append(field.getText().toString());
        }

        if (otpCode.length() < 6) {
            Toast.makeText(this, "Vui lòng nhập trọn vẹn mã xác nhận 6 số!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnVerify.setEnabled(false);

        db.collection("otp_codes").document(userEmail).get()
                .addOnSuccessListener(doc -> {
                    btnVerify.setEnabled(true);
                    if (!doc.exists()) {
                        Toast.makeText(this, "Không tìm thấy mã OTP, vui lòng gửi lại!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String savedCode = doc.getString("code");
                    Long expiresAt = doc.getLong("expiresAt");
                    String uid = doc.getString("uid");

                    if (expiresAt == null || System.currentTimeMillis() > expiresAt) {
                        Toast.makeText(this, "Mã OTP đã hết hạn, vui lòng gửi lại!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!otpCode.toString().equals(savedCode)) {
                        Toast.makeText(this, "Mã OTP không đúng!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // OTP đúng -> đánh dấu verified trong Firestore + xoá mã
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("verified", true);

                    db.collection("users").document(uid).update(updates)
                            .addOnSuccessListener(unused -> {
                                db.collection("otp_codes").document(userEmail).delete();
                                Toast.makeText(this, "Xác thực thành công! Tài khoản đã được tạo.", Toast.LENGTH_LONG).show();
                                navigateToMain();
                            });
                })
                .addOnFailureListener(e -> {
                    btnVerify.setEnabled(true);
                    Toast.makeText(this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void resendOtp() {
        String code = String.valueOf(100000 + new Random().nextInt(900000));
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("code", code);
        otpData.put("expiresAt", System.currentTimeMillis() + 5 * 60 * 1000);

        db.collection("otp_codes").document(userEmail).update(otpData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Mã OTP mới: " + code, Toast.LENGTH_LONG).show(); // demo only
                    btnResend.setVisibility(View.GONE);
                    tvTimer.setVisibility(View.VISIBLE);
                    timeLeftInMillis = 59000;
                    startResendTimer();
                });
    }

    private void navigateToMain() {
        // chuyển sang LoginActivity hoặc MainActivity tuỳ flow của bạn
        finishAffinity();
    }

    private void setupOtpFocusTraversal() {
        for (int i = 0; i < 6; i++) {
            final int currentIndex = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s.length() == 1 && currentIndex < 5) {
                        otpFields[currentIndex + 1].requestFocus();
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0 && currentIndex > 0) {
                        otpFields[currentIndex - 1].requestFocus();
                    }
                }
            });
        }
    }

    private void startResendTimer() {
        countDownTimer = new CountDownTimer(timeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeftInMillis = millisUntilFinished;
                int seconds = (int) (timeLeftInMillis / 1000) % 60;
                tvTimer.setText(String.format(Locale.getDefault(), "Gửi lại mã sau 00:%02d", seconds));
            }
            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                btnResend.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}