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
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class BadgesActivity extends AppCompatActivity {

    private GridView badgesGridView;
    private TextView totalBadgesText;
    private BadgeAdapter badgeAdapter;
    private BadgeManager badgeManager;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int currentUserXP = 0;
    private String userId;
    private ImageView backArrow;

    // Track if activity is being destroyed
    private boolean isDestroyed = false;
    private ListenerRegistration userDataListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_badges);
        getWindow().setStatusBarColor(Color.BLACK);

        isDestroyed = false;
        badgeManager = new BadgeManager();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userId = currentUser.getUid();
        }

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cancelFirebaseOperations();
                finish();
            }
        });

        initializeViews();
        setUpClickListeners();
        setupBadges();
    }

    private void initializeViews() {
        badgesGridView = findViewById(R.id.grid_badges);
        totalBadgesText = findViewById(R.id.tv_total_badges);
        backArrow = findViewById(R.id.milestones_back_arrow);
    }

    private void setUpClickListeners(){
        if (backArrow != null){
            backArrow.setOnClickListener(v -> {
                // Cancel Firebase operations before finishing
                cancelFirebaseOperations();
                finish();
            });
        }
    }

    private void cancelFirebaseOperations() {
        // Remove listener immediately
        if (userDataListener != null) {
            userDataListener.remove();
            userDataListener = null;
        }
        // Mark as destroyed to prevent callbacks
        isDestroyed = true;
    }

    private void setupBadges() {
        if (isDestroyed) return;

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            if (!isDestroyed && !isFinishing()) {
                Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        // Remove existing listener if present
        if (userDataListener != null) {
            userDataListener.remove();
        }

        // Use get() instead of addSnapshotListener to avoid lingering listeners
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    // Check if activity is still alive before updating UI
                    if (isDestroyed || isFinishing()) {
                        return;
                    }

                    if (documentSnapshot.exists()) {
                        Long totalPoints = documentSnapshot.getLong("totalPoints");
                        currentUserXP = (totalPoints != null) ? totalPoints.intValue() : 0;
                    } else {
                        currentUserXP = 0;
                    }

                    updateBadgesDisplay();
                })
                .addOnFailureListener(e -> {
                    // Check if activity is still alive before updating UI
                    if (isDestroyed || isFinishing()) {
                        return;
                    }

                    Toast.makeText(BadgesActivity.this, "Error loading badges", Toast.LENGTH_SHORT).show();
                    currentUserXP = 0;
                    updateBadgesDisplay();
                });
    }

    private void updateBadgesDisplay() {
        // Safety check before updating UI
        if (isDestroyed || isFinishing()) {
            return;
        }

        // Additional null checks for views
        if (badgesGridView == null || totalBadgesText == null) {
            return;
        }

        List<Badge_UI> badges = createAndCheckBadges();

        badgeAdapter = new BadgeAdapter(this, badges);
        badgesGridView.setAdapter(badgeAdapter);

        int earnedBadges = countEarnedBadges(badges);
        ArrayList<Badge> xpBadges = badgeManager.getEarnedBadges(currentUserXP);
        totalBadgesText.setText("Badges Earned: " + earnedBadges + "/" + badges.size() +
                " | XP Badges: " + xpBadges.size() + "/6");
    }

    private boolean checkBadgeRequirements(int xpRequired) {
        return currentUserXP >= xpRequired;
    }

    private List<Badge_UI> createAndCheckBadges() {
        List<Badge_UI> badges = new ArrayList<>();

        badges.add(createBadge("First Steps", "Complete your first lesson",
                R.drawable.badge_first_steps, 10));
        badges.add(createBadge("Quick Learner", "Earn 100 XP",
                R.drawable.badge_quick_learner, 100));
        badges.add(createBadge("Streak Master", "Earn 250 XP",
                R.drawable.badge_streak_master, 250));
        badges.add(createBadge("Quiz Champion", "Earn 500 XP",
                R.drawable.badge_quiz_champion, 500));
        badges.add(createBadge("Perfect Score", "Earn 1000 XP",
                R.drawable.badge_perfect_score, 1000));
        badges.add(createBadge("Speed Demon", "Earn 1200 XP",
                R.drawable.badge_speed_demon, 1200));
        badges.add(createBadge("Night Owl", "Earn 1500 XP",
                R.drawable.badge_night_owl, 1500));
        badges.add(createBadge("Early Bird", "Earn 1800 XP",
                R.drawable.badge_early_bird, 1800));
        badges.add(createBadge("Social Learner", "Earn 2000 XP",
                R.drawable.badge_social_learner, 2000));
        badges.add(createBadge("Dedicated", "Earn 2500 XP",
                R.drawable.badge_dedicated, 2500));
        badges.add(createBadge("Explorer", "Earn 2800 XP",
                R.drawable.badge_explorer, 2800));
        badges.add(createBadge("Master", "Earn 3000 XP",
                R.drawable.badge_master, 3000));

        return badges;
    }

    private Badge_UI createBadge(String title, String description, int iconResource, int xpRequired) {
        boolean isEarned = checkBadgeRequirements(xpRequired);
        return new Badge_UI(title, description, iconResource, isEarned);
    }

    private int countEarnedBadges(List<Badge_UI> badges) {
        int count = 0;
        for (Badge_UI badge : badges) {
            if (badge.isEarned()) {
                count++;
            }
        }
        return count;
    }



    @Override
    protected void onPause() {
        super.onPause();
        // Cancel operations when activity is paused
        cancelFirebaseOperations();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Ensure operations are cancelled
        cancelFirebaseOperations();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Final cleanup
        isDestroyed = true;
        cancelFirebaseOperations();

        // Clear references to prevent memory leaks
        badgeAdapter = null;
        badgesGridView = null;
        totalBadgesText = null;
    }
}