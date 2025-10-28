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
import android.content.Intent;
import android.graphics.Color;
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
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.flexbox.FlexboxLayout;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefinitionBuilder extends AppCompatActivity {
    /*
    * FlexboxLayouts will be used to display and hold words that make up the definition
    * Add implementation("com.google.android.flexbox:flexbox:3.0.0") to build.gradle.kts (Module :app)
    * */
    FlexboxLayout definitionLayout;
    FlexboxLayout wordChoicesLayout;
    TextView termView;
    TextView stopwatchTimer;

    Runnable stopWatchRunnable;
    Handler stopwatchHandler;

    TermsAndDefinitions currentTAndD;

    String[] definition;

    String term;

    int numLevels = 0;

    ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        getWindow().setStatusBarColor(Color.BLACK);
        setContentView(R.layout.activity_definition_builder);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        termView = findViewById(R.id.termToDisplay);
        definitionLayout = findViewById(R.id.wordDropZone);
        wordChoicesLayout = findViewById(R.id.flexboxLayoutWordChoices);
        Button btnCheck = findViewById(R.id.checkButton);
        pb = findViewById(R.id.progressBar);
        stopwatchTimer = findViewById(R.id.stopwatchTimer);

        stopwatchHandler = new Handler();
        pb.setMax(100);

        newLevel();
        startStopwatch();

        /*
        * When the user clicks this button, the system checks if the words are in the correct order
        * */
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkAnswer()){
                    Toast.makeText(DefinitionBuilder.this, "Correct!", Toast.LENGTH_SHORT).show();
                    PointsTracker.addPoints(2.5);
                    newLevel();
                }else{
                    Toast.makeText(DefinitionBuilder.this, "Incorrect!", Toast.LENGTH_SHORT).show();
                    PointsTracker.removePoints();
                }
            }
        });

        ImageButton cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DefinitionBuilder.this, GamesScreen.class);
                startActivity(intent);
            }
        });

        PointsTracker.resetPoints();

        // This callback will only be called when MyFragment is at least Started.
        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Show your alert dialog here
                new AlertDialog.Builder(DefinitionBuilder.this)
                        .setTitle("Leave Screen?")
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
    * Creates new views to display and hold the individual words
    * When a word is clicked, it is moved to the definition layout
    * */
    private void setWords(){
        for (String word : definition) {
            TextView wordView = createWordTextView(word);
            wordChoicesLayout.addView(wordView);
            wordView.setOnClickListener(v -> {
                // Set the initial click listener to move the word TO the definition
                moveWordToDefinition((TextView) v);
            });
        }
    }

    /*
    * Reusable helper method to create styled TextViews for words
    *
    * 1) Creates new TextView object
    * 2) Sets text of the new TextView object to a word in the definition
    * 3) Applies the custom background style "db_word_background" to each new TextView object
    * 4) Sets padding between each TextView to maintain gaps between each word and the edge of its parent, as well as the colour of the font
    * 5) defines the layout rules for how the new TextView object will behave inside its parent
    *   5.1) Sets the width and height of each TextView to match its text
    * 6) Sets the margins of each new TextView to maintain neat and consistent gaps between each word
    * 7) Layout rules are applied
    * 8) Returns fully configured TextView object
    * */
    private TextView createWordTextView(String text) {
        //1)
        TextView textView = new TextView(this);
        //2)
        textView.setText(text);
        //3)
        textView.setBackgroundResource(R.drawable.db_word_background);
        //4)
        textView.setPadding(16, 8, 16, 8);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        textView.setTextSize(20);

        //5)
        FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
            //5.1)
            FlexboxLayout.LayoutParams.WRAP_CONTENT,
            FlexboxLayout.LayoutParams.WRAP_CONTENT
        );
        //6)
        params.setMargins(8,8,8,8);
        //7)
        textView.setLayoutParams(params);
        //8)
        return textView;
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
    * When a word, that is in the definition layout is clicked, it is moved back into the word choices layout
    *
    * 1) Remove the word from the definition layout
    * 2) Add the word back to the word choices layout
    * 3) Set the click listener so it can be moved back to the definition if clicked again
    * */
    private void moveWordToChoices(TextView wordView){
        // 1)
        definitionLayout.removeView(wordView);
        // 2)
        wordChoicesLayout.addView(wordView);
        // 3)
        wordView.setOnClickListener(v -> moveWordToDefinition((TextView) v));
    }


    /*
     * When a word is clicked it is moved to the definition layout.
     *
     * 1) Remove the word from the choices layout
     * 2) Add the word to the definition layout
     * 3) Set the new click listener so it can be moved back to choices
     * */
    private void moveWordToDefinition(TextView wordView){
        // 1)
        wordChoicesLayout.removeView(wordView);
        // 2)
        definitionLayout.addView(wordView);
        // 3)
        wordView.setOnClickListener(v -> moveWordToChoices((TextView) v));
    }

    /*
    * Retrieves a random term and definition from the TermsAndDefinitions class
    * */
    private void getTAndD(){
        if (TermsAndDefinitions.TsAndDs.isEmpty()) {
            Toast.makeText(this, "Game data not loaded. Returning to menu.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        currentTAndD = TermsAndDefinitions.getTermAndDefinition(TermsAndDefinitions.generateRandomIndex());
    }

    /*
    * Retrieves the definition from currentTAndD and splits it into individual words.
    * Any punctuation is attached to the word that precedes it.
    * e.g., "This is a definition." -> ["This", "is", "a", "definition."]
    *
    * 1) Retrieves the definition from currentTAndD
    * 2) The definition is split using spaces and other punctuation as delimiters
    * 3) Any leftover spaces are removed
    * 4) Convert the array to a List and shuffles it
    * 5) Converts list to an array and returns shuffled array
    * */
    private String[] getDefinition(){
        //1)
        String definitionText = currentTAndD.getDefinition();
        //2)
        String[] individualWords = definitionText.split("\\s+");

        //3)
        for (int i = 0; i < individualWords.length; i++){
            individualWords[i] = individualWords[i].trim();
        }

        //4)
        List<String> shuffledWords = Arrays.asList(individualWords);
        Collections.shuffle(shuffledWords);
        //5)
        return shuffledWords.toArray(new String[0]);
    }

    /*
    * Retrieves the term from currentTAndD and stores it in the term global variable
    * */
    private void setTerm(){
        term = currentTAndD.getTerm();
    }


    /*
    * Checks the built definition.
    *
    * 1) Instantiates a variable to hold the user's built answer
    * 2) Instantiates a String Builder that will be used to build the user's answer
    * 3) Loops through all children of the definition layout and checks if they are TextView objects
    *   3.1) If they are, they are appended to the string builder with a space
    * 4) String Builder is converted to a string and trimmed to remove the last space
    * 5) Checks if the user's answer is equal to the definition
    *   5.1) Returns true if the user's answer is correct, false otherwise
    * */
    private boolean checkAnswer(){
        //1)
        String userAnswer = "";
        //2)
        StringBuilder sb = new StringBuilder();

        //3)
        for (int i = 0; i < definitionLayout.getChildCount(); i++){
            View childItem = definitionLayout.getChildAt(i);
            //3.1)
            if (childItem instanceof TextView){
                sb.append(((TextView) childItem).getText()).append(" ");
            }
        }
        //4)
        userAnswer = sb.toString().trim();
        String originalDefinition = currentTAndD.getDefinition().trim().replaceAll("\\s+", " ");
        //5)
        if (originalDefinition.equals(userAnswer)){
            //5.1)
            return true;
        }else{
            return false;
        }
    }

    /*
    * Creates a new level
    *
    * 1) Checks if the game should end
    * 2) If the game is not yet over:
    *   2.1) Increments the number of levels played
    *   2.2) Resets all relevant global variables
    *   2.3) Removes all items from the definition layout and the word layout
    *   2.4) Sets a new level:
    *       2.4.1) Retrieves a new term and definition
    *       2.4.2) Sets the term to the "term" global variable and applies this new term to the term TextView
    *       2.4.3) Retrieves the definition to be divided up
    *       2.4.4) Splits the definition into individual words
    * 3) If the game is over:
    *   3.1) Displays a message that the game is over
    * */
    private void newLevel(){
        //1)
        if (endOfGame()){
            //2)
            //2.1)
            numLevels++;
            pb.incrementProgressBy(20);
            //2.2)
            currentTAndD = null;
            definition = null;
            term = "";
            //2.3)
            definitionLayout.removeAllViews();
            wordChoicesLayout.removeAllViews();

            //2.4)
            //2.4.1)
            getTAndD();
            //2.4.2)
            setTerm();
            termView.setText(term);
            //2.4.3)
            definition = getDefinition();
            //2.4.4)
            setWords();
        }else{//3)
            //3.1)
//            Toast.makeText(DefinitionBuilder.this, "End of game!", Toast.LENGTH_SHORT).show();
        }

    }

    /*
    * Checks if the game will finish
    * If 5 levels have been played, then it will mark the end of the game
    * */
    private boolean endOfGame(){
        if (numLevels <= 5){
//            switch (numLevels){
//                case 1:{
//                    pb.incrementProgressBy(10);
//                }
//                case 2:{
//                    pb.incrementProgressBy(20);
//                }
//                case 3:{
//                    pb.incrementProgressBy(20);
//                }
//                case 4:{
//                    pb.incrementProgressBy(25);
//                }
//                case 5:{
//                    pb.incrementProgressBy(25);
//                }
//            }
            return true;//not end of game
        }else{

//            pb.incrementProgressBy(25);
            endGame();
            return false;//is end of game
        }
    }

    private void endGame(){
        stopStopwatch();

        // Set all data BEFORE starting the intent
        PostGameScreen.setActivity("Definition Builder");

        // Get the final time from the stopwatch
        String finalTime = stopwatchTimer.getText().toString();
        PostGameScreen.setTimePlayed(finalTime);

        // Points are already tracked in PointsTracker
//        PointsTracker.setPointsFromGame(PointsTracker.pointsForCurrentGame);

        // NOW start the intent
        Intent intent = new Intent(DefinitionBuilder.this, PostGameScreen.class);
        startActivity(intent);
        finish(); // Prevent going back to the game
    }


//    private void endGame(){
//        stopStopwatch();
//        PostGameScreen.setActivity("Definition Builder");
//        String finalTime = stopwatchTimer.getText().toString();
//        PostGameScreen.setTimePlayed(finalTime);
//        Intent intent = new Intent(DefinitionBuilder.this, PostGameScreen.class);
//        startActivity(intent);
//    }

    public void onBackPressedDispatcher() {
        // Prevent going back during the game
        new AlertDialog.Builder(this)
                .setTitle("Exit Game")
                .setMessage("Are you sure you want to exit? Your progress will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    stopStopwatch();
                    super.onBackPressed();
                })
                .setNegativeButton("No", null)
                .show();
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopStopwatch();
    }
}