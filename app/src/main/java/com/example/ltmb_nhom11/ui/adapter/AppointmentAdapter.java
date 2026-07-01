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
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/** Hiển thị danh sách lịch hẹn trong màn Lịch sử khám. */
public class AppointmentAdapter extends RecyclerView.Adapter<AppointmentAdapter.VH> {

    public interface OnCancelClick {
        void onCancel(Appointment appointment);
    }

    // --- THÊM CHỖ NÀY: Định nghĩa Interface sự kiện Click vào dòng lịch hẹn ---
    public interface OnItemClickListener {
        void onItemClick(Appointment appointment);
    }

    private final List<Appointment> items = new ArrayList<>();
    private OnCancelClick cancelListener;
    private OnItemClickListener itemClickListener; // Biến lưu listener click dòng

    public void setOnCancelClickListener(OnCancelClick listener) {
        this.cancelListener = listener;
    }

    // --- THÊM CHỖ NÀY: Hàm để HistoryActivity truyền sự kiện click vào ---
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

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

        if ("package".equals(a.getType())) {
            h.tvDoctorName.setText(
                    a.getPackageName() != null
                            ? a.getPackageName()
                            : "Gói khám"
            );
        } else {
            h.tvDoctorName.setText(
                    a.getDoctorName() != null
                            ? a.getDoctorName()
                            : "Lịch khám"
            );
        }

        String time = a.getTime() != null ? a.getTime() : "";
        String date = a.getDate() != null ? a.getDate() : "";
        h.tvDateTime.setText((time + "  •  " + date).trim());

        String status = a.getStatus() != null ? a.getStatus() : "";
        String label;
        int color;
        switch (status) {
            case "upcoming":
                label = "Sắp tới"; color = Color.parseColor("#00685F"); break;
            case "done":
                label = "Đã xong"; color = Color.parseColor("#059669"); break;
            case "cancelled":
                label = "Đã hủy"; color = Color.parseColor("#BA1A1A"); break;
            default:
                label = status; color = Color.parseColor("#3D4947");
        }
        h.tvStatus.setText(label);
        h.tvStatus.setTextColor(color);
        h.viewAccent.setBackgroundColor(color);

        if ("upcoming".equals(status)) {
            h.btnCancel.setVisibility(View.VISIBLE);
            h.btnCancel.setOnClickListener(v -> {
                if (cancelListener != null) cancelListener.onCancel(a);
            });
        } else {
            h.btnCancel.setVisibility(View.GONE);
            h.btnCancel.setOnClickListener(null);
        }

        // --- THÊM CHỖ NÀY: Bắt sự kiện click vào trọn vẹn một item lịch hẹn ---
        h.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(a);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDoctorName, tvDateTime, tvStatus;
        View viewAccent;
        MaterialButton btnCancel;

        VH(@NonNull View itemView) {
            super(itemView);
            tvDoctorName = itemView.findViewById(R.id.tvDoctorName);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            viewAccent = itemView.findViewById(R.id.viewAccent);
            btnCancel = itemView.findViewById(R.id.btnCancelAppointment);
        }
    }
}