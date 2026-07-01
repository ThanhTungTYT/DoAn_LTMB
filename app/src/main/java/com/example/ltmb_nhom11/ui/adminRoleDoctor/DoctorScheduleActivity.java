package com.example.ltmb_nhom11.ui.adminRoleDoctor;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.DoctorScheduleAdapter;
import com.example.ltmb_nhom11.model.DoctorScheduleItem;
import com.example.ltmb_nhom11.ui.LoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class DoctorScheduleActivity extends AppCompatActivity {

    private static final String TAG = "DoctorSchedule";
    private static final TimeZone VN_TIMEZONE = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
    private RecyclerView rvDoctors;
    private TextView tvTotalDoctors, tvActiveCount, tvEmpty, tvSelectedDate;
    private EditText etSearch;
    private DoctorScheduleAdapter adapter;
    private final List<DoctorScheduleItem> fullList = new ArrayList<>();
    private final List<DoctorScheduleItem> filteredList = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doctor_schedule);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupSearch();
        setTodayDate();
        loadDoctors();
        setupBottomNavigation();

        findViewById(R.id.btnAddShift).setOnClickListener(v ->
                Toast.makeText(this, "Thêm ca làm mới", Toast.LENGTH_SHORT).show());
    }

    private void initViews() {
        rvDoctors       = findViewById(R.id.rvDoctors);
        tvTotalDoctors  = findViewById(R.id.tvTotalDoctors);
        tvActiveCount   = findViewById(R.id.tvActiveCount);
        tvEmpty         = findViewById(R.id.tvEmpty);
        tvSelectedDate  = findViewById(R.id.tvSelectedDate);
        etSearch        = findViewById(R.id.etSearch);
    }

    private void setTodayDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(VN_TIMEZONE);
        tvSelectedDate.setText(sdf.format(Calendar.getInstance(VN_TIMEZONE).getTime()));
    }

    private void setupRecyclerView() {
        adapter = new DoctorScheduleAdapter(filteredList,
                new DoctorScheduleAdapter.OnActionListener() {

                    @Override
                    public void onMarkLeave(DoctorScheduleItem item, int position) {
                        updateStatusOnFirestore(item, "On Leave", position);
                    }

                    @Override
                    public void onMarkAvailable(DoctorScheduleItem item, int position) {
                        updateStatusOnFirestore(item, "Sẵn sàng", position);
                    }

                    @Override
                    public void onActivateShift(DoctorScheduleItem item, int position) {
                        updateStatusOnFirestore(item, "Sẵn sàng", position);
                    }

                    @Override
                    public void onEdit(DoctorScheduleItem item, int position) {
                        Toast.makeText(DoctorScheduleActivity.this,
                                "Chỉnh sửa: " + item.getFullName(),
                                Toast.LENGTH_SHORT).show();
                    }
                });

        rvDoctors.setLayoutManager(new LinearLayoutManager(this));
        rvDoctors.setAdapter(adapter);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterList(s.toString().trim());
            }
        });
    }

    private void filterList(String keyword) {
        filteredList.clear();
        if (keyword.isEmpty()) {
            filteredList.addAll(fullList);
        } else {
            String kw = keyword.toLowerCase();
            for (DoctorScheduleItem item : fullList) {
                boolean matchName = item.getFullName() != null
                        && item.getFullName().toLowerCase().contains(kw);
                boolean matchDept = item.getDept() != null
                        && item.getDept().toLowerCase().contains(kw);
                if (matchName || matchDept) filteredList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void loadDoctors() {
        String todayStr = getTodayString(); // "02/07/2026"

        db.collection("users")
                .whereEqualTo("role", "doctor")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    fullList.clear();

                    if (querySnapshot.isEmpty()) {
                        refreshUI();
                        return;
                    }

                    int total = querySnapshot.size();
                    AtomicInteger counter = new AtomicInteger(0);

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        DoctorScheduleItem item = new DoctorScheduleItem();
                        item.setUid(doc.getId());

                        String fullName = doc.getString("fullName");
                        String name     = doc.getString("name");
                        item.setFullName(fullName != null ? fullName : name);

                        item.setDept(doc.getString("dept"));
                        item.setAvatarUrl(doc.getString("avatarUrl"));

                        String rawStatus = doc.getString("status");
                        item.setStatus(rawStatus != null ? rawStatus : "Off Duty");

                        item.setTotalSlots(6);
                        item.setTimeSlot("08:00 - 16:00");

                        final String doctorId = doc.getId();
                        db.collection("appointments")
                                .whereEqualTo("doctorId", doctorId)
                                .get()
                                .addOnSuccessListener(apptSnap -> {
                                    int countToday = 0;
                                    for (QueryDocumentSnapshot appt : apptSnap) {
                                        String date = appt.getString("date");
                                        if (date != null && date.contains(todayStr)) {
                                            countToday++;
                                        }
                                    }
                                    item.setAppointmentsToday(countToday);
                                    fullList.add(item);

                                    if (counter.incrementAndGet() == total) {
                                        sortAndRefreshUI();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    item.setAppointmentsToday(0);
                                    fullList.add(item);
                                    if (counter.incrementAndGet() == total) {
                                        sortAndRefreshUI();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadDoctors error: " + e.getMessage());
                    Toast.makeText(this,
                            "Không tải được danh sách bác sĩ.", Toast.LENGTH_SHORT).show();
                });
    }

    private void sortAndRefreshUI() {
        fullList.sort((a, b) ->
                Integer.compare(statusRank(a.getStatus()), statusRank(b.getStatus())));
        refreshUI();
    }

    private int statusRank(String status) {
        if ("Sẵn sàng".equals(status)) return 0;
        if ("On Leave".equals(status))  return 1;
        return 2;
    }

    private void refreshUI() {
        int totalDocs  = fullList.size();
        int activeDocs = 0;
        for (DoctorScheduleItem item : fullList) {
            if ("Sẵn sàng".equals(item.getStatus())) activeDocs++;
        }
        tvTotalDoctors.setText(String.valueOf(totalDocs));
        tvActiveCount.setText(String.valueOf(activeDocs));

        filteredList.clear();
        filteredList.addAll(fullList);
        adapter.notifyDataSetChanged();

        tvEmpty.setVisibility(filteredList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void updateStatusOnFirestore(DoctorScheduleItem item,
                                         String newStatus, int position) {
        db.collection("users").document(item.getUid())
                .update("status", newStatus)
                .addOnSuccessListener(aVoid -> {
                    item.setStatus(newStatus);
                    adapter.notifyItemChanged(position);

                    int activeDocs = 0;
                    for (DoctorScheduleItem d : fullList) {
                        if ("Sẵn sàng".equals(d.getStatus())) activeDocs++;
                    }
                    tvActiveCount.setText(String.valueOf(activeDocs));

                    Toast.makeText(this,
                            "Cập nhật trạng thái thành công!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "updateStatus error: " + e.getMessage());
                    Toast.makeText(this, "Cập nhật thất bại.", Toast.LENGTH_SHORT).show();
                });
    }

    private String getTodayString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(VN_TIMEZONE);
        return sdf.format(Calendar.getInstance(VN_TIMEZONE).getTime());
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_doctors);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_dashboard) {
                Intent intent = new Intent(this, AdminOverviewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            else if (id == R.id.nav_doctors) {
                return true;
            }
            else if (id == R.id.nav_logout) {
                mAuth.signOut();

                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                return true;
            }
            return false;
        });
    }
}