package com.example.ltmb_nhom11.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.CalendarDate;

import java.util.List;

/**
 * Adapter cho danh sách chọn ngày khám (RecyclerView cuộn ngang).
 * Tự quản lý trạng thái ngày đang được chọn để highlight.
 */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DateViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CalendarDate date);
    }

    private final List<CalendarDate> dateList;
    private final OnItemClickListener listener;
    private int selectedPosition = 0;

    public CalendarAdapter(List<CalendarDate> dateList, OnItemClickListener listener) {
        this.dateList = dateList;
        this.listener = listener;
        // Đặt vị trí chọn ban đầu theo cờ isSelected (nếu có)
        for (int i = 0; i < dateList.size(); i++) {
            if (dateList.get(i).isSelected()) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public DateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_date, parent, false);
        return new DateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DateViewHolder holder, int position) {
        CalendarDate date = dateList.get(position);
        holder.txtDayName.setText(date.getDayName());
        holder.txtDayValue.setText(date.getDayValue());

        boolean isSelected = position == selectedPosition;

        if (isSelected) {
            holder.cardDateHolder.setCardBackgroundColor(Color.parseColor("#00685F"));
            holder.lytDateItem.setBackgroundColor(Color.parseColor("#00685F"));
            holder.txtDayName.setTextColor(Color.WHITE);
            holder.txtDayValue.setTextColor(Color.WHITE);
        } else {
            holder.cardDateHolder.setCardBackgroundColor(Color.WHITE);
            holder.lytDateItem.setBackgroundColor(Color.WHITE);
            holder.txtDayName.setTextColor(Color.parseColor("#5C5F61"));
            holder.txtDayValue.setTextColor(Color.parseColor("#0B1C30"));
        }

        holder.itemView.setOnClickListener(v -> {
            int previous = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previous);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onItemClick(date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dateList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        CardView cardDateHolder;
        LinearLayout lytDateItem;
        TextView txtDayName;
        TextView txtDayValue;

        DateViewHolder(@NonNull View itemView) {
            super(itemView);
            cardDateHolder = itemView.findViewById(R.id.cardDateHolder);
            lytDateItem = itemView.findViewById(R.id.lytDateItem);
            txtDayName = itemView.findViewById(R.id.txtDayName);
            txtDayValue = itemView.findViewById(R.id.txtDayValue);
        }
    }
}
