package com.example.ltmb_nhom11.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.ltmb_nhom11.MainActivity;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.util.SessionManager;
import com.example.ltmb_nhom11.model.Appointment;
import com.example.ltmb_nhom11.repository.AppointmentRepository;
import com.example.ltmb_nhom11.ui.adapter.AppointmentAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    private ImageButton btnMenuHistory;
    private MaterialCardView chipAll, chipUpcoming, chipDone, chipCancelled;
    private FloatingActionButton fabAddAppointment;
    private RecyclerView rvAppointments;
    private TextView tvEmpty;

    private AppointmentAdapter adapter;
    private final List<Appointment> allAppointments = new ArrayList<>();
    private String currentFilter = "Tất cả";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        btnMenuHistory = findViewById(R.id.btnMenuHistory);
        chipAll = findViewById(R.id.chipAll);
        chipUpcoming = findViewById(R.id.chipUpcoming);
        chipDone = findViewById(R.id.chipDone);
        chipCancelled = findViewById(R.id.chipCancelled);
        fabAddAppointment = findViewById(R.id.fabAddAppointment);
        rvAppointments = findViewById(R.id.rvAppointments);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvAppointments.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppointmentAdapter();
        adapter.setOnCancelClickListener(this::confirmCancel);
        adapter.setOnItemClickListener(appointment -> {
            if ("done".equals(appointment.getStatus()) || "completed".equals(appointment.getStatus())) {

                Intent intent = new Intent(HistoryActivity.this, MedicalSummaryActivity.class);
                intent.putExtra("appointmentId", appointment.getId());
                startActivity(intent);

            }
            else if ("upcoming".equals(appointment.getStatus())) {

                // Cách 1: Cho quay lại trang PaymentActivity để họ xem lại thông tin, mã QR nếu cần
                Intent intent = new Intent(HistoryActivity.this, PaymentActivity.class);

                intent.putExtra("packageName", appointment.getPackageName());
                intent.putExtra("doctorName", appointment.getDoctorName());
                intent.putExtra("date", appointment.getDate());
                intent.putExtra("time", appointment.getTime());
                intent.putExtra("price", (int) appointment.getPrice());
                intent.putExtra("packageId", appointment.getPackageId());
                intent.putExtra("doctorId", appointment.getDoctorId());

                startActivity(intent);

            }
        });
        rvAppointments.setAdapter(adapter);

        chipAll.setOnClickListener(v -> handleFilterTabChange("Tất cả", chipAll));
        chipUpcoming.setOnClickListener(v -> handleFilterTabChange("Sắp tới", chipUpcoming));
        chipDone.setOnClickListener(v -> handleFilterTabChange("Đã xong", chipDone));
        chipCancelled.setOnClickListener(v -> handleFilterTabChange("Đã hủy", chipCancelled));

        fabAddAppointment.setOnClickListener(v ->
                startActivity(new Intent(HistoryActivity.this, DoctorSearchActivity.class)));

        btnMenuHistory.setOnClickListener(v -> finish());

        setupBottomNav();
    }

    private void setupBottomNav() {
        LinearLayout navHome = findViewById(R.id.navHome);
        LinearLayout navAppointments = findViewById(R.id.navAppointments);
        LinearLayout navPackages = findViewById(R.id.navPackages);
        LinearLayout navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        navAppointments.setOnClickListener(v -> {});
        navPackages.setOnClickListener(v ->
                Toast.makeText(this, "Chức năng Gói khám đang được xây dựng!", Toast.LENGTH_SHORT).show());
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadAppointments();
    }

    private void loadAppointments() {
        String userId = SessionManager.getCurrentUser() != null
                ? SessionManager.getCurrentUser().getUid() : "test_user";

        new AppointmentRepository().getByUser(userId, new AppointmentRepository.OnList() {
            @Override
            public void onLoaded(List<Appointment> list) {
                allAppointments.clear();

                long now = System.currentTimeMillis();

                for (Appointment a : list) {
                    if ("upcoming".equals(a.getStatus())) {
                        long apptTime = getApptTimestamp(a);
                        if (apptTime > 0 && apptTime < now) {
                            a.setStatus("done");
                            if (a.getId() != null) {
                                new AppointmentRepository().updateStatus(a.getId(), "done", new AppointmentRepository.OnDone() {
                                    @Override public void onSuccess() {}
                                    @Override public void onError(Exception e) {}
                                });
                            }
                        }
                    }
                }

                Collections.sort(list, (a1, a2) -> {
                    String s1 = a1.getStatus() != null ? a1.getStatus() : "";
                    String s2 = a2.getStatus() != null ? a2.getStatus() : "";

                    int p1 = getStatusPriority(s1);
                    int p2 = getStatusPriority(s2);

                    if (p1 != p2) return Integer.compare(p1, p2);

                    long t1 = getApptTimestamp(a1);
                    long t2 = getApptTimestamp(a2);

                    if (p1 == 1) {
                        return Long.compare(t1, t2);
                    } else {
                        return Long.compare(t2, t1);
                    }
                });

                allAppointments.addAll(list);
                applyFilter();
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(HistoryActivity.this, "Lỗi tải lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private int getStatusPriority(String status) {
        if ("upcoming".equals(status)) return 1;
        if ("done".equals(status)) return 2;
        if ("cancelled".equals(status)) return 3;
        return 4;
    }
    private long getApptTimestamp(Appointment a) {
        try {
            String rawDate = a.getDate() != null ? a.getDate() : "";
            if (rawDate.contains(", ")) {
                String[] parts = rawDate.split(", ");
                rawDate = parts[parts.length - 1];
            }
            String rawTime = a.getTime() != null ? a.getTime() : "00:00";

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date d = sdf.parse(rawDate + " " + rawTime);
            return d != null ? d.getTime() : 0;
        } catch (Exception e) {
            return a.getCreatedAt();
        }
    }
    private static final String SUPPORT_PHONE = "0933652267";

    private void confirmCancel(@NonNull Appointment a) {
        String prefix = "Bạn có chắc muốn hủy đặt lịch?\n\nNếu đã đặt cọc trước, hãy liên hệ với chúng tôi qua số điện thoại ";
        String suffix = " để được hỗ trợ hoàn trả.";
        String full = prefix + SUPPORT_PHONE + suffix;

        SpannableString sp = new SpannableString(full);
        int start = prefix.length();
        int end = start + SUPPORT_PHONE.length();
        sp.setSpan(new ForegroundColorSpan(Color.parseColor("#00685F")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        sp.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                copyPhone();
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView message = new TextView(this);
        message.setText(sp);
        message.setMovementMethod(LinkMovementMethod.getInstance());
        message.setTextColor(Color.parseColor("#3D4947"));
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        int pad = Math.round(20 * getResources().getDisplayMetrics().density);
        message.setPadding(pad, pad, pad, 0);

        new AlertDialog.Builder(this)
                .setTitle("Hủy lịch khám")
                .setView(message)
                .setNegativeButton("Không", null)
                .setPositiveButton("Hủy lịch", (d, w) -> doCancel(a))
                .show();
    }
    private void copyPhone() {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            cm.setPrimaryClip(ClipData.newPlainText("phone", SUPPORT_PHONE));
            Toast.makeText(this, "Đã sao chép số điện thoại " + SUPPORT_PHONE, Toast.LENGTH_SHORT).show();
        }
    }

    private void doCancel(@NonNull Appointment a) {
        if (a.getId() == null) {
            Toast.makeText(this, "Không tìm thấy mã lịch hẹn.", Toast.LENGTH_SHORT).show();
            return;
        }
        new AppointmentRepository().updateStatus(a.getId(), "cancelled", new AppointmentRepository.OnDone() {
            @Override
            public void onSuccess() {
                Toast.makeText(HistoryActivity.this, "Đã hủy lịch khám.", Toast.LENGTH_SHORT).show();
                loadAppointments();
            }
            @Override
            public void onError(Exception e) {
                Toast.makeText(HistoryActivity.this, "Hủy lịch thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilter() {
        List<Appointment> filtered = new ArrayList<>();
        for (Appointment a : allAppointments) {
            String status = a.getStatus() != null ? a.getStatus() : "";
            switch (currentFilter) {
                case "Sắp tới":
                    if ("upcoming".equals(status)) filtered.add(a);
                    break;
                case "Đã xong":
                    if ("done".equals(status)) filtered.add(a);
                    break;
                case "Đã hủy":
                    if ("cancelled".equals(status)) filtered.add(a);
                    break;
                default:
                    filtered.add(a);
            }
        }
        adapter.setData(filtered);
        tvEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void handleFilterTabChange(String filterName, MaterialCardView selectedChip) {
        resetAllFilterChips();
        selectedChip.setCardBackgroundColor(getResources().getColor(R.color.colorPrimaryPrimary));
        selectedChip.setStrokeWidth(0);
        setChipTextColor(selectedChip, Color.WHITE);

        currentFilter = filterName;
        applyFilter();
    }

    private void resetAllFilterChips() {
        int white = getResources().getColor(android.R.color.white);
        int darkText = Color.parseColor("#3D4947");
        int strokeGray = Color.parseColor("#BCC9C6");
        int strokePx = Math.round(getResources().getDisplayMetrics().density);

        for (MaterialCardView chip : new MaterialCardView[]{chipAll, chipUpcoming, chipDone, chipCancelled}) {
            chip.setCardBackgroundColor(white);
            chip.setStrokeColor(strokeGray);
            chip.setStrokeWidth(strokePx);
            setChipTextColor(chip, darkText);
        }
    }

    private void setChipTextColor(MaterialCardView chip, int color) {
        if (chip.getChildCount() > 0 && chip.getChildAt(0) instanceof TextView) {
            ((TextView) chip.getChildAt(0)).setTextColor(color);
        }
    }
}