package com.example.ltmb_nhom11.ui;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import com.example.ltmb_nhom11.BuildConfig;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Appointment;
import com.example.ltmb_nhom11.repository.AppointmentRepository;
import com.example.ltmb_nhom11.util.SessionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PaymentActivity extends AppCompatActivity {

    // Tài khoản nhận tiền (VietQR)
    private static final String BANK_ID = "mbbank";
    private static final String ACCOUNT_NO = "0933652267";
    private static final String ACCOUNT_NAME = "NGUYEN HUY BAO";
    private static final long DEPOSIT = 200000;

    private static final String SEPAY_API = "https://my.sepay.vn/userapi/transactions/list?limit=20";
    private static final long POLL_INTERVAL_MS = 3000;   // 3s/lần
    private static final long POLL_TIMEOUT_MS = 30000;   // tối đa 30s

    private ImageButton btnBackPayment;
    private MaterialCardView cardPayDirect, cardPayBank;
    private RadioButton radioDirect, radioBank;
    private View layoutQrSection;
    private ImageView imgQr;
    private TextView tvQrContent;
    private MaterialButton btnConfirmPayment;

    private boolean bankSelected = true;
    private String transferCode;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final OkHttpClient httpClient = new OkHttpClient();
    private boolean isChecking = false;
    private long pollStart;
    private AlertDialog checkingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        transferCode = "HC" + (System.currentTimeMillis() % 1000000);

        initViews();
        bindAppointmentInfo();
        buildQr();

        btnBackPayment.setOnClickListener(v -> finish());
        findViewById(R.id.btnNotificationsPayment).setOnClickListener(v ->
                Toast.makeText(this, "Không có thông báo mới", Toast.LENGTH_SHORT).show());

        cardPayDirect.setOnClickListener(v -> selectDirect());
        radioDirect.setOnClickListener(v -> selectDirect());
        cardPayBank.setOnClickListener(v -> selectBank());
        radioBank.setOnClickListener(v -> selectBank());

        selectBank();

        btnConfirmPayment.setOnClickListener(v -> {
            if (bankSelected) startSepayPolling();
            else saveAppointment();
        });
    }

    private void initViews() {
        btnBackPayment = findViewById(R.id.btnBackPayment);
        cardPayDirect = findViewById(R.id.cardPayDirect);
        cardPayBank = findViewById(R.id.cardPayBank);
        radioDirect = findViewById(R.id.radioDirect);
        radioBank = findViewById(R.id.radioBank);
        layoutQrSection = findViewById(R.id.layoutQrSection);
        imgQr = findViewById(R.id.imgQr);
        tvQrContent = findViewById(R.id.tvQrContent);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
    }

    private void bindAppointmentInfo() {
        String doctorName = getIntent().getStringExtra("doctorName");
        String date = getIntent().getStringExtra("selected_date");
        String time = getIntent().getStringExtra("selected_time");

        if (doctorName != null && !doctorName.isEmpty()) {
            ((TextView) findViewById(R.id.tvPayDoctorName)).setText(doctorName);
        }
        String dt = ((time != null ? time : "") + "  •  " + (date != null ? date : "")).trim();
        if (!dt.equals("•")) {
            ((TextView) findViewById(R.id.tvPayDateTime)).setText(dt);
        }
        String formatted = formatPrice(DEPOSIT);
        ((TextView) findViewById(R.id.tvPayTotal)).setText(formatted);
        ((TextView) findViewById(R.id.tvPayTotalBottom)).setText(formatted);
    }

    private void buildQr() {
        String url = "https://img.vietqr.io/image/" + BANK_ID + "-" + ACCOUNT_NO + "-compact2.png"
                + "?amount=" + DEPOSIT
                + "&addInfo=" + Uri.encode(transferCode)
                + "&accountName=" + Uri.encode(ACCOUNT_NAME);
        imgQr.setImageTintList(null);
        ImageLoader.load(url, imgQr);
        tvQrContent.setText(transferCode);
    }

    private void selectBank() {
        bankSelected = true;
        radioBank.setChecked(true);
        radioDirect.setChecked(false);
        styleOption(cardPayBank, true);
        styleOption(cardPayDirect, false);
        layoutQrSection.setVisibility(View.VISIBLE);
        btnConfirmPayment.setText("Tôi đã chuyển khoản");
    }

    private void selectDirect() {
        bankSelected = false;
        radioDirect.setChecked(true);
        radioBank.setChecked(false);
        styleOption(cardPayDirect, true);
        styleOption(cardPayBank, false);
        layoutQrSection.setVisibility(View.GONE);
        btnConfirmPayment.setText("Xác nhận đặt lịch");
    }

    private void styleOption(MaterialCardView card, boolean selected) {
        if (selected) {
            card.setCardBackgroundColor(Color.parseColor("#F0FDFA"));
            card.setStrokeColor(Color.parseColor("#00685F"));
            card.setStrokeWidth(dp(1.5f));
        } else {
            card.setCardBackgroundColor(Color.WHITE);
            card.setStrokeColor(Color.parseColor("#E2E8F0"));
            card.setStrokeWidth(dp(1f));
        }
    }

    // ===== SePay: tự kiểm tra liên tục tối đa 30s =====

    private void startSepayPolling() {
        if (BuildConfig.SEPAY_TOKEN == null || BuildConfig.SEPAY_TOKEN.isEmpty()) {
            Toast.makeText(this, "Chưa cấu hình SEPAY_TOKEN trong local.properties", Toast.LENGTH_LONG).show();
            return;
        }
        isChecking = true;
        pollStart = System.currentTimeMillis();
        btnConfirmPayment.setEnabled(false);
        showCheckingDialog();
        pollOnce();
    }

    private void pollOnce() {
        if (!isChecking) return;
        Request req = new Request.Builder()
                .url(SEPAY_API)
                .addHeader("Authorization", "Bearer " + BuildConfig.SEPAY_TOKEN)
                .get()
                .build();

        httpClient.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Lỗi mạng tạm thời -> cứ thử lại đến khi hết 30s
                runOnUiThread(PaymentActivity.this::scheduleNextOrTimeout);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                boolean found = isTransactionMatched(body);
                runOnUiThread(() -> {
                    if (!isChecking) return;
                    if (found) {
                        finishChecking();
                        saveAppointment();
                    } else {
                        scheduleNextOrTimeout();
                    }
                });
            }
        });
    }

    private void scheduleNextOrTimeout() {
        if (!isChecking) return;
        if (System.currentTimeMillis() - pollStart >= POLL_TIMEOUT_MS) {
            finishChecking();
            btnConfirmPayment.setEnabled(true);
            btnConfirmPayment.setText("Tôi đã chuyển khoản");
            Toast.makeText(this,
                    "Chưa nhận được chuyển khoản sau 30 giây. Hãy chuyển đúng " + formatPrice(DEPOSIT)
                            + " với nội dung \"" + transferCode + "\" rồi bấm lại.",
                    Toast.LENGTH_LONG).show();
        } else {
            handler.postDelayed(this::pollOnce, POLL_INTERVAL_MS);
        }
    }

    private boolean isTransactionMatched(String body) {
        try {
            JSONArray arr = new JSONObject(body).optJSONArray("transactions");
            if (arr == null) return false;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject t = arr.getJSONObject(i);
                String content = t.optString("transaction_content", "").toUpperCase();
                double amountIn = parseAmount(t.optString("amount_in", "0"));
                if (content.contains(transferCode.toUpperCase()) && amountIn >= DEPOSIT) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    /** Hộp thoại chặn thao tác (không cho thoát) trong lúc kiểm tra. */
    private void showCheckingDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        int pad = dp(24);
        layout.setPadding(pad, pad, pad, pad);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        ProgressBar pb = new ProgressBar(this);

        TextView tv = new TextView(this);
        tv.setText("Đang kiểm tra giao dịch (tối đa 30s)…\nVui lòng không thoát màn hình.");
        tv.setTextColor(Color.parseColor("#0F172A"));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = dp(16);
        tv.setLayoutParams(lp);

        layout.addView(pb);
        layout.addView(tv);

        checkingDialog = new AlertDialog.Builder(this)
                .setView(layout)
                .setCancelable(false)   // bấm back không tắt được -> chặn thoát
                .create();
        checkingDialog.setCanceledOnTouchOutside(false);
        checkingDialog.show();
    }

    private void finishChecking() {
        isChecking = false;
        handler.removeCallbacksAndMessages(null);
        if (checkingDialog != null && checkingDialog.isShowing()) {
            checkingDialog.dismiss();
        }
    }

    private void saveAppointment() {
        String userId = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser().getUid() : "test_user";

        Appointment a = new Appointment(
                userId,
                "doctor",
                getIntent().getStringExtra("doctorId"),
                getIntent().getStringExtra("doctorName"),
                getIntent().getStringExtra("selected_date"),
                getIntent().getStringExtra("selected_time"),
                DEPOSIT,
                "upcoming"
        );

        btnConfirmPayment.setEnabled(false);
        new AppointmentRepository().create(a, new AppointmentRepository.OnDone() {
            @Override public void onSuccess() {
                String method = bankSelected ? "Đã nhận chuyển khoản đặt cọc" : "Thanh toán trực tiếp tại phòng khám";
                Toast.makeText(PaymentActivity.this, "Đặt lịch thành công! (" + method + ")", Toast.LENGTH_LONG).show();
                startActivity(new Intent(PaymentActivity.this, HistoryActivity.class));
                finish();
            }
            @Override public void onError(Exception e) {
                btnConfirmPayment.setEnabled(true);
                btnConfirmPayment.setText(bankSelected ? "Tôi đã chuyển khoản" : "Xác nhận đặt lịch");
                Toast.makeText(PaymentActivity.this, "Lỗi lưu lịch: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finishChecking();
    }

    private int dp(float value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }

    private double parseAmount(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0; }
    }

    private String formatPrice(long price) {
        return String.format("%,d", price).replace(",", ".") + "đ";
    }
}
