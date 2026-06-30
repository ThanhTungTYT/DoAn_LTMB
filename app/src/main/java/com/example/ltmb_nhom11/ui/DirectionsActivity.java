package com.example.ltmb_nhom11.ui;

import com.example.ltmb_nhom11.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DirectionsActivity extends AppCompatActivity {

    private static final String FULL_MAP_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuCBM3GfSf4XeLhBw3yRGTsG8YAbkkWM31cdIpkiqmIhUO2x-nkApumb6TSklwSrT8lu4QV0Z4pGauJlJ8djhQiFoHk24CSFM-wdcTKddy9S_RAvi8JXIvX9pecvNBhQMtdExv3uvcfllzlylyiWWGxDZsgH879x3APmAEPEPT2UjXdcgSACMSmvG9phLaAFiqXTBW3FDo1WfntQUr481xUc5BKlFYbv8X6y23OrmEzhVogKo-WXlJeIggBBg1Hm2IbzzRjQKMh_P1Q2"; // Đổi thành link ảnh thật nếu có
    private boolean isGpsSimulating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);


        ImageView imgFullMap = findViewById(R.id.imgFullMap);
        ImageLoader.load(FULL_MAP_URL, imgFullMap);


        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());




        final LinearLayout btnGPSAction = findViewById(R.id.btnGPSAction);
        final TextView txtGPSAction = findViewById(R.id.txtGPSAction);

        btnGPSAction.setOnClickListener(v -> {

            if (isGpsSimulating) {

                String geoUri = "geo:10.7760,106.6990?q=123+Đường+Lê+Lợi,+Bến+Thành,+Quận+1";
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                mapIntent.setPackage("com.google.android.apps.maps"); // Ép mở bằng app Google Maps


                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {

                    Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=123+Le+Loi,+Ben+Thanh,+Quan+1"));
                    startActivity(webIntent);
                }
                return;
            }


            isGpsSimulating = true;
            txtGPSAction.setText("Đang tìm vị trí của bạn...");
            Toast.makeText(DirectionsActivity.this, "Đang tính toán tuyến đường tối ưu...", Toast.LENGTH_SHORT).show();


            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                txtGPSAction.setText("Mở Google Maps");
                Toast.makeText(DirectionsActivity.this, "Vị trí đã được xác minh!", Toast.LENGTH_SHORT).show();
            }, 1500); // 1500ms = 1.5 giây
        });
    }
}