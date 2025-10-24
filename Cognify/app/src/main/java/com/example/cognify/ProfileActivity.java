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
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private TextView usernameText;
    private TextView passwordText;
    private ProgressBar milestoneProgress;
    private TextView milestoneLevelText;
    private TextView xpProgressText;
    private Button badgesButton;
    private Button milestonesButton;
    private Button changePasswordButton;

    private Button btnBackToHomepage;
    private Button logOut;

    private Button addAndViewInformation;
    private ImageView passwordVisibilityToggle;

    private MilestoneManager milestoneManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int currentUserXP = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        milestoneManager = new MilestoneManager();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    // Already on home page, do nothing or refresh
//                    Toast.makeText(ProfileActivity.this, "Home Page", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(ProfileActivity.this, HomePage.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_books) {
                    // Navigate to Books Activity
                    startActivity(new Intent(ProfileActivity.this, AddAndViewInformation.class));
//                    Toast.makeText(HomePage.this, "Books (Not Implemented)", Toast.LENGTH_SHORT).show(); // Example toast
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    // Navigate to Profile Activity
//                    startActivity(new Intent(ProfileActivity.this, ProfileActivity.class));
                    Toast.makeText(ProfileActivity.this, "Profile Page", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_games) {
                    // Navigate to Games Activity
                    startActivity(new Intent(ProfileActivity.this, GamesScreen.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_settings) {
                    // Navigate to Settings Activity
                    startActivity(new Intent(ProfileActivity.this, HelpActivity.class));
                    return true;
                } else {
                    return false;
                }
            }
        });

        initializeViews();
        setupClickListeners();
        loadUserData();
        loadUserInfo();

        Button deleteAccountButton = findViewById(R.id.btn_delete_account);

        deleteAccountButton.setOnClickListener(view -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account? This cannot be undone.")
                    .setPositiveButton("Yes", (dialog, which) -> deleteUserAccount())
                    .setNegativeButton("No", null)
                    .show();
        });


    }

    private void deleteUserAccount() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = auth.getCurrentUser().getUid();

        // 1. Delete Firestore data
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // If you saved game progress or other collections per user, delete them too
                    db.collection("GameDetailsTracker").document(userId).delete();
                    db.collection("UserRewards").document(userId).delete();
                    db.collection("UserTasks").document(userId).delete();

                    // 2. Delete Auth Account
                    auth.getCurrentUser().delete()
                            .addOnSuccessListener(aVoid2 -> {
                                Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, LoginActivity.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "You must re-login before deleting your account.", Toast.LENGTH_LONG).show();
                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void initializeViews() {
        usernameText = findViewById(R.id.tv_username);
        passwordText = findViewById(R.id.tv_password);
        milestoneProgress = findViewById(R.id.progress_milestone);
        milestoneLevelText = findViewById(R.id.tv_milestone_level);
//        xpProgressText = findViewById(R.id.tv_xp_progress);
        badgesButton = findViewById(R.id.btn_my_badges);
        milestonesButton = findViewById(R.id.btn_milestones);
        changePasswordButton = findViewById(R.id.btn_change_password);
        passwordVisibilityToggle = findViewById(R.id.iv_password_visibility);
        addAndViewInformation = findViewById(R.id.btnAddViewNotes);
        btnBackToHomepage = findViewById(R.id.btnBackToHomeage);
        logOut = findViewById(R.id.btnLogOut);
    }

    private void setupClickListeners() {
        badgesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, BadgesActivity.class);
                startActivity(intent);
            }
        });

        milestonesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MilestonesActivity.class);
                startActivity(intent);
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            }
        });

        if (passwordVisibilityToggle != null) {
            passwordVisibilityToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    togglePasswordVisibility();
                }
            });
        }

        addAndViewInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AddAndViewInformation.class);
                startActivity(intent);
            }
        });

        btnBackToHomepage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HomePage.class);
                startActivity(intent);
            }
        });

        logOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void loadUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        usernameText.setText("Username: " + (username != null ? username : "N/A"));

                        Long totalPoints = documentSnapshot.getLong("totalPoints");
                        currentUserXP = (totalPoints != null) ? totalPoints.intValue() : 0;

                        updateMilestoneDisplay();
                        updateBadgeButtonColor();

                        if (passwordText != null) {
                            passwordText.setText("••••••••");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileActivity.this, "Error loading profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) return;

        String userId = currentUser.getUid();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        TextView usernameView = findViewById(R.id.tv_username);
                        TextView emailView = findViewById(R.id.tv_user_email);
                        TextView studentIdView = findViewById(R.id.tv_student_id);

                        if (usernameView != null) {
                            String username = documentSnapshot.getString("username");
                            usernameView.setText("Username: " + (username != null ? username : "N/A"));
                        }
                        if (emailView != null) {
                            String email = documentSnapshot.getString("email");
                            emailView.setText(email != null ? email : "N/A");
                        }
                        if (studentIdView != null) {
                            studentIdView.setText(userId.substring(0, Math.min(10, userId.length())));
                        }
                    }
                });
    }

    private void updateMilestoneDisplay() {
        Milestone currentMilestone = milestoneManager.getCurrentMilestone(currentUserXP);
        int progress = milestoneManager.calculateProgress(currentUserXP, currentMilestone);

        if (milestoneProgress != null) {
            milestoneProgress.setProgress(progress);
        }

        if (milestoneLevelText != null) {
            milestoneLevelText.setText("Level " + currentMilestone.getLevel() + " - " + currentMilestone.getTitle());
        }

        if (xpProgressText != null) {
            xpProgressText.setText("XP: " + currentUserXP + " / " + currentMilestone.getMaxXP());
        }
    }

    private void updateBadgeButtonColor() {
        if (badgesButton == null) return;

        Milestone currentMilestone = milestoneManager.getCurrentMilestone(currentUserXP);
        int level = currentMilestone.getLevel();

        try {
            switch (level) {
                case 1:
//                    badgesButton.setBackgroundResource(R.drawable.button_milestone_level1);
                    break;
                case 2:
//                    badgesButton.setBackgroundResource(R.drawable.button_milestone_level2);
                    break;
                case 3:
//                    badgesButton.setBackgroundResource(R.drawable.button_milestone_level3);
                    break;
                case 4:
//                    badgesButton.setBackgroundResource(R.drawable.button_milestone_level4);
                    break;
                case 5:
//                    badgesButton.setBackgroundResource(R.drawable.button_milestone_level5);
                    break;
            }
        } catch (Exception e) {
        }
    }

    private void togglePasswordVisibility() {
        if (passwordText == null) return;

        if (passwordText.getVisibility() == View.VISIBLE) {
            passwordText.setVisibility(View.GONE);
            if (passwordVisibilityToggle != null) {
                passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility_off);
            }
        } else {
            passwordText.setVisibility(View.VISIBLE);
            if (passwordVisibilityToggle != null) {
                passwordVisibilityToggle.setImageResource(R.drawable.ic_visibility);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        loadUserInfo();
    }
}