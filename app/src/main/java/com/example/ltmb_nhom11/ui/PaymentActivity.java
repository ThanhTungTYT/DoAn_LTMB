package com.example.ltmb_nhom11.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.google.android.material.button.MaterialButton;

import com.example.ltmb_nhom11.R;

public class PaymentActivity extends AppCompatActivity {

    private ImageButton btnBackPayment;
    private CardView cardPayAtClinic, cardPayOnline;
    private RadioButton radioAtClinic, radioOnline;
    private LinearLayout btnMomo, btnATM;
    private MaterialButton btnConfirmPayment;

    private boolean isOnlinePaymentSelected = false;
    private String selectedMethodDetails = "Trực tiếp phòng khám";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        initViews();

        btnBackPayment.setOnClickListener(v -> finish());

        // Lắng nghe sự kiện RadioButton & Card click
        setupPaymentSelections();

        // Nút Thanh toán cuối cùng
        btnConfirmPayment.setOnClickListener(v -> {
            String selectedSummary = isOnlinePaymentSelected ? "Online qua: " + selectedMethodDetails : "Thanh toán trực tiếp tại bệnh viện";
            Toast.makeText(PaymentActivity.this, "Đặt lịch thành công! " + selectedSummary, Toast.LENGTH_LONG).show();

            // Xử lý logic nghiệp vụ, lưu cơ sở dữ liệu cloud Firestore hoặc backend API
            // Mở màn hình lịch sử khám bệnh
        });
    }

    private void initViews() {
        btnBackPayment = findViewById(R.id.btnBackPayment);
        cardPayAtClinic = findViewById(R.id.cardPayAtClinic);
        cardPayOnline = findViewById(R.id.cardPayOnline);
        radioAtClinic = findViewById(R.id.radioAtClinic);
        radioOnline = findViewById(R.id.radioOnline);

        btnMomo = findViewById(R.id.btnMomo);
        btnATM = findViewById(R.id.btnATM);
        btnConfirmPayment = findViewById(R.id.btnConfirmPayment);
    }

    private void setupPaymentSelections() {
        // Click lựa chọn thanh toán tại phòng khám
        View.OnClickListener clickClinic = v -> {
            isOnlinePaymentSelected = false;
            radioAtClinic.setChecked(true);
            radioOnline.setChecked(false);

            // Cập nhật background cards trực quan
            cardPayAtClinic.setCardBackgroundColor(getResources().getColor(R.color.tealLightestBg));
            cardPayOnline.setCardBackgroundColor(getResources().getColor(android.R.color.white));

            // Tắt bỏ highlight tiểu mục MoMo/ATM
            resetSubOptionsColors();
        };
        cardPayAtClinic.setOnClickListener(clickClinic);
        radioAtClinic.setOnClickListener(clickClinic);

        // Click lựa chọn thanh toán online chung
        View.OnClickListener clickOnlineGroup = v -> {
            isOnlinePaymentSelected = true;
            radioAtClinic.setChecked(false);
            radioOnline.setChecked(true);

            cardPayAtClinic.setCardBackgroundColor(getResources().getColor(android.R.color.white));
            cardPayOnline.setCardBackgroundColor(getResources().getColor(R.color.blueLightestBg));
        };
        layoutOnlineHeaderClickable(); // Cho phép click dòng header Thanh Toán Online

        // Lựa chọn các ví trực tiếp con dưới online
        btnMomo.setOnClickListener(v -> {
            clickOnlineGroup.onClick(null);
            selectedMethodDetails = "Ví MoMo";
            resetSubOptionsColors();
            btnMomo.setBackgroundResource(R.drawable.bg_subpay_btn_selected);
            Toast.makeText(this, "Đã chọn thanh toán qua Ví MoMo", Toast.LENGTH_SHORT).show();
        });

        btnATM.setOnClickListener(v -> {
            clickOnlineGroup.onClick(null);
            selectedMethodDetails = "Thẻ ATM / Bank";
            resetSubOptionsColors();
            btnATM.setBackgroundResource(R.drawable.bg_subpay_btn_selected);
            Toast.makeText(this, "Đã chọn thanh toán qua Thẻ ngân hàng nội địa", Toast.LENGTH_SHORT).show();
        });
    }

    private void layoutOnlineHeaderClickable() {
        findViewById(R.id.layoutOnlineHeader).setOnClickListener(v -> {
            isOnlinePaymentSelected = true;
            radioAtClinic.setChecked(false);
            radioOnline.setChecked(true);
            cardPayAtClinic.setCardBackgroundColor(getResources().getColor(android.R.color.white));
            cardPayOnline.setCardBackgroundColor(getResources().getColor(R.color.blueLightestBg));
        });
    }

    private void resetSubOptionsColors() {
        btnMomo.setBackgroundResource(R.drawable.bg_subpay_btn);
        btnATM.setBackgroundResource(R.drawable.bg_subpay_btn);
    }
}