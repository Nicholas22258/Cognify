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
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;

public class AdminDashboardActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private TextView welcomeText, dateText;
    private TextView totalUsersCount, activeUsersCount, totalMaterialsCount, totalGameSessionsCount;
    private MaterialCardView userManagementCard, contentManagementCard, gameAnalyticsCard, reportsCard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dash);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        initializeViews();

        // Setup toolbar
        setSupportActionBar(toolbar);

        // Setup navigation drawer
        setupNavigationDrawer();

        // Set current date
        setCurrentDate();

        // Load dashboard data
        loadDashboardStatistics();

        // Setup click listeners
        setupClickListeners();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        toolbar = findViewById(R.id.toolbar);

        welcomeText = findViewById(R.id.welcomeText);
        dateText = findViewById(R.id.dateText);

        totalUsersCount = findViewById(R.id.totalUsersCount);
        activeUsersCount = findViewById(R.id.activeUsersCount);
        totalMaterialsCount = findViewById(R.id.totalMaterialsCount);
        totalGameSessionsCount = findViewById(R.id.totalGameSessionsCount);

        userManagementCard = findViewById(R.id.userManagementCard);
        contentManagementCard = findViewById(R.id.contentManagementCard);
        gameAnalyticsCard = findViewById(R.id.gameAnalyticsCard);
        reportsCard = findViewById(R.id.reportsCard);
    }

    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Update nav header with admin info
        View headerView = navigationView.getHeaderView(0);
        TextView navUserName = headerView.findViewById(R.id.navUserName);
        TextView navUserEmail = headerView.findViewById(R.id.navUserEmail);

        if (mAuth.getCurrentUser() != null) {
            navUserEmail.setText(mAuth.getCurrentUser().getEmail());

            // Fetch admin name from Firestore
            db.collection("users").document(mAuth.getCurrentUser().getUid())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            String username = task.getResult().getString("username");
                            Log.d("FirestoreCheck", "Fetched username: " + username); // ✅ Add this
                            if (username != null) {
                                navUserName.setText(username);
                                welcomeText.setText("Welcome Back, " + username + "!");
                            } else {
                                Log.d("FirestoreCheck", "Username is null");
                            }
                        } else {
                            Log.e("FirestoreCheck", "Error getting document", task.getException());
                        }
                    });

        }
    }

    private void setCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault());
        String currentDate = sdf.format(new Date());
        dateText.setText(currentDate);
    }

    private void loadDashboardStatistics() {
        loadTotalUsers();
        loadActiveUsers();
        loadTotalMaterials();
        loadTotalGameSessions();
    }

    private void loadTotalUsers() {
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            totalUsersCount.setText(String.valueOf(count));
                        } else {
                            totalUsersCount.setText("--");
                            Toast.makeText(AdminDashboardActivity.this,
                                    "Failed to load user count", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loadActiveUsers() {
        // Get users active today (those with lastActive timestamp today)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date startOfDay = calendar.getTime();

        db.collection("users")
                .whereGreaterThanOrEqualTo("lastActive", startOfDay)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            activeUsersCount.setText(String.valueOf(count));
                        } else {
                            activeUsersCount.setText("--");
                        }
                    }
                });
    }

    private void loadTotalMaterials() {
        db.collection("studyMaterials")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            totalMaterialsCount.setText(String.valueOf(count));
                        } else {
                            totalMaterialsCount.setText("--");
                        }
                    }
                });
    }

    private void loadTotalGameSessions() {
        db.collection("gameSessions")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int count = task.getResult().size();
                            totalGameSessionsCount.setText(String.valueOf(count));
                        } else {
                            totalGameSessionsCount.setText("--");
                        }
                    }
                });
    }

    private void setupClickListeners() {
        userManagementCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Create UserManagementActivity
                Toast.makeText(AdminDashboardActivity.this,
                        "User Management - Coming Soon", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(AdminDashboardActivity.this, UserManagementActivity.class));
            }
        });

        contentManagementCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Create ContentManagementActivity
                Toast.makeText(AdminDashboardActivity.this,
                        "Content Management - Coming Soon", Toast.LENGTH_SHORT).show();
                 startActivity(new Intent(AdminDashboardActivity.this, ContentManagementActivity.class));
            }
        });

        gameAnalyticsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Create GameAnalyticsActivity
                Toast.makeText(AdminDashboardActivity.this,
                        "Game Analytics - Coming Soon", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(AdminDashboardActivity.this, GameAnalyticsActivity.class));
            }
        });

        reportsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Create ReportsActivity
                Toast.makeText(AdminDashboardActivity.this,
                        "Reports - Coming Soon", Toast.LENGTH_SHORT).show();
                // startActivity(new Intent(AdminDashboardActivity.this, ReportsActivity.class));
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_dashboard) {
            // Already on dashboard
            Toast.makeText(this, "Dashboard", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_users) {
            Toast.makeText(this, "User Management - Coming Soon", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, UserManagementActivity.class));
        } else if (id == R.id.nav_content) {
            Toast.makeText(this, "Content Management - Coming Soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, ContentManagementActivity.class));
        } else if (id == R.id.nav_analytics) {
            Toast.makeText(this, "Game Analytics - Coming Soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, GameAnalyticsActivity.class));
        } else if (id == R.id.nav_reports) {
            Toast.makeText(this, "Reports - Coming Soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, ReportsActivity.class));
        } else if (id == R.id.nav_settings) {
            Toast.makeText(this, "Settings - Coming Soon", Toast.LENGTH_SHORT).show();
            // startActivity(new Intent(this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            showLogoutDialog();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    mAuth.signOut();
                    Intent intent = new Intent(AdminDashboardActivity.this, GamesScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    Toast.makeText(AdminDashboardActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            // Show exit confirmation dialog
            new AlertDialog.Builder(this)
                    .setTitle("Exit")
                    .setMessage("Are you sure you want to exit?")
                    .setPositiveButton("Yes", (dialog, which) -> finish())
                    .setNegativeButton("No", null)
                    .show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh statistics when returning to dashboard
        loadDashboardStatistics();
    }
}