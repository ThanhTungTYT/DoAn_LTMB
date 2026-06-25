package com.example.ltmb_nhom11.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ltmb_nhom11.R;
import com.example.ltmb_nhom11.model.Package;

import java.util.List;

public class PackageAdapter
        extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {

    private final List<Package> packageList;

    public PackageAdapter(List<Package> packageList) {
        this.packageList = packageList;
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
        holder.tvPrice.setText(
                String.valueOf((long)p.getPrice()) + " VNĐ"
        );
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

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvPackageTitle);
            tvDescription =
                    itemView.findViewById(R.id.tvPackageDescription);
            tvPrice =
                    itemView.findViewById(R.id.tvPackagePrice);
        }
    }
}