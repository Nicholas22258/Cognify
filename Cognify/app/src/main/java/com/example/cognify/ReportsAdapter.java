package com.example.cognify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ReportViewHolder> {

    private Context context;
    private List<Report> reportList;
    private OnReportActionListener listener;

    public interface OnReportActionListener {
        void onMarkAddressed(Report report);
    }

    public ReportsAdapter(Context context, List<Report> reportList, OnReportActionListener listener) {
        this.context = context;
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_report, parent, false);
        return new ReportViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        Report report = reportList.get(position);

        // Username
        holder.tvUsername.setText(report.getUsername() != null ? report.getUsername() : "Unknown");

        // Date
        if (report.getDateSent() != null) {
            Timestamp timestamp = report.getDateSent();
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            holder.tvDate.setText("Date: " + sdf.format(date));
        } else {
            holder.tvDate.setText("Date: N/A");
        }

        // Message
        holder.tvMessage.setText(report.getMessage() != null ? report.getMessage() : "");

        // Addressed button
        holder.btnAddress.setEnabled(!report.isAddressed());
        holder.btnAddress.setText(report.isAddressed() ? "Addressed" : "Mark as Addressed");

        holder.btnAddress.setOnClickListener(v -> {
            if (listener != null && !report.isAddressed()) {
                listener.onMarkAddressed(report);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public void updateList(List<Report> newList) {
        reportList.clear();
        reportList.addAll(newList);
        notifyDataSetChanged();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {
        TextView tvUsername, tvDate, tvMessage;
        Button btnAddress;

        public ReportViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.reportUsername);
            tvDate = itemView.findViewById(R.id.reportDate);
            tvMessage = itemView.findViewById(R.id.reportMessage);
            btnAddress = itemView.findViewById(R.id.reportAddressBtn);
        }
    }
}
