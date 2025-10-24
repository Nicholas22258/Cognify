package com.example.cognify;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserManagementActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextInputEditText searchEditText;
    private ChipGroup filterChipGroup;
    private Chip chipAll, chipActive, chipSuspended, chipAdmins;
    private TextView userCountText;
    private RecyclerView usersRecyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private FloatingActionButton fabExport;

    private FirebaseFirestore db;
    private UserAdapter userAdapter;
    private List<User> userList;
    private List<User> filteredUserList;

    private String currentFilter = "all";
    private String searchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_management);

        // Initialize Firebase
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

        // Setup listeners
        setupListeners();

        // Load users
        loadUsers();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        searchEditText = findViewById(R.id.search);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipActive = findViewById(R.id.chipActive);
        chipSuspended = findViewById(R.id.chipSuspended);
        chipAdmins = findViewById(R.id.chipAdmins);
        userCountText = findViewById(R.id.userCountText);
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        progressBar = findViewById(R.id.progressBar);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        fabExport = findViewById(R.id.fabExport);

        userList = new ArrayList<>();
        filteredUserList = new ArrayList<>();
    }

    private void setupRecyclerView() {
        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        userAdapter = new UserAdapter(this, new ArrayList<>(), new UserAdapter.OnUserActionListener() {
            @Override
            public void onViewDetails(User user) {
                viewUserDetails(user);
            }

            @Override
            public void onSuspendUser(User user) {
                suspendUser(user);
            }

            @Override
            public void onActivateUser(User user) {
                activateUser(user);
            }

            @Override
            public void onMakeAdmin(User user) {
                new AlertDialog.Builder(UserManagementActivity.this)
                        .setTitle("Make Admin")
                        .setMessage("Are you sure you want to make " + user.getUsername() + " an admin?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            db.collection("users").document(user.getUserId())
                                    .update("isAdmin", true)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(UserManagementActivity.this,
                                                user.getUsername() + " is now an admin!",
                                                Toast.LENGTH_SHORT).show();
                                        loadUsers(); // Refresh the list after updating Firestore
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(UserManagementActivity.this,
                                                "Failed to update user: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        usersRecyclerView.setAdapter(userAdapter);
    }


    private void setupListeners() {
        // Search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchQuery = s.toString().toLowerCase();
                filterUsers();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Filter chips
        filterChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentFilter = "all";
            } else if (checkedId == R.id.chipActive) {
                currentFilter = "active";
            } else if (checkedId == R.id.chipSuspended) {
                currentFilter = "suspended";
            } else if (checkedId == R.id.chipAdmins) {
                currentFilter = "admins";
            }
            filterUsers();
        });

        // Swipe to refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadUsers();
        });

        // Export button
        fabExport.setOnClickListener(v -> {
            exportUsers();
        });
    }

    private void loadUsers() {
        showLoading(true);
        emptyStateLayout.setVisibility(View.GONE);

        db.collection("users")
                .orderBy("joinDate", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    userList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // ADD THIS - Log raw Firestore data BEFORE converting to User object
                        if (document.getId().equals("gamifying52@gmail.com") ||
                                document.getString("email").equals("gamifying52@gmail.com")) {
                            android.util.Log.d("UserManagement", "=== RAW FIRESTORE DATA for gamifying52 ===");
                            android.util.Log.d("UserManagement", "Document ID: " + document.getId());
                            android.util.Log.d("UserManagement", "All data: " + document.getData());
                            android.util.Log.d("UserManagement", "isAdmin field: " + document.get("isAdmin"));
                            android.util.Log.d("UserManagement", "isActive field: " + document.get("isActive"));
                        }

                        User user = document.toObject(User.class);
                        user.setUserId(document.getId());

                        // Log after conversion
                        if (user.getEmail() != null && user.getEmail().equals("gamifying52@gmail.com")) {
                            android.util.Log.d("UserManagement", "=== AFTER CONVERSION ===");
                            android.util.Log.d("UserManagement", "user.isAdmin(): " + user.isAdmin());
                            android.util.Log.d("UserManagement", "user.isActive(): " + user.isActive());
                        }

                        userList.add(user);
                    }

                    filterUsers();
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    swipeRefreshLayout.setRefreshing(false);
                    Toast.makeText(this, "Failed to load users: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void filterUsers() {
        filteredUserList.clear();

        // ADD THIS LOG
        android.util.Log.d("UserManagement", "=== Filtering with: " + currentFilter + " ===");

        for (User user : userList) {
            boolean matchesFilter = false;
            boolean matchesSearch = false;

            // Apply filter
            switch (currentFilter) {
                case "all":
                    matchesFilter = true;
                    break;
                case "active":
                    matchesFilter = user.isActive();
                    break;
                case "suspended":
                    matchesFilter = !user.isActive();
                    break;
                case "admins":
                    matchesFilter = user.isAdmin();

                    android.util.Log.d("UserManagement", "User: " + user.getUsername() +
                            ", isAdmin: " + user.isAdmin() + ", matches: " + matchesFilter);
                    break;
            }

            // Apply search
            if (searchQuery.isEmpty()) {
                matchesSearch = true;
            } else {
                String username = user.getUsername() != null ? user.getUsername().toLowerCase() : "";
                String email = user.getEmail() != null ? user.getEmail().toLowerCase() : "";
                matchesSearch = username.contains(searchQuery) || email.contains(searchQuery);
            }

            if (matchesFilter && matchesSearch) {
                filteredUserList.add(user);
            }
        }

        // ADD THIS LOG
        android.util.Log.d("UserManagement", "Total users in list: " + userList.size());
        android.util.Log.d("UserManagement", "Filtered users: " + filteredUserList.size());

        userAdapter.updateList(filteredUserList);
        updateUserCount();

        if (filteredUserList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            usersRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            usersRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    private void updateUserCount() {
        userCountText.setText("Total: " + filteredUserList.size() + " users");
    }

    private void viewUserDetails(User user) {
        // Create a dialog to show user details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("User Details: " + user.getUsername());

        String details = "Email: " + user.getEmail() + "\n\n" +
                "User ID: " + user.getUserId() + "\n" +
                "Status: " + (user.isActive() ? "Active" : "Suspended") + "\n" +
                "Admin: " + (user.isAdmin() ? "Yes" : "No") + "\n\n" +
                "Materials Uploaded: " + user.getTotalMaterialsUploaded() + "\n" +
                "Games Played: " + user.getTotalGamesPlayed() + "\n" +
                "Total Points: " + user.getTotalPoints();

        builder.setMessage(details);
        builder.setPositiveButton("Close", null);
        builder.show();
    }

    private void suspendUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Suspend User")
                .setMessage("Are you sure you want to suspend " + user.getUsername() + "?")
                .setPositiveButton("Suspend", (dialog, which) -> {
                    db.collection("users").document(user.getUserId())
                            .update("isActive", false)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "User suspended successfully",
                                        Toast.LENGTH_SHORT).show();
                                loadUsers();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to suspend user: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void activateUser(User user) {
        new AlertDialog.Builder(this)
                .setTitle("Activate User")
                .setMessage("Are you sure you want to activate " + user.getUsername() + "?")
                .setPositiveButton("Activate", (dialog, which) -> {
                    db.collection("users").document(user.getUserId())
                            .update("isActive", true)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "User activated successfully",
                                        Toast.LENGTH_SHORT).show();
                                loadUsers();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Failed to activate user: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void exportUsers() {
        // Simple export to show data
        StringBuilder csv = new StringBuilder();
        csv.append("Username,Email,Status,Admin,Materials,Games,Points\n");

        for (User user : filteredUserList) {
            csv.append(user.getUsername()).append(",");
            csv.append(user.getEmail()).append(",");
            csv.append(user.isActive() ? "Active" : "Suspended").append(",");
            csv.append(user.isAdmin() ? "Yes" : "No").append(",");
            csv.append(user.getTotalMaterialsUploaded()).append(",");
            csv.append(user.getTotalGamesPlayed()).append(",");
            csv.append(user.getTotalPoints()).append("\n");
        }

        Toast.makeText(this, "Exported " + filteredUserList.size() + " users\nCSV data ready",
                Toast.LENGTH_LONG).show();

        // TODO: Save CSV to file or share
        // For now, just showing toast message
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            usersRecyclerView.setVisibility(View.GONE);
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
        loadUsers();
    }
}