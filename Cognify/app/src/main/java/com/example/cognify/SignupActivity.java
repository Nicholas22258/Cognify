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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText emailEditText, usernameEditText, passwordEditText;
    private Button signupButton, googleSignupButton;
    private TextView loginPrompt;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        emailEditText = findViewById(R.id.emailEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signupButton = findViewById(R.id.signupButton);
        googleSignupButton = findViewById(R.id.googleSignupButton);
        loginPrompt = findViewById(R.id.loginPrompt);
        pb = findViewById(R.id.pbLoading);
    }

    private void setupClickListeners() {
        // Handle Email & Password Signup
        signupButton.setOnClickListener(v -> {
            if (validateInput()) {
                String email = emailEditText.getText().toString().trim();
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                createUser(email, password, username);
            }
        });

        // Google Signup placeholder
        googleSignupButton.setOnClickListener(v ->
                Toast.makeText(SignupActivity.this, "Google Sign Up - Coming Soon!", Toast.LENGTH_SHORT).show()
        );

        // Go to Login Screen
        loginPrompt.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void createUser(String email, String password, String username) {
        pb.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            saveUserToFirestore(firebaseUser.getUid(), username, email);
                            pb.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Toast.makeText(SignupActivity.this,
                                "Signup failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                        pb.setVisibility(View.GONE);
                    }
                });
    }

    private void saveUserToFirestore(String userId, String username, String email) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("userId", userId);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("profilePicUrl", "");
        userMap.put("joinDate", new Date());
        userMap.put("isActive", true);
        userMap.put("isAdmin", false);
        userMap.put("totalMaterialsUploaded", 0);
        userMap.put("totalGamesPlayed", 0);
        userMap.put("totalPoints", 0);
        userMap.put("lastActive", new Date());
        userMap.put("createdAt", System.currentTimeMillis());
//password is not stored as it is handled by Firebase authentication

        db.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    // After user is created, create milestones and badges
                    createUserMilestones(userId);
                    createUserBadges(userId);
                    setGamificationDocuments(userId);
                    UserDetails.setUD(new UserDetails(username, userId, email, 0));

                    Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, CompletionActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SignupActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    public void setGamificationDocuments(String userID){
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String monthString = "";

        switch (month){
            case 1: {
                monthString = "January";
                break;
            }
            case 2: {
                monthString = "February";
                break;
            }
            case 3: {
                monthString = "March";
                break;
            }
            case 4: {
                monthString = "April";
                break;
            }
            case 5: {
                monthString = "May";
                break;
            }
            case 6: {
                monthString = "June";
                break;
            }
            case 7: {
                monthString = "July";
                break;
            }
            case 8: {
                monthString = "August";
                break;
            }
            case 9: {
                monthString = "September";
                break;
            }
            case 10: {
                monthString = "October";
                break;
            }
            case 11: {
                monthString = "November";
                break;
            }
            case 12: {
                monthString = "December";
                break;
            }
        }

        String dateString = monthString + " " + day + ", " + year;

        Map<String, Object> gamification = new HashMap<>();
        gamification.put("userId", userID);
        gamification.put("game_type", "Crossword");
        gamification.put("total_points", 0);
        gamification.put("streak", 0);
        gamification.put("date_played", dateString);
        gamification.put("createdAt", System.currentTimeMillis());

        db.collection("gamification").document()
                .set(gamification)
                .addOnSuccessListener(aVoid -> {
                    /*Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, CompletionActivity.class);
                    startActivity(intent);*/
                    Log.d("CREATE GAMIFICATION DOCETS", "Crossword created successfully");
                    finish();
                })
                .addOnFailureListener(e ->
//                        Toast.makeText(SignupActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                Log.e("CREATE GAMIFICATION DOCETS", "Crossword not created: " + e)
                );

        gamification.clear();
        gamification.put("userId", userID);
        gamification.put("game_type", "Definition Builder");
        gamification.put("total_points", 0);
        gamification.put("streak", 0);
        gamification.put("date_played", dateString);
        gamification.put("createdAt", System.currentTimeMillis());

        db.collection("gamification").document()
                .set(gamification)
                .addOnSuccessListener(aVoid -> {
                    /*Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, CompletionActivity.class);
                    startActivity(intent);*/
                    Log.d("CREATE GAMIFICATION DOCETS", "Definition Builder created successfully");
                    finish();
                })
                .addOnFailureListener(e ->
//                        Toast.makeText(SignupActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                Log.e("CREATE GAMIFICATION DOCETS", "Definition Builder not created: " + e)
                );

        gamification.clear();
        gamification.put("userId", userID);
        gamification.put("game_type", "Matching Game");
        gamification.put("total_points", 0);
        gamification.put("streak", 0);
        gamification.put("date_played", dateString);
        gamification.put("createdAt", System.currentTimeMillis());

        db.collection("gamification").document()
                .set(gamification)
                .addOnSuccessListener(aVoid -> {
                    /*Toast.makeText(SignupActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, CompletionActivity.class);
                    startActivity(intent);*/
                    Log.d("CREATE GAMIFICATION DOCETS", "Matching Game created successfully");
                    finish();
                })
                .addOnFailureListener(e ->
//                        Toast.makeText(SignupActivity.this, "Error saving user: " + e.getMessage(), Toast.LENGTH_LONG).show()
                                Log.e("CREATE GAMIFICATION DOCETS", "Matching Game not created: " + e)
                );

        GameDetailsTracker.setGdt(new GameDetailsTracker(0,0,0,0,0,0));
    }

    // Method to create user milestones subcollection
    private void createUserMilestones(String userId) {
        // Define milestone data
        String[] titles = {
                "Beginner",
                "Getting Started",
                "Regular Learner",
                "Dedicated Student",
                "Advanced Learner",
                "Expert Student",
                "Master Learner",
                "Learning Legend"
        };

        String[] descriptions = {
                "Complete your first lesson",
                "Complete 5 lessons",
                "Complete 10 lessons",
                "Complete 25 lessons",
                "Complete 50 lessons",
                "Complete 100 lessons",
                "Complete 200 lessons",
                "Complete 500 lessons"
        };

        int[] xpThresholds = {100, 250, 500, 750, 1000, 1500, 2000, 3000};

        // Create each milestone document in subcollection
        for (int i = 0; i < titles.length; i++) {
            Map<String, Object> milestoneMap = new HashMap<>();
            milestoneMap.put("userId", userId);
            milestoneMap.put("title", titles[i]);
            milestoneMap.put("description", descriptions[i]);
            milestoneMap.put("xpRequired", xpThresholds[i]);
            milestoneMap.put("isCompleted", false);
            milestoneMap.put("progress", 0);
            milestoneMap.put("level", i + 1);

            db.collection("milestones")
                    .document()
                    .set(milestoneMap)
                    .addOnFailureListener(e -> {
                        // Log error but don't block signup
                        Log.e("CREATE BADGE DOCETS", "Milestone not created: " + e);
                    });
        }
    }

    // Method to create user badges subcollection
    private void createUserBadges(String userId) {
        // Define badge data
        String[] titles = {
                "First Steps",
                "Quick Learner",
                "Streak Master",
                "Quiz Champion",
                "Perfect Score",
                "Speed Demon",
                "Night Owl",
                "Early Bird",
                "Social Learner",
                "Dedicated",
                "Explorer",
                "Master"
        };

        String[] descriptions = {
                "Complete your first lesson",
                "Earn 100 XP",
                "Earn 250 XP",
                "Earn 500 XP",
                "Earn 1000 XP",
                "Earn 1200 XP",
                "Earn 1500 XP",
                "Earn 1800 XP",
                "Earn 2000 XP",
                "Earn 2500 XP",
                "Earn 2800 XP",
                "Earn 3000 XP"
        };

        int[] xpRequired = {10, 100, 250, 500, 1000, 1200, 1500, 1800, 2000, 2500, 2800, 3000};

        // Create each badge document in subcollection
        for (int i = 0; i < titles.length; i++) {
            Map<String, Object> badgeMap = new HashMap<>();
            badgeMap.put("userId", userId);
            badgeMap.put("title", titles[i]);
            badgeMap.put("description", descriptions[i]);
            badgeMap.put("xpRequired", xpRequired[i]);
            badgeMap.put("isEarned", false);
            badgeMap.put("earnedDate", null);

            db.collection("badges")
                    .document()
                    .set(badgeMap)
                    .addOnFailureListener(e -> {
                        // Log error but don't block signup
                        Log.e("CREATE BADGE DOCETS", "Badge not created: " + e);
                    });
        }
    }

    private boolean validateInput() {
        String email = emailEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            emailEditText.setError("Email is required");
            return false;
        }

        if (TextUtils.isEmpty(username)) {
            usernameEditText.setError("Username is required");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            passwordEditText.setError("Password must be at least 6 characters");
            return false;
        }

        return true;
    }
}