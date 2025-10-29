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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.OnBackPressedCallback;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Source;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText usernameEditText, passwordEditText;
    private Button loginButton;
    private TextView createAccountPrompt, forgotPasswordPrompt;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private ProgressBar pb;
    private String userId;
    private int totalPoints;

    public interface GameDetailsCallback {
        void onDetailsFetched(int calculatedTotalPoints);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setStatusBarColor(Color.BLACK);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize views
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        createAccountPrompt = findViewById(R.id.createAccountPrompt);
        forgotPasswordPrompt = findViewById(R.id.forgotPasswordPrompt);
        pb = findViewById(R.id.pbLoading);

        setupClickListeners();
    }

    private void setupClickListeners() {
        //Ensures the application exits, instead going to the previous page
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // When the back button is pressed, finish all activities in the task.
                // This will exit the application.
                finishAffinity();
            }
        };
        // Add the callback to the dispatcher
        getOnBackPressedDispatcher().addCallback(this, callback);

        // Login button
        loginButton.setOnClickListener(v -> {
            String email = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                usernameEditText.setError("Email is required");
                return;
            }
            if (TextUtils.isEmpty(password)) {
                passwordEditText.setError("Password is required");
                return;
            }

            loginUser(email, password);
        });

        // Navigate to Signup
        createAccountPrompt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            finish();
        });

        // Navigate to Reset Password
        forgotPasswordPrompt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
        });
    }

    private void loginUser(String email, String password) {
        pb.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                        // Force fetch latest user data from Firestore
                        db.collection("users").document(userId)
                                .get(Source.SERVER)
                                .addOnSuccessListener(documentSnapshot -> {
                                    if (documentSnapshot.exists()) {
                                        Boolean isAdmin = documentSnapshot.getBoolean("isAdmin");

                                        String username = documentSnapshot.getString("username");
                                        String email2 = documentSnapshot.getString("email");

                                        setGameDetails(new GameDetailsCallback() {
                                            @Override
                                            public void onDetailsFetched(int calculatedTotalPoints) {
                                                // This code will only run AFTER the game details are ready.
                                                if (isAdmin != null && isAdmin) {
                                                    startActivity(new Intent(LoginActivity.this, AdminDashboardActivity.class));
                                                } else {
                                                    // Now use the correctly calculated total points
                                                    UserDetails.setUD(new UserDetails(username, userId, email2, calculatedTotalPoints));
                                                    startActivity(new Intent(LoginActivity.this, HomePage.class));
                                                }

                                                pb.setVisibility(View.GONE);
                                                finish();
                                            }
                                        });
                                    } else {
                                        pb.setVisibility(View.GONE);
                                        Toast.makeText(LoginActivity.this, "User data not found", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> Toast.makeText(LoginActivity.this, "Failed to fetch user data", Toast.LENGTH_SHORT).show());

                    } else {
                        // Handle failed login properly
                        pb.setVisibility(View.GONE);
                        String error = Objects.requireNonNull(task.getException()).getMessage();
                        Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void setGameDetails(GameDetailsCallback callback){
        db.collection("gamification")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                // This block executes only when the data is successfully retrieved.

            Map<String, Map<String, Object>> gameDataMap = new HashMap<>();

            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                String gameName = document.getString("game_type");
                Long points = document.getLong("total_points");
                Long streak = document.getLong("streak");

                if (gameName != null && points != null && streak != null) {
                    Map<String, Object> details = new HashMap<>();
                    details.put("points", points.intValue());
                    details.put("streak", streak.intValue());
                    gameDataMap.put(gameName, details);
                }
            }

            // Default values in case a game document is missing
            int mgPoints = 0, mgStreak = 0;
            int dbPoints = 0, dbStreak = 0;
            int crPoints = 0, crStreak = 0;

            // Safely get data for each game
            Map<String, Object> mgDetails = gameDataMap.get("Matching Game");
            if (mgDetails != null) {
                mgPoints = (int) mgDetails.get("points");
                mgStreak = (int) mgDetails.get("streak");
            }
            Map<String, Object> dbDetails = gameDataMap.get("Definition Builder");
            if (dbDetails != null) {
                dbPoints = (int) dbDetails.get("points");
                dbStreak = (int) dbDetails.get("streak");
            }
            Map<String, Object> crDetails = gameDataMap.get("Crossword");
            if (crDetails != null) {
                crPoints = (int) crDetails.get("points");
                crStreak = (int) crDetails.get("streak");
            }

            // Create the singleton tracker object
            GameDetailsTracker.setGdt(new GameDetailsTracker(mgStreak, dbStreak, crStreak, mgPoints, dbPoints, crPoints));

            // Calculate the total points
            int finalTotalPoints = mgPoints + dbPoints + crPoints;

            // *** Use the callback to return the result and signal completion ***
            callback.onDetailsFetched(finalTotalPoints);

        })
                .addOnFailureListener(e -> {
            // If fetching fails, create a default tracker and proceed
            Log.e("LoginActivity", "Failed to fetch game details.", e);
            GameDetailsTracker.setGdt(new GameDetailsTracker(0, 0, 0, 0, 0, 0));
            // Signal completion with 0 points
            callback.onDetailsFetched(0);
        });
    }
}

