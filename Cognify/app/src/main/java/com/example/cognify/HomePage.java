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
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class HomePage extends AppCompatActivity {

    // Declare views as class members for easy access
    private ImageView ivProfilePicture;
    private TextView tvHello;
    private TextView tvName;
    private ImageView ivBellIcon;
    private ImageView ivMenuIcon;
    private TextView tvStreakCountMG;
    private TextView tvStreakCountDB;
    private TextView tvStreakCountCR;
    // Add more if needed, e.g., for day circles or titles

    private LinearLayout llMatchingGame,llDayCircles;

    private LinearLayout llDefinitionBuilder;

    private LinearLayout llCrossword;

    private LinearLayout llCourse1;

    private LinearLayout llCourse2;

    private LinearLayout llCourse3;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);  // Link to your XML layout

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Find views by ID
        ivProfilePicture = findViewById(R.id.ivProfilePicture);
        tvHello = findViewById(R.id.tvHello);
        tvName = findViewById(R.id.tvName);
        ivBellIcon = findViewById(R.id.ivBellIcon);
        ivMenuIcon = findViewById(R.id.ivMenuIcon);
        tvStreakCountMG = findViewById(R.id.tvStreakCountMG);
        tvStreakCountDB = findViewById(R.id.tvStreakCountDB);
        tvStreakCountCR = findViewById(R.id.tvStreakCountCR);

        // Find game cards (assuming you add IDs in XML)
        llMatchingGame = findViewById(R.id.llMatchingGame);  // Add ID in XML for this
        llDefinitionBuilder = findViewById(R.id.llDefinitionBuilder);  // Add ID
        llCrossword = findViewById(R.id.llCrossword);  // Add ID

        llCourse1 = findViewById(R.id.llCourse1);  // Add ID in XML for this
        llCourse2 = findViewById(R.id.llCourse2);  // Add ID
        llCourse3 = findViewById(R.id.llCourse3);  // Add ID

        llDayCircles = findViewById(R.id.llDayCircles);
        // Set dynamic data (e.g., from SharedPreferences or API)
        // For example:

//        String userName = "Kimberly";  // Fetch from data source
//        int streakCount = 11;  // Fetch from data source
//        tvName.setText(userName);
//        tvStreakCount.setText(String.valueOf(streakCount));
        getUserDetails();
        setOnClickListeners();
        highlightStreak();


        // Set up Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    // Already on home page, do nothing or refresh
                    Toast.makeText(HomePage.this, "Home Page", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_books) {
                    // Navigate to Books Activity
                    startActivity(new Intent(HomePage.this, AddAndViewInformation.class));
//                    Toast.makeText(HomePage.this, "Books (Not Implemented)", Toast.LENGTH_SHORT).show(); // Example toast
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    // Navigate to Profile Activity
                    startActivity(new Intent(HomePage.this, ProfileActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_games) {
                    // Navigate to Games Activity
                    startActivity(new Intent(HomePage.this, GamesScreen.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_settings) {
                    // Navigate to Settings Activity
                    startActivity(new Intent(HomePage.this, HelpActivity.class));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }



    private void setOnClickListeners() {
        // Set click listeners
        ivBellIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Bell Icon click (e.g., open notifications)
                Toast.makeText(HomePage.this, "Opening Notifications", Toast.LENGTH_SHORT).show();
                // You could start a new activity or fragment here
                // Intent intent = new Intent(HomePage.this, NotificationsActivity.class);
                // startActivity(intent);
            }
        });

        ivMenuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Menu Icon click (e.g., open a drawer or menu)
                Toast.makeText(HomePage.this, "Opening Menu", Toast.LENGTH_SHORT).show();
                // If you have a DrawerLayout, use:
                // DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                // drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Set click listeners for game cards
        llMatchingGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Matching Game click (e.g., start the game)
                Toast.makeText(HomePage.this, "Starting Matching Game", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(HomePage.this, MatchingGameActivity.class);
                // startActivity(intent);
            }
        });

        llDefinitionBuilder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Definition Builder click
                Toast.makeText(HomePage.this, "Starting Definition Builder", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(HomePage.this, DefinitionBuilderActivity.class);
                // startActivity(intent);
            }
        });

        llCrossword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Word Master click
                Toast.makeText(HomePage.this, "Starting Word Master", Toast.LENGTH_SHORT).show();
                // Intent intent = new Intent(HomePage.this, WordMasterActivity.class);
                // startActivity(intent);
            }
        });

        // Set click listeners for course cards (they say "Coming Soon")
        llCourse1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle Course 1 click
                Toast.makeText(HomePage.this, "Course 1: Coming Soon", Toast.LENGTH_SHORT).show();
                // You could show a dialog or navigate if available
            }
        });

        llCourse2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Course 2: Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });

        llCourse3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(HomePage.this, "Course 3: Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void getUserDetails() {
        // Check if the UserDetails singleton has been cleared (e.g., app was killed)
        if (UserDetails.getUsername() == null) {
            // If data is missing, re-fetch it from Firestore
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();
                fetchDataFromFirestore(userId);
            }
        } else {
            // If data is already present, just update the UI like before
            updateUiWithLocalData();
        }
    }

    // New method to keep the UI update logic separate
    private void updateUiWithLocalData() {
        if (UserDetails.getUsername() != null) {
            tvName.setText(UserDetails.getUsername());
        }

        GameDetailsTracker temp = GameDetailsTracker.getGdt();
        if (temp != null) {
            tvStreakCountMG.setText(String.valueOf(temp.getMatchingGameStreak()));
            tvStreakCountDB.setText(String.valueOf(temp.getDefinitionBuilderStreak()));
            tvStreakCountCR.setText(String.valueOf(temp.getCrosswordStreak()));
        }
    }

    //Method to fetch all required data from Firestore
    private void fetchDataFromFirestore(String userId) {
        // 1. Fetch User Document
        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String username = documentSnapshot.getString("username");
                String email = documentSnapshot.getString("email");

                // 2. Fetch Gamification Data
                db.collection("gamification").whereEqualTo("userId", userId).get().addOnSuccessListener(querySnapshot -> {
                    // Use the same logic from your LoginActivity to parse game data
                    int mgPoints = 0, mgStreak = 0;
                    int dbPoints = 0, dbStreak = 0;
                    int crPoints = 0, crStreak = 0;

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String gameType = doc.getString("game_type");
                        if (gameType == null) continue;

                        int points = doc.getLong("total_points").intValue();
                        int streak = doc.getLong("streak").intValue();

                        switch (gameType) {
                            case "Matching Game":{
                                mgPoints = points;
                                mgStreak = streak;
                                break;
                            }
                            case "Definition Builder": {
                                dbPoints = points;
                                dbStreak = streak;
                                break;
                            }
                            case "Crossword": {
                                crPoints = points;
                                crStreak = streak;
                                break;
                            }
                        }
                    }

                    int totalPoints = mgPoints + dbPoints + crPoints;

                    // 3. Re-populate the singletons
                    UserDetails.setUD(new UserDetails(username, userId, email, totalPoints));
                    GameDetailsTracker.setGdt(new GameDetailsTracker(mgStreak, dbStreak, crStreak, mgPoints, dbPoints, crPoints));

                    // 4. Update the UI with the newly fetched data
                    updateUiWithLocalData();

                }).addOnFailureListener(e -> {
                    // Handle failure to get game data
                    Toast.makeText(this, "Failed to load game stats.", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            // Handle failure to get user data
            Toast.makeText(this, "Failed to load user profile.", Toast.LENGTH_SHORT).show();
        });
    }

    private void highlightStreak() {
        if (llDayCircles == null) return;

        int todayIndex = mapDayOfWeekToIndex(Calendar.getInstance().get(Calendar.DAY_OF_WEEK));
        for (int i = 0; i < llDayCircles.getChildCount(); i++) {
            TextView day = (TextView) llDayCircles.getChildAt(i);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);

            if (i < todayIndex) {
                circle.setColor(Color.LTGRAY);
                day.setTextColor(Color.BLACK);
            } else if (i == todayIndex) {
                circle.setColor(Color.parseColor("#FF9800"));
                day.setTextColor(Color.WHITE);
            } else {
                circle.setColor(Color.parseColor("#DDDDDD"));
                day.setTextColor(Color.BLACK);
            }
            circle.setStroke(3, Color.BLACK);
            day.setBackground(circle);
        }
    }

    private int mapDayOfWeekToIndex(int calendarDay) {
        switch (calendarDay) {
            case Calendar.MONDAY: return 0;
            case Calendar.TUESDAY: return 1;
            case Calendar.WEDNESDAY: return 2;
            case Calendar.THURSDAY: return 3;
            case Calendar.FRIDAY: return 4;
            case Calendar.SATURDAY: return 5;
            case Calendar.SUNDAY: return 6;
            default: return 0;
        }
    }
    @Override
    public void onResume(){
        super.onResume();
        getUserDetails();
        updateUiWithLocalData();
        highlightStreak();

    }
}