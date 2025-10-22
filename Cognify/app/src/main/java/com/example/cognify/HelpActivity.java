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
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HelpActivity extends AppCompatActivity {

    private EditText feedbackInput;
    private Button feedbackCancelBtn, feedbackSubmitBtn;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_feeback); // replace with your XML name

        // Find views
        feedbackInput = findViewById(R.id.feedbackInput);
        feedbackSubmitBtn = findViewById(R.id.helpSubmitBtn);
        feedbackCancelBtn = findViewById(R.id.feedbackCancelBtn);
        db = FirebaseFirestore.getInstance();

        feedbackSubmitBtn.setEnabled(false);

        feedbackInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                feedbackSubmitBtn.setEnabled(s.toString().trim().length() > 0);
            }
            @Override public void afterTextChanged(Editable s) {

            }
        });

        feedbackCancelBtn.setOnClickListener(v -> feedbackInput.getText().clear());

        feedbackSubmitBtn.setOnClickListener(v -> {
            String feedbackText = feedbackInput.getText().toString().trim();
            if(!feedbackText.isEmpty()){
                Map<String, Object> feedbackData = new HashMap<>();
                feedbackData.put("message", feedbackText);
                feedbackData.put("userId", UserDetails.getUserID());
                feedbackData.put("date_sent", new Date());

                sendFeedback(feedbackData);

                Toast.makeText(this, "Feedback submitted!", Toast.LENGTH_SHORT).show();
                feedbackInput.getText().clear();
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_settings);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    // Already on home page, do nothing or refresh
//                    Toast.makeText(HelpActivity.this, "Home Page", Toast.LENGTH_SHORT).show();
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
//                    startActivity(new Intent(HelpActivity.this, HelpActivity.class));
                    Toast.makeText(HelpActivity.this, "Help Page", Toast.LENGTH_SHORT).show();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void sendFeedback(Map<String, Object> userFeedback){
        db.collection("reports").document()
                .set(userFeedback)
                .addOnSuccessListener(aVoid -> {
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HelpActivity.this, "Error submitting report. Please try again later.", Toast.LENGTH_LONG).show()
                );
    }
}
