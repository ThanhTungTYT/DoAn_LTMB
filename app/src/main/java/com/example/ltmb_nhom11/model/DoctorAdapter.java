package com.example.ltmb_nhom11.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.ui.ImageLoader;
import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.ViewHolder> {

    private final List<Doctor> doctors;
    private OnBookClickListener listener;

    public interface OnBookClickListener {
        void onBookClick(Doctor doctor);
    }

    public DoctorAdapter(List<Doctor> doctors, OnBookClickListener listener) {
        this.doctors = doctors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_doctor, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Doctor doc = doctors.get(position);
        holder.tvName.setText(doc.getName());
        holder.tvDept.setText(doc.getDept() + " • " + doc.getStatus());

        if (doc.getAvatarUrl() != null && !doc.getAvatarUrl().isEmpty()) {
            ImageLoader.load(doc.getAvatarUrl(), holder.imgAvatar);
        }

        holder.btnBook.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookClick(doc);
            }
        });
    }

    @Override
    public int getItemCount() {
        return doctors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgAvatar;
        public TextView tvName;
        public TextView tvDept;
        public Button btnBook;

        public ViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgDoctorAvatar);
            tvName = itemView.findViewById(R.id.tvDoctorName);
            tvDept = itemView.findViewById(R.id.tvDoctorDept);
            btnBook = itemView.findViewById(R.id.btnBook);
        }
    }
}