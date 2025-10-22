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
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.util.List;

public class GamesScreen extends AppCompatActivity {

    private ImageButton goToMatchingGame;
    private ImageButton goToDefinitionBuilder;
    private ImageButton goToCrossword;

    private ProgressBar loadingProgressBar;
    private boolean dataLoaded = true;

    private ImageView backArrow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.games_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
//        TermsAndDefinitions.loadDummyTsAndDs();
        goToMatchingGame = findViewById(R.id.matchingGame);
        goToDefinitionBuilder = findViewById(R.id.definitionBuilder);
        goToCrossword = findViewById(R.id.crossword);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
//        backArrow = findViewById(R.id.back_arrow);

        disableGameButtons();
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        if (AddAndViewInformation.courseIsSelected){
            enableGameButtons();
        }else{
            Toast.makeText(this, "Please select a course to start", Toast.LENGTH_LONG).show();
        }

//        backArrow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(GamesScreen.this, HomePage.class);
//                startActivity(intent);
//            }
//        });

        // Set up click listeners (they won't work until buttons are enabled)
        goToMatchingGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataLoaded) {
                    Intent intent = new Intent(GamesScreen.this, MatchingGame.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(GamesScreen.this, "Please wait, loading data...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        goToDefinitionBuilder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataLoaded) {
                    Intent intent = new Intent(GamesScreen.this, DefinitionBuilder.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(GamesScreen.this, "Please wait, loading data...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        goToCrossword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dataLoaded) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    Intent intent = new Intent(GamesScreen.this, Crossword.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(GamesScreen.this, "Please wait, loading data...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_games);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    // Already on home page, do nothing or refresh
                    startActivity(new Intent(GamesScreen.this, HomePage.class));
//                    Toast.makeText(GamesScreen.this, "Home Page", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_books) {
                    // Navigate to Books Activity
                    startActivity(new Intent(GamesScreen.this, AddAndViewInformation.class));
//                    Toast.makeText(HomePage.this, "Books (Not Implemented)", Toast.LENGTH_SHORT).show(); // Example toast
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    // Navigate to Profile Activity
                    startActivity(new Intent(GamesScreen.this, ProfileActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_games) {
                    // Navigate to Games Activity
//                    startActivity(new Intent(GamesScreen.this, GamesScreen.class));
                    Toast.makeText(GamesScreen.this, "GamesScreen", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_settings) {
                    // Navigate to Settings Activity
                    startActivity(new Intent(GamesScreen.this, HelpActivity.class));
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Load data when returning from AddAndViewInformation activity
        loadLastSelectedPdf();
    }

    private void loadLastSelectedPdf() {
        SharedPreferences settings = getSharedPreferences(AddAndViewInformation.PREFS_NAME, 0);
        String uriString = settings.getString(AddAndViewInformation.PREF_LAST_SELECTED_URI, null);
        String courseName = settings.getString(AddAndViewInformation.PREF_LAST_SELECTED_NAME, null);

        if (uriString != null && courseName != null) {
            Uri uri = Uri.parse(uriString);

            // Show loading indicator
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.VISIBLE);
            }

            // Load PDF data in background
            new Thread(() -> {
                try {
                    String textFromPDF = readPdfFromUri(uri);
                    loadToTsAndDsList(textFromPDF);

                    // Update UI on main thread
                    runOnUiThread(() -> {
                        dataLoaded = true;
                        AddAndViewInformation.courseIsSelected = true;
                        AddAndViewInformation.courseName = courseName;

                        if (loadingProgressBar != null) {
                            loadingProgressBar.setVisibility(View.GONE);
                        }

                        enableGameButtons();
                        Toast.makeText(GamesScreen.this,
                                "Loaded: " + courseName + " (" + TermsAndDefinitions.TsAndDs.size() + " terms)",
                                Toast.LENGTH_SHORT).show();
                    });

                } catch (IOException | SecurityException e) {
                    Log.e("MainActivity", "Error loading PDF", e);
                    runOnUiThread(() -> {
                        if (loadingProgressBar != null) {
                            loadingProgressBar.setVisibility(View.GONE);
                        }
                        Toast.makeText(GamesScreen.this,
                                "Failed to load PDF. Please select a course.",
                                Toast.LENGTH_LONG).show();
                        disableGameButtons();
                    });
                }
            }).start();

        } else {
            // No PDF selected yet
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(View.GONE);
            }
            Toast.makeText(this, "Please select a course to start", Toast.LENGTH_LONG).show();
            disableGameButtons();
        }
    }

    private String readPdfFromUri(Uri uri) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        PdfReader reader = new PdfReader(getContentResolver().openInputStream(uri));
        int n = reader.getNumberOfPages();
        for (int i = 0; i < n; i++) {
            stringBuilder.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim()).append("\n");
        }
        reader.close();
        return stringBuilder.toString();
    }

    private void loadToTsAndDsList(String textFromPDF) {
        final String pairDelimiter = ";";
        final String termDefinitionDelimiter = ":";

        String term;
        String definition;
        String[] termDefPair;

        // Clear previous data
        TermsAndDefinitions.TsAndDs.clear();

        String[] TsAndDs = textFromPDF.split(pairDelimiter);
        for (int i = 0; i < TsAndDs.length; i++) {
            termDefPair = TsAndDs[i].trim().split(termDefinitionDelimiter, 2);

            if (termDefPair.length > 1) {
                term = termDefPair[0].trim();
                definition = termDefPair[1].trim();

                if (!term.isEmpty() && !definition.isEmpty()) {
                    TermsAndDefinitions termDefinition = new TermsAndDefinitions(i, term, definition);
                    TermsAndDefinitions.TsAndDs.add(termDefinition);
                }
            }
        }
    }


    private void disableGameButtons() {
        goToMatchingGame.setEnabled(false);
        goToDefinitionBuilder.setEnabled(false);
        goToCrossword.setEnabled(false);

        // Optional: Make them visually appear disabled
        goToMatchingGame.setAlpha(0.5f);
        goToDefinitionBuilder.setAlpha(0.5f);
        goToCrossword.setAlpha(0.5f);
    }

    private void enableGameButtons() {
        goToMatchingGame.setEnabled(true);
        goToDefinitionBuilder.setEnabled(true);
        goToCrossword.setEnabled(true);

        // Restore full opacity
        goToMatchingGame.setAlpha(1.0f);
        goToDefinitionBuilder.setAlpha(1.0f);
        goToCrossword.setAlpha(1.0f);
    }
}