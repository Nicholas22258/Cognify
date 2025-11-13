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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.util.List;

public class GamesScreen extends AppCompatActivity {
    private PdfLoader pdfLoader;

    private ImageButton goToMatchingGame;
    private ImageButton goToDefinitionBuilder;
    private ImageButton goToCrossword;

    private ProgressBar loadingProgressBar;
    private boolean dataLoaded = true;

    private DrawerLayout drawerLayout;
    private ImageView ivMenu;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
//        EdgeToEdge.enable(this);
        setContentView(R.layout.games_screen);
        getWindow().setStatusBarColor(Color.BLACK);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pdfLoader = new PdfLoader();

        drawerLayout = findViewById(R.id.drawer_layout);
        ivMenu = findViewById(R.id.ivMenu);
        navigationView = findViewById(R.id.navigationView);

        goToMatchingGame = findViewById(R.id.matchingGame);
        goToDefinitionBuilder = findViewById(R.id.definitionBuilder);
        goToCrossword = findViewById(R.id.crossword);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);

        ivMenu.setOnClickListener(v ->{
            drawerLayout.openDrawer(GravityCompat.START);
        });

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemID = item.getItemId();

            if (itemID == R.id.homepage){
                startActivity(new Intent(GamesScreen.this, HomePage.class));
            }else if (itemID == R.id.information){
                startActivity(new Intent(GamesScreen.this, AddAndViewInformation.class));
            }else if (itemID == R.id.profile){
                startActivity(new Intent(GamesScreen.this, ProfileActivity.class));
            }else if (itemID == R.id.games){
                Toast.makeText(GamesScreen.this, "Games Page", Toast.LENGTH_SHORT).show();
            }else if (itemID == R.id.feedback){
                startActivity(new Intent(GamesScreen.this, HelpActivity.class));
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        disableGameButtons();
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }

        if (AddAndViewInformation.courseIsSelected){
            enableGameButtons();
        }else{
            Toast.makeText(this, "Please select a course to start", Toast.LENGTH_LONG).show();
        }

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
        if (loadingProgressBar != null) {
            loadingProgressBar.setVisibility(View.VISIBLE);
        }
        disableGameButtons();

        pdfLoader.loadLastSelectedPdf(this, new PdfLoader.PdfLoaderListener() {
            @Override
            public void onPdfLoaded(String courseName, int termCount) {
                // This runs on success
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
                enableGameButtons();
                Toast.makeText(GamesScreen.this,
                        "Loaded: " + courseName, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPdfLoadFailed(String errorMessage) {
                // This runs on failure
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
                disableGameButtons();
                Toast.makeText(GamesScreen.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
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