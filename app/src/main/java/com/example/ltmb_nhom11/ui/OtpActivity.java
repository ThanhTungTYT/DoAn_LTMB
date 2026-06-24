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
import java.util.Locale;
import com.example.ltmb_nhom11.R;


public class OtpActivity extends AppCompatActivity {

    private EditText[] otpFields = new EditText[6];
    private TextView tvOtpEmail, tvTimer;
    private Button btnResend, btnVerify;
    private ImageButton btnBack;
    private CountDownTimer countDownTimer;
    private long timeLeftInMillis = 59000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        // Ánh xạ các UI View từ Xml
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

        String userEmail = getIntent().getStringExtra("USER_EMAIL");
        if (userEmail != null && !userEmail.isEmpty()) {
            tvOtpEmail.setText(userEmail);
        }

        setupOtpFocusTraversal();

        startResendTimer();

        btnResend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(OtpActivity.this, "Một mã OTP mới đang được gởi lại...", Toast.LENGTH_SHORT).show();
                btnResend.setVisibility(View.GONE);
                tvTimer.setVisibility(View.VISIBLE);
                timeLeftInMillis = 59000;
                startResendTimer();
            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StringBuilder otpCode = new StringBuilder();
                for (EditText field : otpFields) {
                    otpCode.append(field.getText().toString());
                }

                if (otpCode.length() < 6) {
                    Toast.makeText(OtpActivity.this, "Vui lòng nhập trọn vẹn mã xác nhận 6 số!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(OtpActivity.this, "Mã kích hoạt " + otpCode.toString() + " hợp lệ! Đang chuẩn bị tạo tài khoản.", Toast.LENGTH_LONG).show();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupOtpFocusTraversal() {
        for (int i = 0; i < 6; i++) {
            final int currentIndex = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // Nếu đã ghi ký tự và không phải ô cuối, chuyển sang ô tiếp theo
                    if (s.length() == 1 && currentIndex < 5) {
                        otpFields[currentIndex + 1].requestFocus();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    // Nếu xoá ký tự (về rỗng) và không phải ô đầu tiên, lùi về ô trước
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
                updateCountDownText();
            }

            @Override
            public void onFinish() {
                tvTimer.setVisibility(View.GONE);
                btnResend.setVisibility(View.VISIBLE);
            }
        }.start();
    }

    private void updateCountDownText() {
        int seconds = (int) (timeLeftInMillis / 1000) % 60;
        String timeLeftFormatted = String.format(Locale.getDefault(), "Gửi lại mã sau 00:%02d", seconds);
        tvTimer.setText(timeLeftFormatted);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}