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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/*The goal of this game is for the user to match a given term with a given definition. This game has five rounds.*/

public class MatchingGame extends AppCompatActivity {
    private final List<TermsAndDefinitions> TsAndDs = new ArrayList<>();//Will store TermsAndDefinitions objects
    private final Map<String, String> tdPair = new HashMap<>();//Will map terms to definitions

    /*
     * The variable arrays of 1 are the only way to call values in button clicks
     * */
    private final boolean[] defIsSelected = {false};//stores if a definition button has been selected
    private final boolean[] termIsSelected = {false};//stores if a term button has been selected

    final Button[] selectedDefinitionButton = {null};//Stores which definition button is currently selected
    final Button[] selectedTermButton = {null};//Stores which term button is currently selected

    //Buttons for Definitions
    private Button def1;
    private Button def2;
    private Button def3;
    private Button def4;
    private Button def5;

    //Buttons for Terms
    private Button term1;
    private Button term2;
    private Button term3;
    private Button term4;
    private Button term5;

    private int numCorrectAnswers = 0;
    private int totalWrongAnswers = 0;
    private int totalCorrectAnswers = 0;
    private int numLevelsSofar = 0;

    private String selectedDefinitionText = "";
    private String selectedTermText = "";

    TextView stopwatchTimer;

    Runnable stopWatchRunnable;
    Handler stopwatchHandler;

    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_matching_game);
        getWindow().setStatusBarColor(Color.BLACK);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        def1 = findViewById(R.id.Def1);
        def2 = findViewById(R.id.Def2);
        def3 = findViewById(R.id.Def3);
        def4 = findViewById(R.id.Def4);
        def5 = findViewById(R.id.Def5);

        term1 = findViewById(R.id.Term1);
        term2 = findViewById(R.id.Term2);
        term3 = findViewById(R.id.Term3);
        term4 = findViewById(R.id.Term4);
        term5 = findViewById(R.id.Term5);

        pb = findViewById(R.id.progressBar);
        pb.setMax(100);

        newLevel();//sets new game for user

        def1.setOnClickListener(v -> onDefinitionButtonClick(def1, def1.getText().toString().trim()));
        def2.setOnClickListener(v -> onDefinitionButtonClick(def2, def2.getText().toString().trim()));
        def3.setOnClickListener(v -> onDefinitionButtonClick(def3, def3.getText().toString().trim()));
        def4.setOnClickListener(v -> onDefinitionButtonClick(def4, def4.getText().toString().trim()));
        def5.setOnClickListener(v -> onDefinitionButtonClick(def5, def5.getText().toString().trim()));

        term1.setOnClickListener(v -> onTermButtonClick(term1, term1.getText().toString().trim()));
        term2.setOnClickListener(v -> onTermButtonClick(term2, term2.getText().toString().trim()));
        term3.setOnClickListener(v -> onTermButtonClick(term3, term3.getText().toString().trim()));
        term4.setOnClickListener(v -> onTermButtonClick(term4, term4.getText().toString().trim()));
        term5.setOnClickListener(v -> onTermButtonClick(term5, term5.getText().toString().trim()));

        stopwatchTimer = findViewById(R.id.stopwatchTimer);
        stopwatchHandler = new Handler();
        startStopwatch();

        ImageButton cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MatchingGame.this, GamesScreen.class);
                startActivity(intent);
            }
        });

        PointsTracker.resetPoints();

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Show your alert dialog here
                new AlertDialog.Builder(MatchingGame.this)
                        .setTitle("Exit Game?")
                        .setMessage("Are you sure you want to exit? All progressed will be lost.")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                stopStopwatch();
                                finish();
                            }
                        })
                        .setNegativeButton("No", null) // If 'No' is clicked, do nothing (dialog dismissed)
                        .show();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    /*
    * A single, reusable method to handle clicks on any definition button
    * 1) The user clicks the same button again to deselect it
    * 2) The user clicks a different definition button while one was already selected
    * 3) No definition button was selected, so select this button is selected
    * 4) Check for a match after any selection change
    * */
    private void onDefinitionButtonClick(Button clickedButton, String definitionText) {

        if (clickedButton == selectedDefinitionButton[0]) {
            //1)
            clickedButton.setBackgroundResource(R.drawable.mg_definition_button_format);
            selectedDefinitionText = "";
            defIsSelected[0] = false;
            selectedDefinitionButton[0] = null;
        }else if (defIsSelected[0]) {
            //2)
            selectedDefinitionButton[0].setBackgroundResource(R.drawable.mg_definition_button_format);
            clickedButton.setBackgroundResource(R.drawable.mg_definition_selected_format);
            selectedDefinitionText = definitionText;
            selectedDefinitionButton[0] = clickedButton;
        }else {
            //3)
            clickedButton.setBackgroundResource(R.drawable.mg_definition_selected_format);
            selectedDefinitionText = definitionText;
            defIsSelected[0] = true;
            selectedDefinitionButton[0] = clickedButton;
        }

        //4)
        if (termAndDefSelected()) {
            if (checkPair(selectedTermText, selectedDefinitionText)) {
                PointsTracker.addPoints();
                // Pass the actual button objects directly
                animateCorrectAnswerColour(selectedDefinitionButton[0], selectedTermButton[0]);
            }else {
                PointsTracker.removePoints();
                // Pass the actual button objects directly
                animateWrongAnswerColour(selectedDefinitionButton[0], selectedTermButton[0]);

                fullReset();
            }
        }
    }

    /*
    * A single, reusable method to handle clicks on any term button
    * 1) The user clicks the same button again to deselect it
    * 2) The user clicks a different term button while one was already selected
    * 3) No term button was selected, so select this button is selected
    * 4) Check for a match after any selection change
    * */
    private void onTermButtonClick(Button clickedButton, String termText) {
        if (clickedButton == selectedTermButton[0]) {
            //1)
            clickedButton.setBackgroundResource(R.drawable.mg_term_button_format);
            selectedTermText = "";
            termIsSelected[0] = false;
            selectedTermButton[0] = null;
        }else if (termIsSelected[0]) {
            //2)
            selectedTermButton[0].setBackgroundResource(R.drawable.mg_term_button_format);
            clickedButton.setBackgroundResource(R.drawable.mg_term_selected_format);
            selectedTermText = termText;
            selectedTermButton[0] = clickedButton;
        }else {
            //3)
            clickedButton.setBackgroundResource(R.drawable.mg_term_selected_format);
            selectedTermText = termText;
            termIsSelected[0] = true;
            selectedTermButton[0] = clickedButton;
        }

        //4)
        if (termAndDefSelected()) {
            if (checkPair(selectedTermText, selectedDefinitionText)) {
                PointsTracker.addPoints();
                animateCorrectAnswerColour(selectedDefinitionButton[0], selectedTermButton[0]);
            }else {
                PointsTracker.removePoints();
                animateWrongAnswerColour(selectedDefinitionButton[0], selectedTermButton[0]);

                fullReset();
            }
        }
    }

    /*
     * Starts a timer to show how long the user is playing the game
     * 1) Retrieves the current system time as the start time
     * 2) Creates a new runnable (thread) to consistently update the timer
     *   2.1) Subtracts the start time from the current system time
     *   2.2) Converts the elapsed time to seconds
     *   2.3) Converts the elapsed time to minutes
     *   2.4) Converts the elapsed time to hours
     *   2.5) Converts the elapsed time to a string
     *   2.6) Sets the text of the timer
     *   2.7) Repeats 2.1) to 2.6) every second
     * */
    private void startStopwatch(){
        //1)
        long startTime = SystemClock.uptimeMillis();
        //2)
        stopWatchRunnable = new Runnable() {
            @Override
            public void run() {
                //2.1)
                long elapsedTime = SystemClock.uptimeMillis() - startTime;
                //2.2) - 2.4)
                int seconds = (int) (elapsedTime/1000);
                int minutes = seconds/60;
                int hours = minutes/60;
                seconds = seconds%60;
                minutes = minutes%60;

                //2.5)
                String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                //2.6)
                stopwatchTimer.setText(time);
                //2.7)
                stopwatchHandler.postDelayed(this, 1000);//Update every second
            }
        };
        stopwatchHandler.post(stopWatchRunnable);
    }

    /*
     * Smoothly stops the timer to show how long the user took to play the game
     * */
    private void stopStopwatch(){
        if (stopwatchHandler != null && stopWatchRunnable != null) {
            stopwatchHandler.removeCallbacks(stopWatchRunnable);
        }
    }

    /*
     * Gets five unique, random terms and definitions objects from the TermsAndDefinitions class.
     * It uses a while loop to continue adding items until the list has 5 unique entries.
     * */
    private void getTermsAndDefinitions(){
        if (TermsAndDefinitions.TsAndDs.isEmpty()) {
            Toast.makeText(this, "Game data not loaded. Returning to menu.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TsAndDs.clear();

        TermsAndDefinitions termsAndDefinitionsTemp;
        while (TsAndDs.size() < 5){
            termsAndDefinitionsTemp = TermsAndDefinitions.getTermAndDefinition(TermsAndDefinitions.generateRandomIndex());
            if (!TsAndDs.contains(termsAndDefinitionsTemp)){
                TsAndDs.add(termsAndDefinitionsTemp);
            }
        }
    }

    /*
    * Takes the terms and conditions from the TermsAndConditions list and puts them into a HashMap,
    *   where the term is the key, and the definition is the value
    * */
    private void putToMap(){
        for (int i = 0; i < TsAndDs.size(); i++){
            tdPair.put(TsAndDs.get(i).getTerm(), TsAndDs.get(i).getDefinition());
        }
    }

    /*
    * Sets the text of the buttons to the definitions and terms
    * Collections.shuffle() randomises the order of the terms and definitions
    * */
    private void setButtonText(){
        Integer[] randomisedDefinitionArray = {0, 1, 2, 3, 4};
        Integer[] randomisedTermArray = {0, 1, 2, 3, 4};

        List<Integer> randomisedDefinitionList = Arrays.asList(randomisedDefinitionArray);
        List<Integer> randomisedTermList = Arrays.asList(randomisedTermArray);

        Collections.shuffle(randomisedDefinitionList);
        Collections.shuffle(randomisedTermList);

        def1.setText(TsAndDs.get(randomisedDefinitionList.get(0)).definition);
        def2.setText(TsAndDs.get(randomisedDefinitionList.get(1)).definition);
        def3.setText(TsAndDs.get(randomisedDefinitionList.get(2)).definition);
        def4.setText(TsAndDs.get(randomisedDefinitionList.get(3)).definition);
        def5.setText(TsAndDs.get(randomisedDefinitionList.get(4)).definition);

        term1.setText(TsAndDs.get(randomisedTermList.get(0)).term);
        term2.setText(TsAndDs.get(randomisedTermList.get(1)).term);
        term3.setText(TsAndDs.get(randomisedTermList.get(2)).term);
        term4.setText(TsAndDs.get(randomisedTermList.get(3)).term);
        term5.setText(TsAndDs.get(randomisedTermList.get(4)).term);
    }

    /*
    * Checks if a term and a definition is selected. This will be used to check if they match.
    * */
    private boolean termAndDefSelected(){
        return termIsSelected[0] && defIsSelected[0];
    }

    /*
    * Checks if the selected term and definition is correct
    * */
    private boolean checkPair(String term, String definition){
        if (tdPair.containsKey(term)){
            if (Objects.equals(tdPair.get(term), definition)){
                numCorrectAnswers++;
                totalCorrectAnswers++;
                return true;
            }else{
                totalWrongAnswers++;
                return false;
            }
        }else{
            return false;
        }
    }

    /*
    * 1) Sets number of correct answers to 0 and increments number of levels so far
    * 2) Creates a new level by:
    *   2.1) Retrieving new terms and definitions, and putting them to a map
    *   2.2) Setting the text of the buttons to the new terms and definitions
    *   2.3) Resetting the buttons so that they can be used again for the new level
    *   2.4) Resetting all variables used to hold the selected term and definition
    * 3) Checks how many levels have been played. If it is more than 5, than the game ends.
    * */
    private void newLevel(){
        if (numLevelsSofar == 5){//3)
//            pb.incrementProgressBy(25);
            endGame();
//            Toast.makeText(this, "End of game!", Toast.LENGTH_SHORT).show();
            return;
        }

        numCorrectAnswers = 0;//1)
        numLevelsSofar++;
        pb.incrementProgressBy(20);
//        switch (numLevelsSofar){
//            case 1:{
//                pb.incrementProgressBy(10);
//            }
//            case 2:{
//                pb.incrementProgressBy(20);
//            }
//            case 3:{
//                pb.incrementProgressBy(20);
//            }
//            case 4:{
//                pb.incrementProgressBy(25);
//            }
//            case 5:{
//                pb.incrementProgressBy(25);
//            }
//
//        }

//        pb.incrementProgressBy(50);

        tdPair.clear(); // Clear the old map entries
        getTermsAndDefinitions();//2.1)
        putToMap();
        setButtonText();//2.2)
        resetButtonsForNewGame();//2.3)
        fullReset();//2.4)

        Toast.makeText(this, "New Level! Current level is " + (numLevelsSofar), Toast.LENGTH_SHORT).show();
    }

    /*
    * Completely resets all variables used to store term and definition information to be used again later
    * */
    private void fullReset(){
        defIsSelected[0] = false;
        termIsSelected[0] = false;
        selectedDefinitionButton[0] = null;
        selectedTermButton[0] = null;
        selectedDefinitionText = "";
        selectedTermText = "";
    }

    /*
     * The selected buttons will show red before going back to their original colour
     * This is shown when the user's answer is wrong
     * */
    private void animateWrongAnswerColour(Button definitionButton, Button termButton) {
        definitionButton.setBackgroundResource(R.drawable.mg_definition_button_format);
        termButton.setBackgroundResource(R.drawable.mg_term_button_format);

        //Get the drawable objects from buttons
        GradientDrawable defDrawable = (GradientDrawable) definitionButton.getBackground();
        GradientDrawable termDrawable = (GradientDrawable) termButton.getBackground();

        //Define the colors we will animate between
        // Get the "unselected" colors from colors.xml file.
        int originalDefColor = getResources().getColor(R.color.definition_button_colour, getTheme());
        int originalTermColor = getResources().getColor(R.color.term_button_colour, getTheme());
        int redColor = Color.parseColor("#ee020b");

        //Animate from the current color to red
        ValueAnimator defToRed = ValueAnimator.ofObject(new ArgbEvaluator(), originalDefColor, redColor);
        defToRed.setDuration(500);
        defToRed.addUpdateListener(animation -> defDrawable.setColor((int) animation.getAnimatedValue()));

        ValueAnimator termToRed = ValueAnimator.ofObject(new ArgbEvaluator(), originalTermColor, redColor);
        termToRed.setDuration(500);
        termToRed.addUpdateListener(animation -> termDrawable.setColor((int) animation.getAnimatedValue()));

        //After a delay, animate from red back to the original colors
        int fadeBackDelay = 250;

        definitionButton.postDelayed(() -> {
            ValueAnimator reverseDefAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), redColor, originalDefColor);
            reverseDefAnimation.setDuration(500);
            reverseDefAnimation.addUpdateListener(animation -> defDrawable.setColor((int) animation.getAnimatedValue()));
            reverseDefAnimation.start();
        }, fadeBackDelay);

        termButton.postDelayed(() -> {
            ValueAnimator reverseTermAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), redColor, originalTermColor);
            reverseTermAnimation.setDuration(500);
            reverseTermAnimation.addUpdateListener(animation -> termDrawable.setColor((int) animation.getAnimatedValue()));
            reverseTermAnimation.start();
        }, fadeBackDelay);

        //Start the animations to red immediately
        defToRed.start();
        termToRed.start();

        definitionButton.setSelected(false);
        termButton.setSelected(false);
        fullReset();
    }


    /*
     * The selected buttons will show green before fading to grey, and being set to disabled
     * This is shown when the user's answer is correct
     * */
    private void animateCorrectAnswerColour(Button definitionButton, Button termButton) {
        //Get the drawable objects and define colors
        GradientDrawable defDrawable = (GradientDrawable) definitionButton.getBackground();
        GradientDrawable termDrawable = (GradientDrawable) termButton.getBackground();

        int greenColor = Color.parseColor("#71e73a");
        int greyColor = Color.parseColor("#808080");

        int selectedDefColor = getResources().getColor(R.color.definition_button_colour, getTheme());
        int selectedTermColor = getResources().getColor(R.color.term_button_colour, getTheme());

        //Animate from selected color to green
        ValueAnimator defToGreen = ValueAnimator.ofObject(new ArgbEvaluator(), selectedDefColor, greenColor);
        defToGreen.setDuration(500);
        defToGreen.addUpdateListener(animation -> defDrawable.setColor((int) animation.getAnimatedValue()));

        ValueAnimator termToGreen = ValueAnimator.ofObject(new ArgbEvaluator(), selectedTermColor, greenColor);
        termToGreen.setDuration(500);
        termToGreen.addUpdateListener(animation -> termDrawable.setColor((int) animation.getAnimatedValue()));

        //Turn buttons to grey after a delay
        int fadeToGreyDelay = 250;

        // Track which animations have completed
        final int[] animationsCompleteCount = {0};

        definitionButton.postDelayed(() -> {
            ValueAnimator defToGrey = ValueAnimator.ofObject(new ArgbEvaluator(), greenColor, greyColor);
            defToGrey.setDuration(500);
            defToGrey.addUpdateListener(animation -> defDrawable.setColor((int) animation.getAnimatedValue()));

            defToGrey.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    definitionButton.setEnabled(false);
                    animationsCompleteCount[0]++;

                    // Only check for level end when BOTH animations are complete
                    if (animationsCompleteCount[0] == 2) {
                        checkAndStartNewLevel();
                    }
                }
            });
            defToGrey.start();
        }, fadeToGreyDelay);

        termButton.postDelayed(() -> {
            ValueAnimator termToGrey = ValueAnimator.ofObject(new ArgbEvaluator(), greenColor, greyColor);
            termToGrey.setDuration(500);
            termToGrey.addUpdateListener(animation -> termDrawable.setColor((int) animation.getAnimatedValue()));

            termToGrey.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    termButton.setEnabled(false);
                    animationsCompleteCount[0]++;

                    // Only check for level end when BOTH animations are complete
                    if (animationsCompleteCount[0] == 2) {
                        checkAndStartNewLevel();
                    }
                }
            });
            termToGrey.start();
        }, fadeToGreyDelay);

        //Start the animations to green immediately
        defToGreen.start();
        termToGreen.start();

        definitionButton.setSelected(false);
        termButton.setSelected(false);
        fullReset();
    }

    private void checkAndStartNewLevel() {
        if (isEndOfLevel()) {
            // Add a small delay to ensure all animations are fully complete
            new Handler().postDelayed(() -> {
                newLevel();
            }, 100);
        }
    }

    /*
    * Resets buttons so that they can be used again
    * */
    private void resetButtonsForNewGame(){
        def1.setEnabled(false);
        def2.setEnabled(false);
        def3.setEnabled(false);
        def4.setEnabled(false);
        def5.setEnabled(false);

        term1.setEnabled(false);
        term2.setEnabled(false);
        term3.setEnabled(false);
        term4.setEnabled(false);
        term5.setEnabled(false);

        def1.setEnabled(true);
        def2.setEnabled(true);
        def3.setEnabled(true);
        def4.setEnabled(true);
        def5.setEnabled(true);

        term1.setEnabled(true);
        term2.setEnabled(true);
        term3.setEnabled(true);
        term4.setEnabled(true);
        term5.setEnabled(true);

        def1.setBackgroundResource(R.drawable.mg_definition_button_format);
        def2.setBackgroundResource(R.drawable.mg_definition_button_format);
        def3.setBackgroundResource(R.drawable.mg_definition_button_format);
        def4.setBackgroundResource(R.drawable.mg_definition_button_format);
        def5.setBackgroundResource(R.drawable.mg_definition_button_format);

        term1.setBackgroundResource(R.drawable.mg_term_button_format);
        term2.setBackgroundResource(R.drawable.mg_term_button_format);
        term3.setBackgroundResource(R.drawable.mg_term_button_format);
        term4.setBackgroundResource(R.drawable.mg_term_button_format);
        term5.setBackgroundResource(R.drawable.mg_term_button_format);
    }

    /*
    * If 5 levels have been played, then the game ends.
    * The points are tallied up and the user is taken to the post-game screen
    * */
//    private void endGame(){
//        int totalPoints = (totalCorrectAnswers * PointsTracker.POINTS_FOR_CORRECT_ANSWER) + (totalWrongAnswers * PointsTracker.POINTS_FOR_INCORRECT_ANSWER);
//        stopStopwatch();
//        PostGameScreen.setActivity("Matching Game");
//        PostGameScreen.setPointsFromCrosswordGame(totalPoints);
//        Intent intent = new Intent(MatchingGame.this, PostGameScreen.class);
//        startActivity(intent);
//    }

    private void endGame(){
        stopStopwatch();

        // Calculate total points
        int totalPoints = (totalCorrectAnswers * PointsTracker.POINTS_FOR_CORRECT_ANSWER) +
                (totalWrongAnswers * PointsTracker.POINTS_FOR_INCORRECT_ANSWER);

        // Set all data BEFORE starting the intent
        PostGameScreen.setActivity("Matching Game");

        // Get the final time from the stopwatch
        String finalTime = stopwatchTimer.getText().toString();
        PostGameScreen.setTimePlayed(finalTime);

        PointsTracker.setPointsFromGame(totalPoints);

        // NOW start the intent
        Intent intent = new Intent(MatchingGame.this, PostGameScreen.class);
        startActivity(intent);
        finish(); // Prevent going back to the game
    }

    /*
    * Checks if 5 correct answers have been given. If so, it will return true and mark the end of the game
    * */
    private boolean isEndOfLevel(){
        return numCorrectAnswers == 5;
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopStopwatch();
    }

}