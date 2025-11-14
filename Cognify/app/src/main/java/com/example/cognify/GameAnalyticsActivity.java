package com.example.cognify;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class GameAnalyticsActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RadioGroup gameTypeGroup;
    private RadioButton radioMatching, radioCrossword, radioDefinition;
    private TableLayout playerTable;
    private TextView playerCountText;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_analytics);
        getWindow().setStatusBarColor(Color.BLACK);

        db = FirebaseFirestore.getInstance();

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        gameTypeGroup = findViewById(R.id.gameTypeGroup);
        radioMatching = findViewById(R.id.radioMatching);
        radioCrossword = findViewById(R.id.radioCrossword);
        radioDefinition = findViewById(R.id.radioDefinition);
        playerTable = findViewById(R.id.playerTable);
        playerCountText = findViewById(R.id.playerCountText);

        // Setup toolbar back arrow
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Load default game (Matching)
        loadGamePlayers("Matching Game");

        // Handle radio group changes
        gameTypeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioMatching) {
                loadGamePlayers("Matching Game");
            } else if (checkedId == R.id.radioCrossword) {
                loadGamePlayers("Crossword");
            } else if (checkedId == R.id.radioDefinition) {
                loadGamePlayers("Definition Builder");
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Go back
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadGamePlayers(String gameType) {
        playerTable.removeAllViews();
        playerCountText.setText("Loading...");

        // Add table header
        TableRow headerRow = new TableRow(this);
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT);
        headerRow.setLayoutParams(params);

        String[] headers = {"Username", "UserID", "Plays"};
        int countHeaders = 0;
        for (String header : headers) {
            TableRow.LayoutParams newParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.33f);

            TextView tv = new TextView(this);
            tv.setText(header);
            tv.setTextColor(getResources().getColor(R.color.black));
            tv.setPadding(24, 16, 24, 16);
            tv.setTextSize(16f);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setBackgroundColor(0xFFBDBDBD); // gray header
            if (countHeaders == 0){
                 newParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.20f);
            }else if (countHeaders == 1){
                newParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.46f);
            }else if (countHeaders == 2){
                newParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.15f);
            }
            tv.setLayoutParams(newParams);
            headerRow.addView(tv);
            countHeaders++;
        }
        playerTable.addView(headerRow);

        db.collection("gamification")
                .whereEqualTo("game_type", gameType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Integer> userPlayCounts = new HashMap<>();

                    // Count plays per user
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String userId = doc.getString("userId"); // make sure field matches Firestore
                        if (userId != null) {
                            int count = userPlayCounts.getOrDefault(userId, 0);
                            userPlayCounts.put(userId, count + 1);
                        }
                    }

                    playerCountText.setText("Total Players: " + userPlayCounts.size());

                    if (userPlayCounts.isEmpty()) {
                        TableRow emptyRow = new TableRow(this);
                        TextView emptyText = new TextView(this);
                        emptyText.setText("No players have played this game yet.");
                        emptyText.setTextColor(getResources().getColor(R.color.black));
                        emptyText.setPadding(24, 16, 24, 16);
                        emptyText.setTextSize(16f);
                        emptyRow.addView(emptyText);
                        playerTable.addView(emptyRow);
                        return;
                    }

                    int rowIndex = 0;
                    for (Map.Entry<String, Integer> entry : userPlayCounts.entrySet()) {
                        String uid = entry.getKey();
                        int playCount = entry.getValue();

                        int finalRowIndex = rowIndex;
                        db.collection("users").document(uid).get()
                                .addOnSuccessListener(userDoc -> {
                                    String username = userDoc.exists() ? userDoc.getString("username") : "Unknown";

                                    TableRow row = new TableRow(this);

                                    int bgColor = (finalRowIndex % 2 == 0) ? 0xFFFFFFFF : 0xFFF5F5F5; // alternating row color
                                    row.setBackgroundColor(bgColor);

                                    TextView usernameTV = new TextView(this);
                                    usernameTV.setText(username);
                                    usernameTV.setTextColor(getResources().getColor(R.color.black));
                                    usernameTV.setPadding(24, 12, 24, 12);

                                    TextView userIdTV = new TextView(this);
                                    userIdTV.setText(uid);
                                    userIdTV.setTextColor(getResources().getColor(R.color.black));
                                    userIdTV.setPadding(24, 12, 24, 12);

                                    TextView playsTV = new TextView(this);
                                    playsTV.setText(String.valueOf(playCount));
                                    playsTV.setTextColor(getResources().getColor(R.color.black));
                                    playsTV.setPadding(24, 12, 24, 12);

                                    TableRow.LayoutParams usernameParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.20f);
                                    TableRow.LayoutParams userIdParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.48f);
                                    TableRow.LayoutParams playsParams = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 0.15f);

                                    usernameTV.setLayoutParams(usernameParams);
                                    userIdTV.setLayoutParams(userIdParams);
                                    playsTV.setLayoutParams(playsParams);

                                    row.addView(usernameTV);
                                    row.addView(userIdTV);
                                    row.addView(playsTV);

                                    playerTable.addView(row);
                                })
                                .addOnFailureListener(e -> Toast.makeText(this,
                                        "Failed to fetch user: " + e.getMessage(),
                                        Toast.LENGTH_SHORT).show());

                        rowIndex++;
                    }

                })
                .addOnFailureListener(e -> {
                    playerCountText.setText("Total Players: 0");
                    Toast.makeText(this, "Failed to fetch game data: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}
