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

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class PostGameScreen extends AppCompatActivity {
    private static String playedGame;
    private static int pointsForCurrentGame;

    private static String timePlayed;

    private AtomicBoolean isUpdating = new AtomicBoolean(false);

    private Button continueButton;

    private ProgressBar loadingProgressBar;

    FirebaseFirestore db;

    DocumentReference docRef = null;

    TextView pointsTextView;
    TextView timeTextView;

    List<String> finishMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_post_game_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pointsTextView = findViewById(R.id.Points);
        timeTextView = findViewById(R.id.Time);
        continueButton = findViewById(R.id.btnContinue);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        TextView congratsMessage = findViewById(R.id.congratulations);

        db = FirebaseFirestore.getInstance();

        finishMessages.add("That was amazing!");
        finishMessages.add("Nicely done! Keep it up!");
        finishMessages.add("You are a master learner!");
        finishMessages.add("Woah, that was cool!");
        finishMessages.add("That's some top notch work.");
        finishMessages.add("Fantastic!");
        Collections.shuffle(finishMessages);
        congratsMessage.setText(finishMessages.get(0));

        showToUser();

        continueButton.setEnabled(false);
        continueButton.setAlpha(0.5f);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable(){
            @Override
            public void run() {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                }
                performUpdates();
                continueButton.setAlpha(1.0f);
            }
        };
        handler.postDelayed(runnable, 1500);
        loadingProgressBar.setVisibility(View.GONE);

        continueButton = findViewById(R.id.btnContinue);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isUpdating.get()) {
                    Toast.makeText(PostGameScreen.this,
                            "Saving your progress...", Toast.LENGTH_SHORT).show();
                    return;
                }
//                Intent intent = new Intent(PostGameScreen.this, GamesScreen.class);
//                startActivity(intent);
                finish();
            }
        });

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Show your alert dialog here
                new AlertDialog.Builder(PostGameScreen.this)
                        .setTitle("Leave Screen?")
                        .setMessage("Are you sure you want to exit? All progressed will be lost.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        })
                        .setNegativeButton("No", null) // If 'No' is clicked, do nothing (dialog dismissed)
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    public static void setActivity(String activity){
        playedGame = activity;
    }

    public static void setTimePlayed(String timePlayedForGame){
        timePlayed = timePlayedForGame;
    }

    private void performUpdates(){
        if (playedGame == null || UserDetails.getUserID() == null) {
            Toast.makeText(this, "Error: Game data or user is invalid.", Toast.LENGTH_SHORT).show();
            enableContinueButton(); // Make sure the button is usable
            return;
        }

        isUpdating.set(true);
        continueButton.setEnabled(false);
        continueButton.setText("Saving...");

        db.collection("gamification")
                .whereEqualTo("userId", UserDetails.getUserID())
                .whereEqualTo("game_type", playedGame)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            // 2. We found the document. Get it.
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            // Get the DocumentReference to use in processUpdates
                            docRef = document.getReference();

                            // 3. Proceed with your existing update logic
                            processUpdates(document);

                            // The rest of your UI update logic
                            Handler handler = new Handler(Looper.getMainLooper());
                            Runnable runnable = new Runnable(){
                                @Override
                                public void run() {
                                    if (loadingProgressBar != null) {
                                        loadingProgressBar.setVisibility(View.GONE);
                                    }
                                    continueButton.setAlpha(1.0f);
                                }
                            };
                            handler.postDelayed(runnable, 2000);

                        } else {
                            // Document not found for this user and game
                            handleUpdateError("Game data not found for user.");
                        }
                    } else {
                        // The query itself failed (e.g., permissions issue)
                        handleUpdateError("Failed to retrieve game data: " + task.getException().getMessage());
                    }
                });
    }

    private void processUpdates(DocumentSnapshot document) {
//        boolean pointsUpdatedForSingleGame = false;
//        boolean totalPointsUpdated = false;
        // Calculate streak update
        long newStreak = calculateStreakValue(document);

        // Get current points and add new points
        Long currentTotalPoints = document.getLong("total_points");
        if (currentTotalPoints == null) {
            currentTotalPoints = 0L;
        }
        long updatedTotalPoints = currentTotalPoints + pointsForCurrentGame;

        // Prepare batch update
        Map<String, Object> updates = new HashMap<>();
        updates.put("streak", newStreak);
        updates.put("total_points", updatedTotalPoints);
        updates.put("last_played", Timestamp.now());

        // Perform the update
        docRef.update(updates)
                .addOnSuccessListener(aVoid -> {
                    updateLocalGameDetailsTracker(playedGame, (int) newStreak, (int) updatedTotalPoints);
                    Toast.makeText(PostGameScreen.this,
                            "Progress saved successfully!", Toast.LENGTH_SHORT).show();
                    enableContinueButton();
                    updateUserTotalPoints(pointsForCurrentGame, (int) newStreak, (int) updatedTotalPoints);
                })
                .addOnFailureListener(e -> {
                    handleUpdateError("Failed to save progress: " + e.getMessage());
                });
    }

    private void updateUserTotalPoints(int pointsToAdd, int newStreak, int gameTotalPoints) {
        String userId = UserDetails.getUserID();
        if (userId == null) {
            handleUpdateError("User ID not found");
            return;
        }

        DocumentReference userDocRef = db.collection("users").document(userId);

        userDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentUserTotalPoints = documentSnapshot.getLong("totalPoints");
                        if (currentUserTotalPoints == null) {
                            currentUserTotalPoints = 0L;
                        }
                        long updatedUserTotalPoints = currentUserTotalPoints + pointsToAdd;

                        // Update the user document
                        userDocRef.update("totalPoints", updatedUserTotalPoints)
                                .addOnSuccessListener(aVoid2 -> {
                                    updateLocalGameDetailsTracker(playedGame, newStreak, gameTotalPoints);
                                    Toast.makeText(PostGameScreen.this,
                                            "Progress saved successfully!", Toast.LENGTH_SHORT).show();
                                    enableContinueButton();
                                })
                                .addOnFailureListener(e -> {
                                    handleUpdateError("Failed to update user points: " + e.getMessage());
                                });
                    } else {
                        handleUpdateError("User document not found");
                    }
                })
                .addOnFailureListener(e -> {
                    handleUpdateError("Failed to retrieve user data: " + e.getMessage());
                });
    }

    private long calculateStreakValue(DocumentSnapshot document) {
        Timestamp dateLastPlayedTimestamp = document.getTimestamp("last_played");
        Long currentStreak = document.getLong("streak");

        if (currentStreak == null) {
            currentStreak = 0L;
        }

        if (dateLastPlayedTimestamp == null) {
            // First time playing this game
            return 1L;
        }

        Date dateLastPlayed = dateLastPlayedTimestamp.toDate();

        // Convert to Instant for modern date handling
        Instant lastPlayedInstant = dateLastPlayed.toInstant();
        Instant currentInstant = Instant.now();

        // Use device's time zone
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime lastPlayedZoned = lastPlayedInstant.atZone(zoneId);
        ZonedDateTime currentZoned = currentInstant.atZone(zoneId);

        // Truncate to start of day
        ZonedDateTime startOfLastPlayedDay = lastPlayedZoned.truncatedTo(ChronoUnit.DAYS);
        ZonedDateTime startOfCurrentDay = currentZoned.truncatedTo(ChronoUnit.DAYS);

        // Calculate days between
        long daysBetween = ChronoUnit.DAYS.between(startOfLastPlayedDay, startOfCurrentDay);

        if (daysBetween > 1) {
            // Streak broken - reset to 1
            return 1L;
        } else if (daysBetween == 1) {
            // Played yesterday - extend streak
            return currentStreak + 1;
        } else {
            // Played today already - keep current streak
            return currentStreak;
        }
    }

    private void enableContinueButton() {
        isUpdating.set(false);
        continueButton.setEnabled(true);
        continueButton.setText("Continue");
    }

    private void handleUpdateError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        enableContinueButton();
    }

    private void showToUser(){
        setPoints();
        pointsTextView.setText(Integer.toString(pointsForCurrentGame));
        timeTextView.setText(timePlayed);
    }

    private void setPoints(){
        pointsForCurrentGame = PointsTracker.getPointsForCurrentGame();
    }

    private void updateLocalGameDetailsTracker(String gameType, int newStreak, int newTotalPoints) {
        GameDetailsTracker gdt = GameDetailsTracker.getGdt();
        if (gdt != null) {
            switch (gameType) {
                case "Matching Game":
                    gdt.setMatchingGameStreak(newStreak);
                    gdt.setMatchingGamePoints(newTotalPoints);
                    break;
                case "Definition Builder":
                    gdt.setDefinitionBuilderStreak(newStreak);
                    gdt.setDefinitionBuilderPoints(newTotalPoints);
                    break;
                case "Crossword":
                    gdt.setCrosswordStreak(newStreak);
                    gdt.setCrosswordPoints(newTotalPoints);
                    break;
            }

            // Also update the total points in UserDetails
            UserDetails userDetails = UserDetails.getUD();
            if (userDetails != null) {
                int currentTotal = userDetails.getTotalPoints(); // You'll need to add a getter
                int difference = pointsForCurrentGame;
                userDetails.setTotalPoints(currentTotal + difference); // You'll need to add a setter
            }
        }
    }
}