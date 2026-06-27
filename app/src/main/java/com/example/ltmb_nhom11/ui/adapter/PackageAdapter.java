package com.example.ltmb_nhom11.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Package;

import java.util.List;
import java.text.NumberFormat;
import java.util.Locale;


public class PackageAdapter
        extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    private final List<Package> packageList;
    private final OnPackageClickListener listener;

    public interface OnPackageClickListener {
        void onRegisterClick(Package medicalPackage);
    }

    public PackageAdapter(List<Package> packageList,
                          OnPackageClickListener listener) {
        this.packageList = packageList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(
                        R.layout.item_medical_package,
                        parent,
                        false
                );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position) {

        Package p = packageList.get(position);

        holder.tvName.setText(p.getName());
        holder.tvDescription.setText(p.getDescription());
        NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));

        holder.tvPrice.setText(
                formatter.format((long) p.getPrice()) + " VNĐ"
        );
        holder.btnRegister.setOnClickListener(v -> {
            listener.onRegisterClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return packageList.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvName;
        TextView tvDescription;
        TextView tvPrice;

        Button btnRegister;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvPackageTitle);
            tvDescription =
                    itemView.findViewById(R.id.tvPackageDescription);
            tvPrice =
                    itemView.findViewById(R.id.tvPackagePrice);
            btnRegister =
                    itemView.findViewById(R.id.btnRegister);
        }
    }
}