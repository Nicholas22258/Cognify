package com.example.cognify;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;


public class HelpActivity extends AppCompatActivity {

    private EditText feedbackInput;
    private Button feedbackCancelBtn, feedbackSubmitBtn;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_feeback); // your XML

        // Find views
        feedbackInput = findViewById(R.id.feedbackInput);
        feedbackSubmitBtn = findViewById(R.id.helpSubmitBtn);
        feedbackCancelBtn = findViewById(R.id.feedbackCancelBtn);
        db = FirebaseFirestore.getInstance();

        feedbackSubmitBtn.setEnabled(false);

        feedbackInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                feedbackSubmitBtn.setEnabled(s.toString().trim().length() > 0);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        feedbackCancelBtn.setOnClickListener(v -> feedbackInput.getText().clear());

        feedbackSubmitBtn.setOnClickListener(v -> {
            String feedbackText = feedbackInput.getText().toString().trim();
            if (!feedbackText.isEmpty()) {
                Report report = new Report();
                report.setUserId(UserDetails.getUserID());
                report.setUsername(UserDetails.getUsername());
                report.setMessage(feedbackText);
                report.setDateSent(new Timestamp(new java.util.Date()));
                report.setAddressed(false);

                sendReport(report);
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    // Already on home page, do nothing or refresh

                    startActivity(new Intent(HelpActivity.this, HomePage.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_books) {
                    // Navigate to Books Activity
                    startActivity(new Intent(HelpActivity.this, AddAndViewInformation.class));
//                    Toast.makeText(HomePage.this, "Books (Not Implemented)", Toast.LENGTH_SHORT).show(); // Example toast
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    // Navigate to Profile Activity
                    startActivity(new Intent(HelpActivity.this, ProfileActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_games) {
                    // Navigate to Games Activity
                    startActivity(new Intent(HelpActivity.this, GamesScreen.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_settings) {
                    // Navigate to Settings Activity
                    Toast.makeText(HelpActivity.this, "Home Page", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void sendReport(Report report) {
        db.collection("reports")
                .add(report)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(HelpActivity.this, "Report submitted!", Toast.LENGTH_SHORT).show();
                    feedbackInput.getText().clear();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HelpActivity.this, "Error submitting report. Please try again later.", Toast.LENGTH_LONG).show()
                );
    }
}
