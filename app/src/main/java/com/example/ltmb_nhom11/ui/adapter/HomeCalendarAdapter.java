package com.example.ltmb_nhom11.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ltmb_nhom11.R;
import java.util.List;

public class HomeCalendarAdapter extends RecyclerView.Adapter<HomeCalendarAdapter.ViewHolder> {
    private final List<String> days;
    private final List<String> bookedDays;

    public HomeCalendarAdapter(List<String> days, List<String> bookedDays) {
        this.days = days;
        this.bookedDays = bookedDays;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_calendar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String day = days.get(position);
        holder.tvDayNumber.setText(day);


        if (day.isEmpty()) {
            holder.tvDayNumber.setVisibility(View.GONE);
            holder.dotMarker.setVisibility(View.GONE);
            return;
        }

        holder.tvDayNumber.setVisibility(View.VISIBLE);


        if (position % 7 == 6) {
            holder.tvDayNumber.setTextColor(Color.parseColor("#BA1A1A")); // Màu đỏ (error)
            holder.tvDayNumber.setAlpha(0.6f);
        } else {
            holder.tvDayNumber.setTextColor(Color.parseColor("#191C1D")); // Màu mặc định
            holder.tvDayNumber.setAlpha(1.0f);
        }


        if (bookedDays.contains(day)) {
            holder.dotMarker.setVisibility(View.VISIBLE);
            holder.tvDayNumber.setTypeface(null, android.graphics.Typeface.BOLD);
        } else {
            holder.dotMarker.setVisibility(View.GONE);
            holder.tvDayNumber.setTypeface(null, android.graphics.Typeface.NORMAL);
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber;
        View dotMarker;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            dotMarker = itemView.findViewById(R.id.dotMarker);
        }
    }
}