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

    private static final String FULL_MAP_URL = "https://lh3.googleusercontent.com/aida-public/AB6AXuCBM3GfSf4XeLhBw3yRGTsG8YAbkkWM31cdIpkiqmIhUO2x-nkApumb6TSklwSrT8lu4QV0Z4pGauJlJ8djhQiFoHk24CSFM-wdcTKddy9S_RAvi8JXIvX9pecvNBhQMtdExv3uvcfllzlylyiWWGxDZsgH879x3APmAEPEPT2UjXdcgSACMSmvG9phLaAFiqXTBW3FDo1WfntQUr481xUc5BKlFYbv8X6y23OrmEzhVogKo-WXlJeIggBBg1Hm2IbzzRjQKMh_P1Q2";
    private boolean isGpsSimulating = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_directions);

        // Load background map image
        ImageView imgFullMap = findViewById(R.id.imgFullMap);
        ImageLoader.load(FULL_MAP_URL, imgFullMap);

        // Setup back button
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Setup notifications
        ImageView btnNotification = findViewById(R.id.btnNotification);
        btnNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DirectionsActivity.this, "Bạn không có thông báo mới!", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup GPS Simulator Button mechanics
        final LinearLayout btnGPSAction = findViewById(R.id.btnGPSAction);
        final TextView txtGPSAction = findViewById(R.id.txtGPSAction);

        btnGPSAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGpsSimulating) {
                    // Open Google Maps external browser/app navigation
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("geo:10.7760,106.6990?q=123+Đường+Lê+Lợi,+Bến+Thành,+Quận+1"));
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(mapIntent);
                    } else {
                        // Open in web browser
                        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://www.google.com/maps/search/?api=1&query=123+Đường+Lê+Lợi,+Bến+Thành,+Quận+1"));
                        startActivity(webIntent);
                    }
                    return;
                }

                isGpsSimulating = true;
                txtGPSAction.setText("Đang tìm vị trí của bạn...");
                Toast.makeText(DirectionsActivity.this, "Đang tính toán tuyến đường tối ưu...", Toast.LENGTH_SHORT).show();

                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        txtGPSAction.setText("Mở Google Maps");
                        Toast.makeText(DirectionsActivity.this, "Vị trí đã được xác minh!", Toast.LENGTH_SHORT).show();
                    }
                }, 1500);
            }
        });
    }
}
