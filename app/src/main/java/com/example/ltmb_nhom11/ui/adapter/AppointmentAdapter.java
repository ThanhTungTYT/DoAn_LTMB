package com.example.ltmb_nhom11.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Appointment;

import java.util.ArrayList;
import java.util.List;

/** Hiển thị danh sách lịch hẹn trong màn Lịch sử khám. */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    private final List<Appointment> items = new ArrayList<>();

    public void setData(List<Appointment> data) {
        items.clear();
        if (data != null) items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_appointment, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Appointment a = items.get(position);

        h.tvDoctorName.setText(a.getDoctorName() != null ? a.getDoctorName() : "Lịch khám");

        String time = a.getTime() != null ? a.getTime() : "";
        String date = a.getDate() != null ? a.getDate() : "";
        h.tvDateTime.setText((time + "  •  " + date).trim());

        // Nhãn + màu theo trạng thái
        String status = a.getStatus() != null ? a.getStatus() : "";
        String label;
        int color;
        switch (status) {
            case "upcoming":
                label = "Sắp tới"; color = Color.parseColor("#00685F"); break;
            case "done":
                label = "Đã hoàn thành"; color = Color.parseColor("#059669"); break;
            case "cancelled":
                label = "Đã hủy"; color = Color.parseColor("#BA1A1A"); break;
            default:
                label = status; color = Color.parseColor("#3D4947");
        }
        h.tvStatus.setText(label);
        h.tvStatus.setTextColor(color);
        h.viewAccent.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDateTime, tvStatus;
        View viewAccent;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            viewAccent = itemView.findViewById(R.id.viewAccent);
        }
    }
}
