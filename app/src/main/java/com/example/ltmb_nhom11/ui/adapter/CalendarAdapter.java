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
 * Adapter danh sách chọn ngày khám (cuộn ngang).
 * Chủ nhật (selectable=false) hiển thị viền đỏ và không cho chọn.
 */
public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.DateViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(CalendarDate date);
    }

    private final List<CalendarDate> dateList;
    private final OnItemClickListener listener;
    private int selectedPosition = -1;

    public CalendarAdapter(List<CalendarDate> dateList, OnItemClickListener listener) {
        this.dateList = dateList;
        this.listener = listener;
        // Chọn sẵn ngày đầu tiên có thể đặt (bỏ qua Chủ nhật)
        for (int i = 0; i < dateList.size(); i++) {
            if (dateList.get(i).isSelectable()) {
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

        if (!date.isSelectable()) {
            // Chủ nhật: viền đỏ, mờ, không bấm được
            holder.cardDateHolder.setCardBackgroundColor(Color.WHITE);
            holder.lytDateItem.setBackgroundResource(R.drawable.bg_date_sunday);
            holder.txtDayName.setTextColor(Color.parseColor("#BA1A1A"));
            holder.txtDayValue.setTextColor(Color.parseColor("#BA1A1A"));
            holder.itemView.setAlpha(0.6f);
            holder.itemView.setClickable(false);
            holder.itemView.setOnClickListener(null);
            return;
        }

        holder.itemView.setAlpha(1f);
        holder.itemView.setClickable(true);

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
