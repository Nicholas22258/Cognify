package com.example.cognify;

/*
 * @Author Nicholas Leong        EDUV4551823
 * @Author Aarya Manowah         be.2023.q4t9k6
 * @Author Nyasha Masket        BE.2023.R3M0Y0
 * @Author Sakhile Lesedi Mnisi  BE.2022.j9f3j4
 * @Author Dominic Newton       EDUV4818782
 * @Author Kimberly Sean Sibanda EDUV4818746
 *
 * Supervisor: Stacey Byrne      Stacey.byrne@eduvos.com
 * */

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ContentManagementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private Chip chipAll, chipPending, chipApproved, chipRejected;
    private Spinner subjectSpinner;
    private TextView materialCountText;
    private RecyclerView materialsRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;

    private FirebaseFirestore db;
    private MaterialAdapter materialAdapter;
    private List<StudyMaterial> materialList;
    private List<StudyMaterial> filteredMaterialList;

    private String currentFilter = "all";
    private String searchQuery = "";
    private String selectedSubject = "All Subjects";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_management);}}

       /* // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Setup RecyclerView
        setupRecyclerView();

        // Setup subject spinner
        setupSubjectSpinner();

        // Setup listeners
        setupListeners();

        // Load materials
        loadMaterials();
    }
/*
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        searchEditText = findViewById(R.id.searchEditText);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipPending = findViewById(R.id.chipPending);
        chipApproved = findViewById(R.id.chipApproved);
        chipRejected = findViewById(R.id.chipRejected);
        subjectSpinner = findViewById(R.id.subjectSpinner);
        materialCountText = findViewById(R.id.materialCountText);
        materialsRecyclerView = findViewById(R.id.materialsRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);

        materialList = new ArrayList<>();
        filteredMaterialList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        materialsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        materialAdapter = new MaterialAdapter(this, filteredMaterialList, new MaterialAdapter.OnMaterialActionListener() {
            @Override
            public void onViewMaterial(StudyMaterial material) {
                viewMaterial(material);
            }

            @Override
            public void onApproveMaterial(StudyMaterial material) {
                approveMaterial(material);
            }

            @Override
            public void onRejectMaterial(StudyMaterial material) {
                rejectMaterial(material);
            }

            @Override
            public void onDeleteMaterial(StudyMaterial material) {
                deleteMaterial(material);
            }
        });
        materialsRecyclerView.setAdapter(materialAdapter);
    }

    private void setupSubjectSpinner() {
        List<String> subjects = Arrays.asList(
                "All Subjects", "Mathematics", "Science", "English",
                "History", "Geography", "Physics", "Chemistry", "Biology", "Other"
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, subjects
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        subjectSpinner.setAdapter(adapter);

        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubject = subjects.get(position);
                filterMaterials();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setupListeners() {
        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                filterMaterials();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter chips
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentFilter = "all";
            } else if (checkedId == R.id.chipPending) {
                currentFilter = "pending";
            } else if (checkedId == R.id.chipApproved) {
                currentFilter = "approved";
            } else if (checkedId == R.id.chipRejected) {
                currentFilter = "rejected";
            }
            filterMaterials();
        });

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadMaterials();
        });
    }

    private void loadMaterials() {
        showLoading(true);
        emptyStateLayout.setVisibility(View.GONE);

        db.collection("studyMaterials")
                .orderBy("uploadDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    materialList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        StudyMaterial material = document.toObject(StudyMaterial.class);
                        material.setMaterialId(document.getId());
                        materialList.add(material);
                    }

                    filterMaterials();
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Failed to load materials: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void filterMaterials() {
        filteredMaterialList.clear();

        for (StudyMaterial material : materialList) {
            boolean matchesFilter = false;
            boolean matchesSearch = false;
            boolean matchesSubject = false;

            // Apply status filter
            switch (currentFilter) {
                case "all":
                    matchesFilter = true;
                    break;
                case "pending":
                    matchesFilter = "pending".equalsIgnoreCase(material.getStatus());
                    break;
                case "approved":
                    matchesFilter = "approved".equalsIgnoreCase(material.getStatus());
                    break;
                case "rejected":
                    matchesFilter = "rejected".equalsIgnoreCase(material.getStatus());
                    break;
            }

            // Apply search
            if (searchQuery.isEmpty()) {
                matchesSearch = true;
            } else {
                String title = material.getTitle() != null ? material.getTitle().toLowerCase() : "";
                String uploader = material.getUploadedBy() != null ? material.getUploadedBy().toLowerCase() : "";
                matchesSearch = title.contains(searchQuery) || uploader.contains(searchQuery);
            }

            // Apply subject filter
            if (selectedSubject.equals("All Subjects")) {
                matchesSubject = true;
            } else {
                matchesSubject = selectedSubject.equalsIgnoreCase(material.getSubject());
            }

            if (matchesFilter && matchesSearch && matchesSubject) {
                filteredMaterialList.add(material);
            }
        }

        materialAdapter.notifyDataSetChanged();
        updateMaterialCount();

        if (filteredMaterialList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            materialsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            materialsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void updateMaterialCount() {
        materialCountText.setText("Total: " + filteredMaterialList.size() + " materials");
    }

    private void viewMaterial(StudyMaterial material) {
        // Show material details dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(material.getTitle());

        String details = "Subject: " + material.getSubject() + "\n" +
                "Uploaded by: " + material.getUploadedBy() + "\n" +
                "File Size: " + material.getFileSize() + "\n" +
                "Downloads: " + material.getDownloadCount() + "\n" +
                "Status: " + material.getStatus();

        if (material.getRejectionReason() != null && !material.getRejectionReason().isEmpty()) {
            details += "\n\nRejection Reason: " + material.getRejectionReason();
        }

        builder.setMessage(details);
        builder.setPositiveButton("Open File", (dialog, which) -> {
            if (material.getFileUrl() != null && !material.getFileUrl().isEmpty()) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(material.getFileUrl()));
                startActivity(intent);
            } else {
                Toast.makeText(this, "File URL not available", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void approveMaterial(StudyMaterial material) {
        new AlertDialog.Builder(this)
                .setTitle("Approve Material")
                .setMessage("Are you sure you want to approve '" + material.getTitle() + "'?")
                .setPositiveButton("Approve", (dialog, which) -> {
                    db.collection("studyMaterials").document(material.getMaterialId())
                            .update("status", "approved")
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Material approved successfully",
                                        Toast.LENGTH_SHORT).show();
                                loadMaterials();
                                // TODO: Send notification to uploader
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to approve material: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void rejectMaterial(StudyMaterial material) {
        // Create dialog with reason input
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reject Material");

        final EditText input = new EditText(this);
        input.setHint("Enter rejection reason (optional)");
        builder.setView(input);

        builder.setPositiveButton("Reject", (dialog, which) -> {
            String reason = input.getText().toString().trim();

            db.collection("studyMaterials").document(material.getMaterialId())
                    .update("status", "rejected", "rejectionReason", reason)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Material rejected", Toast.LENGTH_SHORT).show();
                        loadMaterials();
                        // TODO: Send notification to uploader
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to reject material: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void deleteMaterial(StudyMaterial material) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Material")
                .setMessage("Are you sure you want to permanently delete '" + material.getTitle() + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("studyMaterials").document(material.getMaterialId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Material deleted successfully",
                                        Toast.LENGTH_SHORT).show();
                                loadMaterials();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to delete material: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            materialsRecyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this screen
        loadMaterials();
    }
}*/