package com.example.ltmb_nhom11.model;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.ltmb_nhom11.R;

import java.util.List;

public class DoctorScheduleAdapter
        extends RecyclerView.Adapter<DoctorScheduleAdapter.ViewHolder> {

    public interface OnActionListener {
        void onMarkLeave(DoctorScheduleItem item, int position);
        void onMarkAvailable(DoctorScheduleItem item, int position);
        void onActivateShift(DoctorScheduleItem item, int position);
        void onEdit(DoctorScheduleItem item, int position);
    }

    private List<DoctorScheduleItem> items;
    private final OnActionListener listener;

    public DoctorScheduleAdapter(List<DoctorScheduleItem> items, OnActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateList(List<DoctorScheduleItem> newList) {
        this.items = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor_schedule, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        DoctorScheduleItem item = items.get(position);

        // Avatar — dùng Glide giống MyScheduleActivity
        if (item.getAvatarUrl() != null && !item.getAvatarUrl().isEmpty()) {
            Glide.with(h.imgAvatar.getContext())
                    .load(item.getAvatarUrl())
                    .circleCrop()
                    .placeholder(android.R.drawable.ic_menu_myplaces)
                    .into(h.imgAvatar);
        } else {
            h.imgAvatar.setImageResource(android.R.drawable.ic_menu_myplaces);
        }

        h.tvName.setText(item.getFullName() != null ? item.getFullName() : "Bác sĩ");
        h.tvDept.setText(item.getDept() != null ? item.getDept().toUpperCase() : "");

        String status = item.getStatus() != null ? item.getStatus() : "";
        bindStatus(h, item, status);

        h.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(item, h.getAdapterPosition());
        });
    }

    /**
     * Áp dụng UI theo trạng thái — giống cách updateSlotUI() trong MyScheduleActivity
     * "Sẵn sàng" → Available (xanh lá) | "On Leave" → đỏ | còn lại → Off Duty (xám)
     */
    private void bindStatus(ViewHolder h, DoctorScheduleItem item, String status) {
        switch (status) {
            case "Sẵn sàng":
                setDotColor(h.viewStatusDot, "#4CAF50");
                h.tvStatus.setText("Available");
                h.tvStatus.setTextColor(Color.parseColor("#4CAF50"));

                h.tvTimeSlot.setVisibility(View.VISIBLE);
                h.tvTimeSlot.setText(item.getTimeSlot() != null ? item.getTimeSlot() : "");

                h.tvLeaveNote.setVisibility(View.GONE);

                h.layoutProgress.setVisibility(View.VISIBLE);
                h.progressDoctor.setVisibility(View.VISIBLE);
                int percent = item.getProgressPercent();
                h.tvProgressLabel.setText("Appointments");
                h.tvProgressPercent.setText(item.getAppointmentsToday()
                        + "/" + item.getTotalSlots());
                h.progressDoctor.setProgress(percent);
                setProgressColor(h.progressDoctor, "#4CAF50");

                h.btnAction.setText("MARK LEAVE");
                h.btnAction.setBackgroundResource(R.drawable.bg_btn_outline);
                h.btnAction.setTextColor(Color.parseColor("#1565C0"));
                h.btnAction.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onMarkLeave(item, h.getAdapterPosition());
                });
                break;

            case "On Leave":
                setDotColor(h.viewStatusDot, "#F44336");
                h.tvStatus.setText("On Leave");
                h.tvStatus.setTextColor(Color.parseColor("#F44336"));

                h.tvTimeSlot.setVisibility(View.GONE);

                h.tvLeaveNote.setVisibility(View.VISIBLE);
                h.tvLeaveNote.setText(item.getLeaveNote() != null
                        ? item.getLeaveNote() : "Đang nghỉ phép");

                h.layoutProgress.setVisibility(View.GONE);
                h.progressDoctor.setVisibility(View.GONE);

                h.btnAction.setText("MARK AVAILABLE");
                h.btnAction.setBackgroundResource(R.drawable.bg_btn_filled);
                h.btnAction.setTextColor(Color.WHITE);
                h.btnAction.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onMarkAvailable(item, h.getAdapterPosition());
                });
                break;

            default: // Off Duty hoặc trạng thái khác
                setDotColor(h.viewStatusDot, "#9AA0A6");
                h.tvStatus.setText("Off Duty");
                h.tvStatus.setTextColor(Color.parseColor("#9AA0A6"));

                h.tvTimeSlot.setVisibility(View.GONE);

                h.tvLeaveNote.setVisibility(View.VISIBLE);
                h.tvLeaveNote.setText(item.getLeaveNote() != null
                        ? item.getLeaveNote() : "Không có ca làm hôm nay");

                h.layoutProgress.setVisibility(View.GONE);
                h.progressDoctor.setVisibility(View.GONE);

                h.btnAction.setText("ACTIVATE SHIFT");
                h.btnAction.setBackgroundResource(R.drawable.bg_btn_filled);
                h.btnAction.setTextColor(Color.WHITE);
                h.btnAction.setOnClickListener(v -> {
                    if (listener != null)
                        listener.onActivateShift(item, h.getAdapterPosition());
                });
                break;
        }
    }

    private void setDotColor(View dot, String hex) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(Color.parseColor(hex));
        dot.setBackground(gd);
    }

    private void setProgressColor(ProgressBar bar, String hex) {
        LayerDrawable ld = (LayerDrawable) bar.getProgressDrawable();
        ld.findDrawableByLayerId(android.R.id.progress)
                .setColorFilter(Color.parseColor(hex), android.graphics.PorterDuff.Mode.SRC_IN);
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvDept, tvStatus, tvTimeSlot, tvLeaveNote;
        TextView tvProgressLabel, tvProgressPercent;
        View viewStatusDot, layoutProgress;
        ProgressBar progressDoctor;
        Button btnAction;
        ImageButton btnEdit;

        ViewHolder(@NonNull View v) {
            super(v);
            imgAvatar      = v.findViewById(R.id.imgDoctorAvatar);
            tvName         = v.findViewById(R.id.tvDoctorName);
            tvDept         = v.findViewById(R.id.tvDoctorDept);
            tvStatus       = v.findViewById(R.id.tvStatus);
            tvTimeSlot     = v.findViewById(R.id.tvTimeSlot);
            tvLeaveNote    = v.findViewById(R.id.tvLeaveNote);
            viewStatusDot  = v.findViewById(R.id.viewStatusDot);
            tvProgressLabel   = v.findViewById(R.id.tvProgressLabel);
            tvProgressPercent = v.findViewById(R.id.tvProgressPercent);
            progressDoctor = v.findViewById(R.id.progressDoctor);
            layoutProgress = v.findViewById(R.id.llProgressRow);
            btnAction      = v.findViewById(R.id.btnAction);
            btnEdit        = v.findViewById(R.id.btnEdit);
        }
    }
}