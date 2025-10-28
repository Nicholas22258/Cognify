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

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MilestonesActivity extends AppCompatActivity {

    private ListView milestonesListView;
    private TextView currentLevelText;
    private TextView totalXpText;
    private MilestoneAdapter milestoneAdapter;
    private ImageView backArrow;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private String userId;
    private int currentUserXP = 0;

    private List<Milestone> masterMilestones;
    private List<Milestone> userMilestones;

    // Track if activity is being destroyed
    private boolean isDestroyed = false;
    // Track milestone creation to avoid duplicates
    private boolean isCreatingMilestones = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_milestones);
        getWindow().setStatusBarColor(Color.BLACK);

        isDestroyed = false;
        initializeViews();

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        userId = currentUser.getUid();
        masterMilestones = new ArrayList<>();
        userMilestones = new ArrayList<>();

        // Handle back press using OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cancelFirebaseOperations();
                finish();
            }
        });

        setupBackButton();
        loadUserXPAndMilestones();
    }

    private void initializeViews() {
        milestonesListView = findViewById(R.id.list_milestones);
        currentLevelText = findViewById(R.id.tv_current_level);
        totalXpText = findViewById(R.id.tv_total_xp);
        backArrow = findViewById(R.id.milestones_back_arrow);
    }

    private void setupBackButton() {
        if (backArrow != null) {
            backArrow.setOnClickListener(v -> {
                cancelFirebaseOperations();
                finish();
            });
        }
    }

    private void cancelFirebaseOperations() {
        isDestroyed = true;
        isCreatingMilestones = false;
    }

    private void loadUserXPAndMilestones() {
        if (isDestroyed) return;

        // First, load user's total XP
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isDestroyed || isFinishing()) return;

                    if (documentSnapshot.exists()) {
                        Long totalPoints = documentSnapshot.getLong("totalPoints");
                        currentUserXP = (totalPoints != null) ? totalPoints.intValue() : 0;
                    }

                    // Then load milestones from Firebase
                    loadMasterMilestones();
                })
                .addOnFailureListener(e -> {
                    if (isDestroyed || isFinishing()) return;

                    Toast.makeText(this, "Error loading XP", Toast.LENGTH_SHORT).show();
                    currentUserXP = 0;
                    loadMasterMilestones();
                });
    }

    private void loadMasterMilestones() {
        if (isDestroyed) return;

        db.collection("milestones")
                .orderBy("order")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (isDestroyed || isFinishing()) return;

                    masterMilestones.clear(); // Always clear before loading
                    if (queryDocumentSnapshots.isEmpty()) {
                        // No master milestones in database, use hardcoded ones
                        createHardcodedMilestones();
                    } else {
                        // Load from Firebase
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Milestone m = doc.toObject(Milestone.class);
                            if (m != null) {
                                m.setId(doc.getId());
                                masterMilestones.add(m);
                            }
                        }
                        sortMilestonesByOrder(masterMilestones);
                    }
                    // *** CENTRAL HUB: Now that we have master milestones, process them ***
                    processUserMilestones();
                })
                .addOnFailureListener(e -> {
                    if (isDestroyed || isFinishing()) return;
                    // Fallback to hardcoded milestones
                    masterMilestones.clear();
                    createHardcodedMilestones();
                    // *** CENTRAL HUB: Now that we have master milestones, process them ***
                    processUserMilestones();
                });
    }

    private void createHardcodedMilestones() {
        if (isDestroyed) return;

        // Fallback: Create milestones based on XP (simple approach)
        masterMilestones.clear();
        masterMilestones.add(new Milestone("Beginner", "Complete your first lesson", 100, 1));
        masterMilestones.add(new Milestone("Getting Started", "Complete 5 lessons", 250, 2));
        masterMilestones.add(new Milestone("Regular Learner", "Complete 10 lessons", 500, 3));
        masterMilestones.add(new Milestone("Dedicated Student", "Complete 25 lessons", 750, 4));
        masterMilestones.add(new Milestone("Advanced Learner", "Complete 50 lessons", 1000, 5));
        masterMilestones.add(new Milestone("Expert Student", "Complete 100 lessons", 1500, 6));
        masterMilestones.add(new Milestone("Master Learner", "Complete 200 lessons", 2000, 7));
        masterMilestones.add(new Milestone("Learning Legend", "Complete 500 lessons", 3000, 8));

        // Use XP-based calculation for status
        calculateMilestonesFromXP();
    }

    private void calculateMilestonesFromXP() {
        userMilestones.clear();
        for (Milestone m : masterMilestones) {
            boolean meetsRequirements = currentUserXP >= m.getXpReward();
            String status = meetsRequirements ? "Completed" : "Locked";

            Milestone userM = new Milestone(m.getName(), m.getDescription(), m.getXpReward(), meetsRequirements, status);
            userM.setOrder(m.getOrder());

            userMilestones.add(userM);
        }
        sortMilestonesByOrder(userMilestones);
        unlockFirstIncompleteMilestone(); // Make sure the next one is "In Progress"
        // *** FINAL UI UPDATE CALL ***
        updateUI();
    }

    private void initializeUserMilestones() {
        if (isDestroyed) return;

        db.collection("userMilestones")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (isDestroyed || isFinishing()) return;

                    if (querySnapshot.isEmpty()) {
                        // New user: create milestones for them (ALL LOCKED initially)
                        createUserMilestones();
                    } else {
                        // Existing user: load progress and check requirements
                        loadUserMilestonesFromSnapshot(querySnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    if (isDestroyed || isFinishing()) return;

                    Toast.makeText(this, "Error loading milestones", Toast.LENGTH_SHORT).show();
                    // Fallback to XP-based calculation
                    calculateMilestonesFromXP();
                });
    }

    //Central point for handling user milestones
    private void processUserMilestones() {
        if (isDestroyed) return;

        db.collection("userMilestones")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (isDestroyed || isFinishing()) return;

                    userMilestones.clear(); // *** CRITICAL: Clear the list before processing ***

                    if (querySnapshot.isEmpty()) {
                        // New user: create their milestones based on the master list
                        createUserMilestones();
                    } else {
                        // Existing user: load their progress
                        loadUserMilestonesFromSnapshot(querySnapshot);
                    }
                })
                .addOnFailureListener(e -> {
                    // If we can't get user-specific milestones, fall back to a simple XP check
                    // This prevents the user from seeing a broken screen
                    Toast.makeText(this, "Could not load progress, showing estimated status.", Toast.LENGTH_SHORT).show();
                    calculateMilestonesFromXP();
                });
    }

    private void createUserMilestones() {
        // This method now only calculates the initial state. The saving to Firebase can happen
        // in the background and shouldn't block the UI update.
        for (Milestone master : masterMilestones) {
            boolean isFirst = master.getOrder() == 1;
            String status = isFirst ? "In Progress" : "Locked";
            Milestone userM = new Milestone(master.getName(), master.getDescription(), master.getXpReward(), false, status);
            userM.setOrder(master.getOrder());
            userM.setId(master.getId());
            userMilestones.add(userM);

            // Save this initial state to Firebase in the background
            saveNewUserMilestoneToFirebase(userM);
        }
        sortMilestonesByOrder(userMilestones);
        // *** FINAL UI UPDATE CALL ***
        updateUI();
    }

    private void saveNewUserMilestoneToFirebase(Milestone milestone) {
        Map<String, Object> progress = new HashMap<>();
        progress.put("userId", userId);
        progress.put("milestoneId", milestone.getId());
        progress.put("completed", milestone.isCompleted());
        progress.put("status", milestone.getStatus());
        progress.put("order", milestone.getOrder());

        db.collection("userMilestones").add(progress)
                .addOnFailureListener(e ->
                        Log.e("MilestonesActivity", "Failed to save initial milestone", e));
    }

    private void loadUserMilestonesFromSnapshot(QuerySnapshot querySnapshot) {
        Map<String, Milestone> userMilestoneMap = new HashMap<>();

        // First, create a map of the user's progress from Firebase
        for (DocumentSnapshot doc : querySnapshot) {
            String milestoneId = doc.getString("milestoneId");
            Milestone temp = new Milestone();
            temp.setCompleted(doc.getBoolean("completed") != null && doc.getBoolean("completed"));
            temp.setStatus(doc.getString("status"));
            if (milestoneId != null) {
                userMilestoneMap.put(milestoneId, temp);
            }
        }

        // Now, build the final list using the master list as the source of truth
        for (Milestone master : masterMilestones) {
            Milestone userProgress = userMilestoneMap.get(master.getId());
            if (userProgress != null) {
                // User has progress for this milestone, use it
                Milestone finalMilestone = new Milestone(master.getName(), master.getDescription(), master.getXpReward(), userProgress.isCompleted(), userProgress.getStatus());
                finalMilestone.setOrder(master.getOrder());
                finalMilestone.setId(master.getId());
                userMilestones.add(finalMilestone);
            } else {
                // User is missing this milestone (maybe it was added later), treat as locked
                Milestone missingMilestone = new Milestone(master.getName(), master.getDescription(), master.getXpReward(), false, "Locked");
                missingMilestone.setOrder(master.getOrder());
                missingMilestone.setId(master.getId());
                userMilestones.add(missingMilestone);
            }
        }

        sortMilestonesByOrder(userMilestones);
        checkAndUpdateMilestoneRequirements(); // Check if any milestones can be completed
        // *** FINAL UI UPDATE CALL ***
        updateUI();
    }

    // Helper method to sort milestones by order
    private void sortMilestonesByOrder(List<Milestone> milestones) {
        Collections.sort(milestones, new Comparator<Milestone>() {
            @Override
            public int compare(Milestone m1, Milestone m2) {
                return Integer.compare(m1.getOrder(), m2.getOrder());
            }
        });
    }

    // METHOD TO CHECK IF USER MEETS MILESTONE REQUIREMENTS
    private boolean checkMilestoneRequirements(Milestone milestone) {
        // Check if user has enough XP to complete this milestone
        return currentUserXP >= milestone.getXpReward();
    }

    // METHOD TO CHECK ALL MILESTONES AND UPDATE THEIR STATUS
    private void checkAndUpdateMilestoneRequirements() {
        if (isDestroyed) return;

        for (Milestone m : userMilestones) {
            if (!m.isCompleted()) {
                boolean meetsRequirements = currentUserXP >= m.getXpReward();
                if (meetsRequirements) {
                    m.setCompleted(true);
                    m.setStatus("Completed");
                    updateMilestoneInFirebase(m);
                }
            }
        }
        unlockFirstIncompleteMilestone();
    }

    private void unlockFirstIncompleteMilestone() {
        if (isDestroyed) return;

        // Find the FIRST non-completed milestone and set it to "In Progress"
        // All others should remain "Locked"
        boolean foundFirstIncomplete = false;

        for (Milestone m : userMilestones) {
            if (!m.isCompleted() && !foundFirstIncomplete) {
                // This is the first incomplete milestone - set to "In Progress"
                m.setStatus("In Progress");
                updateMilestoneInFirebase(m);
                foundFirstIncomplete = true;
            } else if (!m.isCompleted() && foundFirstIncomplete) {
                // All other incomplete milestones should be "Locked"
                if (!m.getStatus().equals("Locked")) {
                    m.setStatus("Locked");
                    updateMilestoneInFirebase(m);
                }
            }
        }
    }

    private void updateMilestoneInFirebase(Milestone milestone) {
        if (isDestroyed) return;

        // Update milestone status in Firebase
        db.collection("userMilestones")
                .whereEqualTo("userId", userId)
                .whereEqualTo("milestoneId", milestone.getId())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (isDestroyed || isFinishing()) return;

                    for (DocumentSnapshot doc : querySnapshot) {
                        db.collection("userMilestones").document(doc.getId())
                                .update("completed", milestone.isCompleted(),
                                        "status", milestone.getStatus());
                    }
                });
    }

    private void updateUI() {
        if (isDestroyed || isFinishing()) return;

        // Additional null checks for views
        if (milestonesListView == null || currentLevelText == null || totalXpText == null) {
            return;
        }

        if (userMilestones.isEmpty()) return;

        // Calculate current level based on completed milestones
        int currentLevel = 0;
        for (Milestone m : userMilestones) {
            if (m.isCompleted() && m.getOrder() > currentLevel) {
                currentLevel = m.getOrder();
            }
        }

        currentLevelText.setText("Current Level: " + (currentLevel == 0 ? 1 : currentLevel));

        // Find next milestone XP requirement
        int nextMilestoneXp = 0;
        for (Milestone m : masterMilestones) {
            if (m.getOrder() == currentLevel + 1) {
                nextMilestoneXp = m.getXpReward();
                break;
            }
        }

        if (nextMilestoneXp == 0 && !masterMilestones.isEmpty()) {
            nextMilestoneXp = masterMilestones.get(masterMilestones.size() - 1).getXpReward();
        }

        totalXpText.setText("Total XP: " + currentUserXP + " / " + nextMilestoneXp);

        // Set adapter - milestones are already sorted
        milestoneAdapter = new MilestoneAdapter(this, userMilestones);
        milestonesListView.setAdapter(milestoneAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelFirebaseOperations();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelFirebaseOperations();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        isCreatingMilestones = false;

        // Clear references to prevent memory leaks
        milestoneAdapter = null;
        milestonesListView = null;
        currentLevelText = null;
        totalXpText = null;

        if (masterMilestones != null) {
            masterMilestones.clear();
        }
        if (userMilestones != null) {
            userMilestones.clear();
        }
    }
}