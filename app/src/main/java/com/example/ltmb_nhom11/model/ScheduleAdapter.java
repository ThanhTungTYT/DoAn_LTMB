package com.example.ltmb_nhom11.model;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.R;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(PatientSchedule item, int position);
    }

    private final List<PatientSchedule> items;
    private final OnItemClickListener listener;

    public ScheduleAdapter(List<PatientSchedule> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule_patient, parent, false);
        return new ScheduleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleViewHolder holder, int position) {
        PatientSchedule item = items.get(position);
        holder.tvTime.setText(item.getTime() + "\n" + item.getAmpm());
        holder.tvName.setText(item.getName());
        holder.tvDetail.setText(item.getDetail());
        holder.tvStatus.setText(item.getStatus());

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(item.getStatusBgColor());
        bg.setCornerRadius(20f);
        holder.tvStatus.setBackground(bg);
        holder.tvStatus.setTextColor(item.getStatusTextColor());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ScheduleViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvName, tvDetail, tvStatus;

        ScheduleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}