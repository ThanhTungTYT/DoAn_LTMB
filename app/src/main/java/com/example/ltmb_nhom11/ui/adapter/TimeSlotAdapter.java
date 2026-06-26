package com.example.ltmb_nhom11.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.TimeSlot;

import java.util.List;

public class TimeSlotAdapter extends RecyclerView.Adapter<TimeSlotAdapter.ViewHolder> {

    public interface OnTimeClickListener {
        void onClick(TimeSlot slot);
    }

    private final List<TimeSlot> list;
    private final OnTimeClickListener listener;

    private int selectedPosition = -1;

    public TimeSlotAdapter(List<TimeSlot> list,
                           OnTimeClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_time_slot, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        TimeSlot slot = list.get(position);

        holder.tvTime.setText(slot.getTime());

        if (position == selectedPosition) {

            holder.cardView.setCardBackgroundColor(
                    Color.parseColor("#0A7067")
            );

            holder.tvTime.setTextColor(Color.WHITE);

        } else {

            holder.cardView.setCardBackgroundColor(Color.WHITE);

            holder.tvTime.setTextColor(
                    Color.parseColor("#0B1C30")
            );
        }

        holder.itemView.setOnClickListener(v -> {

            int old = selectedPosition;

            selectedPosition = holder.getAdapterPosition();

            if (old != RecyclerView.NO_POSITION) {
                notifyItemChanged(old);
            }

            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onClick(slot);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        TextView tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.cardTimeSlot);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}