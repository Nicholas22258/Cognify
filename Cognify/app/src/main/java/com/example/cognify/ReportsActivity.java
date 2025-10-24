package com.example.cognify;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ReportsActivity extends AppCompatActivity {

    private RecyclerView rvReports;
    private EditText etSearch;
    private TextView tvNoReports;
    private Button btnBack;

    private ReportsAdapter reportsAdapter;
    private List<Report> reportList = new ArrayList<>();
    private List<Report> filteredList = new ArrayList<>();

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_reports);

        // Initialize views
        rvReports = findViewById(R.id.rvReports);
        etSearch = findViewById(R.id.etSearch);
        tvNoReports = findViewById(R.id.tvNoReports);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();

        // RecyclerView setup
        reportsAdapter = new ReportsAdapter(this, new ArrayList<>(), this::markReportAddressed);
        rvReports.setLayoutManager(new LinearLayoutManager(this));
        rvReports.setAdapter(reportsAdapter);

        // Listen for live Firestore updates
        listenForReports();

        // Search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterReports(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        // Back button
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void listenForReports() {
        db.collection("reports")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Failed to load reports: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ReportsActivity", "Error fetching reports", e);
                        return;
                    }

                    reportList.clear();

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            try {
                                Report report = doc.toObject(Report.class);
                                if (report != null) {
                                    report.setId(doc.getId());
                                    reportList.add(report);
                                    Log.d("ReportsActivity", "Loaded report: " + report.getUsername() + " - " + report.getMessage());
                                }
                            } catch (Exception ex) {
                                Log.e("ReportsActivity", "Error parsing report: " + ex.getMessage());
                            }
                        }

                        // Sort by dateSent descending
                        reportList.sort((r1, r2) -> {
                            if (r1.getDateSent() == null) return 1;
                            if (r2.getDateSent() == null) return -1;
                            return r2.getDateSent().toDate().compareTo(r1.getDateSent().toDate());
                        });

                        Log.d("ReportsActivity", "Total reports loaded: " + reportList.size());
                    } else {
                        Log.d("ReportsActivity", "No reports found");
                    }

                    // Show all reports by default or filter by search
                    filterReports(etSearch.getText().toString());
                });
    }

    private void filterReports(String query) {
        filteredList.clear();
        query = query.toLowerCase().trim();

        if (query.isEmpty()) {
            filteredList.addAll(reportList);
        } else {
            for (Report report : reportList) {
                String username = report.getUsername() != null ? report.getUsername().toLowerCase() : "";
                String message = report.getMessage() != null ? report.getMessage().toLowerCase() : "";

                if (username.contains(query) || message.contains(query)) {
                    filteredList.add(report);
                }
            }
        }

        if (filteredList.isEmpty()) {
            tvNoReports.setVisibility(View.VISIBLE);
            rvReports.setVisibility(View.GONE);
        } else {
            tvNoReports.setVisibility(View.GONE);
            rvReports.setVisibility(View.VISIBLE);
        }

        reportsAdapter.updateList(filteredList);
        Log.d("ReportsActivity", "Filtered list size: " + filteredList.size());
    }


    private void markReportAddressed(Report report) {
        db.collection("reports")
                .document(report.getId())
                .update("addressed", true)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Report marked as addressed", Toast.LENGTH_SHORT).show();
                    report.setAddressed(true);
                    reportsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to mark report: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
