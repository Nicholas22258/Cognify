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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*This game presents the user with a dynamically created crossword game to solve.*/

/*In the code:
* - "terms" and "words" refer to the same thing: terms are the words that the user needs to enter into the crossword
* - "definitions" and "clues" refer to the same thing: definitions function as the clues that tell the user what the word is
*   - These may be used interchangeably
*
* In this game, there are two classes of which to take note: PlacedWord and Word
* - PlacedWord is used as a temporary object when the crossword grid is generated.
*   - It keeps track of where the word will be placed on the grid
*   - It only stores information about the word itself, i.e. the term, it's starting point (row & column), and it's direction (ACROSS/DOWN)
*
* - Word is used to finalise the crossword game
*   - It is the data structure that will be read from and displayed to the user
*   - It stores all relevant information about the word:
*       - It's ID, which is it's clue number (e.g. 1A, 3D)
*       - It's term
*       - It's associate definition, which will serve as the term's clue
*       - It's starting coordinates (row, column)
*       - It's direction (ACROSS/DOWN)
* */

public class Crossword extends AppCompatActivity {

    //gridLayout will be used to display the crossword grid
    private GridLayout gridLayout;
    //recyclerViewAcrossClues will display the definitions of the words that go across in the grid
    private RecyclerView recyclerViewAcrossClues;
    //recyclerViewDownClues will display the definitions of the words that go down in the grid
    private RecyclerView recyclerViewDownClues;

    //There will be a maximum of 7 terms and definitions that will be used per crossword level
    private final int NUM_TERMS_AND_DEFINITIONS = 7;
    //TsAndDs will be used to store the terms and definitions for the crossword level
    private List<TermsAndDefinitions> TsAndDs = new ArrayList<>();

    private List<String> terms = new ArrayList<>();
    private List<String> definitions = new ArrayList<>();

    //cells will be used to represent the individual cells of the crossword grid.
    //Each cell will be an EditText in which the user can enter a letter
    private final EditText[][] cells = new EditText[10][10];

    //placedWords will be used to store the words that have been placed on the grid
    private final List<PlacedWord> placedWords = new ArrayList<>();
    //the GRID_SIZE is 10 by 10, to make 100 cells
    private final int GRID_SIZE = 10;
    //gridLogic will be used to store the letters that have been placed on the grid
    private final char[][] gridLogic = new char[GRID_SIZE][GRID_SIZE];
    //clueNumbers will be used to store where the start of each word is on the grid
    private final int[][] clueNumbers = new int[GRID_SIZE][GRID_SIZE];

    //acrossWords will store the words that go across the grid
    private final List<Word> acrossWords = new ArrayList<>();
    //downWords will store the words that go down the grid
    private final List<Word> downWords = new ArrayList<>();

    TextView stopwatchTimer;

    Runnable stopWatchRunnable;
    Handler stopwatchHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_crossword);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        gridLayout = findViewById(R.id.gridLayoutCrossword);
        recyclerViewAcrossClues = findViewById(R.id.recyclerViewAcrossClues);
        recyclerViewDownClues = findViewById(R.id.recyclerViewDownClues);
        Button buttonCheck = findViewById(R.id.checkAnswerButton);//Will be used to check the answers on the grid
        stopwatchTimer = findViewById(R.id.stopwatchTimer);
        stopwatchHandler = new Handler();

        buttonCheck.setOnClickListener(v -> checkAnswers());

        setupCrosswordGrid();
        newLevel();

        ImageButton cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                stopStopwatch();
                Intent intent = new Intent(Crossword.this, GamesScreen.class);
                startActivity(intent);
            }
        });

        PointsTracker.resetPoints();

        OnBackPressedCallback callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Show your alert dialog here
                new AlertDialog.Builder(Crossword.this)
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
    * Sets up the crossword grid
    * 1) Creates a new ContextThemWrapper that will be used to style the EditText cells, that will make up the crossword grid
    * 2) Creates 100 EditTexts for each grid cell:
    *   2.1) Creates a new EditText to be placed into the grid
    *   2.2) Creates a new LayoutParams object to define how the abovementioned EditText will behave in the gridLayout
    *   2.3) The new EditText is placed into the next available row and column, and is set to fill the whole available space
    *   2.4) The new EditText's width and height are set to 0 so that the system does not get confused
    *   2.5) The new EditText is added to the gridLayout
    *   2.6) The EditText is set to open the normal keyboard for the user to enter a letter in the EditText; The keyboard will show capital letters by default
    *   2.7) The EditText is set to capitalise all entered characters, and limits the number of letters entered to 1
    *   2.8) The input letters will be centered horizontally and vertically
    *   2.9) The EditText is added to the gridLayout
    *   2.10) Calculates the position in cells[][] to place the new EditText
    *   2.11) Places the new EditText into the correct position in cells[][]
    * */
    private void setupCrosswordGrid() {                             //Found in themes.xml
        //1)
        Context context = new ContextThemeWrapper(this, R.style.CrosswordCell);

        //2)
        for (int i = 0; i < GRID_SIZE * GRID_SIZE; i++) {
            //2.1)
            EditText cell = new EditText(context);
            //2.2)
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            //2.3)
            params.rowSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            //2.4)
            params.width = 0;
            params.height = 0;
            //2.5)
            cell.setLayoutParams(params);

            //2.6)
            cell.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
            //2.7)
            cell.setFilters(new InputFilter[]{new InputFilter.LengthFilter(1), new InputFilter.AllCaps()});
            //2.8)
            cell.setGravity(Gravity.CENTER);

            //2.9)
            gridLayout.addView(cell);

            //2.10)
            int row = i / GRID_SIZE;
            int col = i % GRID_SIZE;
            //2.11)
            cells[row][col] = cell;
        }
    }

    /*
     * Sets up the clues (definitions) for the crossword grid
     *
     * 1) Creates layout managers for the recyclerViewAcrossClues and recyclerViewDownClues, each of which will hold their respective clues
     * 2) Two lists, acrossClues and downClues, are created as a logical storage for their respective clues
     * 3) The clues are placed into their respective lists
     * 4) The clues are then displayed in the recyclerViewAcrossClues and recyclerViewDownClues using an adapter
     * */
    private void setHints() {
        //1)
        recyclerViewAcrossClues.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDownClues.setLayoutManager(new LinearLayoutManager(this));
        //2)
        List<String> acrossClues = new ArrayList<>();
        List<String> downClues = new ArrayList<>();
        //3)
        for (Word word : acrossWords) {
            acrossClues.add(word.id + ". " + word.definition);
        }

        for (Word word : downWords) {
            downClues.add(word.id + ". " + word.definition);
        }
        //4)
        recyclerViewAcrossClues.setAdapter(new ClueAdapter(acrossClues));
        recyclerViewDownClues.setAdapter(new ClueAdapter(downClues));
    }

    /*
     *  Determines where the words will be placed on the grid, and whether they will be placed across or down
     *
     * 1) Clears all Lists that store the selected words (terms)
     * 2) gridLogic and clue numbers are reset to be filled with null and 0 respectively
     * 3) Creates a list to sort the Terms from longest to shortest. This will make placing the words easier
     * 4) Retrieves the first word in the sorted list and places it in the grid so that it starts in the second row and one of the first few columns. The first word is then removed so that the rest of the words can try to be placed.
     * 5) The rest of the words are "placed" using tryPlaceWithIntersection(), which determines if the word can be placed into the grid in a way that it intersects one of the other words
     * 6) A clue number is assigned to the newly placed word
     * 7) The grid is cleared so that the user can't see the answers, and the user can enter the answers
     * */
    private void determineDownAndAcross() {
        //1)
        placedWords.clear();
        acrossWords.clear();
        downWords.clear();

        //2)
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                gridLogic[i][j] = '\0';
                clueNumbers[i][j] = 0;
            }
        }

        //3)
        List<String> sortedTerms = new ArrayList<>(terms);
        sortedTerms.sort((s1, s2) -> s2.length() - s1.length());

        //4)
        String firstWord = sortedTerms.get(0);
        int startRow = 1;
        int startCol = (GRID_SIZE - firstWord.length()) / 2;
        placeWord(firstWord, startRow, startCol, true);
        sortedTerms.remove(0);

        boolean canPlaceWithIntersection;
        //5)
        for (String wordToPlace : sortedTerms) {
            canPlaceWithIntersection = tryPlaceWithIntersection(wordToPlace);
        }

        //6)
        assignClueNumbers();

        //7)
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                cells[r][c].setText("");
                cells[r][c].setEnabled(gridLogic[r][c] != '\0');
            }
        }
    }

    /*
     * This method creates and assigns clue numbers (e.g. 1A, 3D) like seen in traditional crossword games
     *
     * 1) The first clue starts at 1
     * 2) A map will be used to link the term to it's respective clue number
     * 3) Loops through the whole grid to find where letters start:
     *  3.1) Checks if a not-null cell is the start of an across word
     *  3.2) Checks if a not-null cell is the start of a down word
     *  3.3) If the cell is that start of a word:
     *      3.3.1) The cell is assigned a clue number
     *      3.3.2) The word is mapped to it's clue number
     *      3.3.3) The clue number is incremented
     * 3.4) Creates the word objects that will be used to finalise the crossword grid
     *      3.4.1) The word's clue number is retrieved from the map
     *      3.4.2) If the word's clue number is not null:
     *          3.4.2.1) The original index of the word is retrieved and used to retrieve it's associated definition
     *          3.4.2.2) The word's clueID is created from the word's number and it's direction ('A' for across and 'D' for Down)
     *          3.4.2.3) The word is created and added to the appropriate list (across or down)
     * 3.5) The word lists are sorted by their clue numbers so that they are displayed in the correct order
     * */
    private void assignClueNumbers() {
        //1)
        int clueNumber = 1;
        //2)
        Map<String, Integer> wordToNumber = new HashMap<>();

        //3)
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                boolean isAcrossStart = false;
                boolean isDownStart = false;

                //3.1)
                if (gridLogic[row][col] != '\0' && (col == 0 || gridLogic[row][col - 1] == '\0')) {
                    if (col + 1 < GRID_SIZE && gridLogic[row][col + 1] != '\0') {
                        isAcrossStart = true;
                    }
                }

                //3.2)
                if (gridLogic[row][col] != '\0' && (row == 0 || gridLogic[row - 1][col] == '\0')) {
                    if (row + 1 < GRID_SIZE && gridLogic[row + 1][col] != '\0') {
                        isDownStart = true;
                    }
                }

                //3.3)
                if (isAcrossStart || isDownStart) {
                    //3.3.1)
                    clueNumbers[row][col] = clueNumber;

                    //3.3.2)
                    for (PlacedWord placedWord : placedWords) {
                        if (placedWord.row == row && placedWord.col == col) {
                            wordToNumber.put(placedWord.term + "_" + placedWord.isAcross, clueNumber);
                        }
                    }
                    //3.3.3)
                    clueNumber++;
                }
            }
        }

        //3.4)
        for (PlacedWord placedWord : placedWords) {
            //3.4.1)
            String key = placedWord.term + "_" + placedWord.isAcross;
            Integer number = wordToNumber.get(key);
            //3.4.2.1)
            if (number != null) {
                int originalIndex = terms.indexOf(placedWord.term);
                if (originalIndex != -1) {
                    String definition = definitions.get(originalIndex);
                    //3.4.2.2)
                    Direction dir = placedWord.isAcross ? Direction.ACROSS : Direction.DOWN;
                    String clueID = number + (placedWord.isAcross ? "A" : "D");
                    //3.4.2.3)
                    Word word = new Word(clueID, placedWord.term, definition, placedWord.row, placedWord.col, dir);

                    if (placedWord.isAcross) {
                        acrossWords.add(word);
                    }else{
                        downWords.add(word);
                    }
                }
            }
        }

        //3.5)
        acrossWords.sort((w1, w2) -> {
            int num1 = Integer.parseInt(w1.id.substring(0, w1.id.length() - 1));
            int num2 = Integer.parseInt(w2.id.substring(0, w2.id.length() - 1));
            return num1 - num2;
        });

        downWords.sort((w1, w2) -> {
            int num1 = Integer.parseInt(w1.id.substring(0, w1.id.length() - 1));
            int num2 = Integer.parseInt(w2.id.substring(0, w2.id.length() - 1));
            return num1 - num2;
        });
    }

    /*
     * This method determines if a given word can be placed into the grid in such a way that it intersects another word
     *
     * 1) Loops through all letters in the given word
     * 2) Loops through the existing words that are already on the board
     *  2.1) Loops through all letters in the first/next detected word
     *      2.1.1) If the letter of the given word matches the letter of the detected word:
     *          2.1.1.1) The word will be placed in the opposite direction of the detected word
     *          2.1.1.2) The theoretically correct indexes for the new word, to be placed in the grid, are calculated
     *          2.1.1.3) If one of the cell coordinates are not possible (i.e. less than 0), then it will look for the next possible placement
     *          2.1.1.4) Checks if the word placement is possible (using method canPlaceWord()) so that it does not create fake words through putting words parallel to each other
     *              2.1.1.4.1) If the word placement is possible, then it will place the word and return true;
     * 3) False is returned if the word cannot be placed in such a way to make it intersect another word
     * */
    private boolean tryPlaceWithIntersection(String word) {
        //1)
        for (int i = 0; i < word.length(); i++) {
            char letterInGivenWord = word.charAt(i);

            //2)
            for (PlacedWord wordOnGrid : placedWords) {
                //2.1)
                for (int j = 0; j < wordOnGrid.term.length(); j++) {
                    char letterOnGrid = wordOnGrid.term.charAt(j);

                    //2.1.1)
                    if (letterInGivenWord == letterOnGrid) {
                        int newRow, newCol;
                        //2.1.1.1)
                        boolean newIsAcross = !wordOnGrid.isAcross;

                        //2.1.1.2)
                        if (wordOnGrid.isAcross) {
                            newRow = wordOnGrid.row - i;
                            newCol = wordOnGrid.col + j;
                        }else{
                            newRow = wordOnGrid.row + j;
                            newCol = wordOnGrid.col - i;
                        }

                        //2.1.1.3)
                        if (newRow < 0 || newCol < 0){
                            continue;
                        }

                        //2.1.1.4)
                        if (canPlaceWord(word, newRow, newCol, newIsAcross)) {
                            //2.1.1.4.1)
                            placeWord(word, newRow, newCol, newIsAcross);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
     * This method determines if placing a word will violate the rules of the crossword game, i.e. it will not create fake words
     * This is different from tryPlaceWithIntersection() as that method determines if a word can be placed to intersect another word
     *
     * 1) Checks if the coordinates are valid
     * 2) Checks if the word will continue off the grid, which will violate the rules of the crossword game
     * 3) Checks if there is a space before the starting letter of the word, if there is not, it will result in the creation of a fake word, e.g. IGAME instead of GAME
     * 4) Checks if there is a space after the word, if there is, it will result in the creation of a fake word, e.g. GAMEB instead of GAME
     * 5) Checks each cell by retrieving the coordinates of the next found letter
     *  5.1) Checks if the letter of the given word matches the letter in the cell
     *  5.2) Checks if placing a letter will create a fake word
     *      5.2.1) If the word is going across, it will check above and below the letter
     *      5.2.2) If the word is going down, it will check to the left and right of the letter
     * 6) If all checks pass, the word can be placed
     * */
    private boolean canPlaceWord(String word, int row, int col, boolean isAcross) {
        //1)
        if (row < 0 || col < 0) {
            return false;
        }

        //2)
        if ((isAcross && col + word.length() > GRID_SIZE) ||  (!isAcross && row + word.length() > GRID_SIZE)) {
            return false;
        }

        //3)
        if (isAcross && col > 0 && gridLogic[row][col - 1] != '\0') {
            return false;
        }
        if (!isAcross && row > 0 && gridLogic[row - 1][col] != '\0') {
            return false;
        }

        //4)
        if (isAcross && col + word.length() < GRID_SIZE && gridLogic[row][col + word.length()] != '\0') {
            return false;
        }
        if (!isAcross && row + word.length() < GRID_SIZE && gridLogic[row + word.length()][col] != '\0') {
            return false;
        }

        //5)
        for (int i = 0; i < word.length(); i++) {
            int rowCoordinate = isAcross ? row : row + i;
            int colCoordinate = isAcross ? col + i : col;

            if (rowCoordinate >= GRID_SIZE || colCoordinate >= GRID_SIZE) {
                return false;
            }

            //5.1)
            char letterOnGrid = gridLogic[rowCoordinate][colCoordinate];
            char newLetter = word.charAt(i);

            if (letterOnGrid != '\0' && letterOnGrid != newLetter) {
                return false;
            }

            //5.2)
            if (letterOnGrid == '\0') {
                if (isAcross) {
                    //5.2.1)
                    if ((rowCoordinate > 0 && gridLogic[rowCoordinate - 1][colCoordinate] != '\0') ||
                            (rowCoordinate < GRID_SIZE - 1 && gridLogic[rowCoordinate + 1][colCoordinate] != '\0')) {
                        return false;
                    }
                }else{
                    //5.2.2)
                    if ((colCoordinate > 0 && gridLogic[rowCoordinate][colCoordinate - 1] != '\0') ||
                            (colCoordinate < GRID_SIZE - 1 && gridLogic[rowCoordinate][colCoordinate + 1] != '\0')) {
                        return false;
                    }
                }
            }
        }
        //6)
        return true;
    }

    /*
     * This method puts a word into the grid
     *
     * 1) Loops through the letters of the word
     * 2) Places the letter into the grid by:
     *  2.1) Checking the direction of the word
     *  2.2) Adding the letter to the next available cell
     *      2.2.1) In the next column to the right if it is across
     *      2.2.2) In the next row to the bottom if it is down
     * 3) Adds the word to the list of placed words
     * */
    private void placeWord(String word, int row, int col, boolean isAcross) {
        //1)
        for (int i = 0; i < word.length(); i++) {
            //2.1)
            if (isAcross) {
                //2.2.1)
                gridLogic[row][col + i] = word.charAt(i);
            }else{
                //2.2.2)
                gridLogic[row + i][col] = word.charAt(i);
            }
        }
        //3)
        placedWords.add(new PlacedWord(word, row, col, isAcross));
    }


    /*
    * This method looks for empty cells and disables and blacks them out
    *
    * 1) Loops through the grid
    *   1.1) Checks if the cell is empty
    *       1.1.1) If it is empty, it is set to black and disabled
    *       1.1.2) Otherwise the background is set to White and the clue number is added, if there is one
    * */
    public void blackoutEmptySquares() {
        //1)
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                //1.1)
                if (gridLogic[row][col] == '\0') {
                    //1.1.1)
                    cells[row][col].setBackgroundColor(Color.BLACK);
                    cells[row][col].setEnabled(false);
                }else{
                    //1.1.2)
                    cells[row][col].setBackgroundColor(Color.WHITE);
                    // Add clue number if present
                    if (clueNumbers[row][col] > 0) {
                        cells[row][col].setHint(String.valueOf(clueNumbers[row][col]));
                    }
                }
            }
        }
    }

    /*
     * This method checks if the user has entered the correct answers
     *
     * 1) Loops through the grid
     * 2) Checks if the cell is not empty
     *  2.1) Retrieves the user's input in that cell
     *  2.2) Retrieves the correct answer from gridLogic
     *  2.3) Checks if the user's input matches the correct answer
     *      2.3.1) If it does, the cell is set to green and increments the number of correct answers
     *      2.3.2) If it doesn't, then the cell text is set to red and allCorrect is set to false
     * 3) The total points are calculated and the user is taken to the post game screen
     * */
//    private void checkAnswers() {
//        boolean allCorrect = true;
//        int numCorrectAnswers = 0;
//        int numWrongAnswers = 0;
//        int totalCells = 0;
//
//        //1)
//        for (int row = 0; row < GRID_SIZE; row++) {
//            for (int col = 0; col < GRID_SIZE; col++) {
//                //2)
//                if (gridLogic[row][col] != '\0') {
//                    totalCells++;
//                    //2.1)
//                    String userInput = cells[row][col].getText().toString().toUpperCase();
//                    //2.2)
//                    String correctAnswer = String.valueOf(gridLogic[row][col]).toUpperCase();
//
//                    //2.3)
//                    if (userInput.equals(correctAnswer)) {
//                        //2.3.1)
//                        cells[row][col].setTextColor(Color.GREEN);
//                        numCorrectAnswers++;
//                    }else{
//                        //2.3.2)
//                        cells[row][col].setTextColor(Color.RED);
//                        numWrongAnswers++;
//                        allCorrect = false;
//                    }
//                }
//            }
//        }
//
//        int pointsForThisGame = (numCorrectAnswers * PointsTracker.POINTS_FOR_CORRECT_ANSWER) + (numWrongAnswers * PointsTracker.POINTS_FOR_INCORRECT_ANSWER);
//        PostGameScreen.setPointsFromCrosswordGame(pointsForThisGame);
//        endGame();

//        if (allCorrect) {
//            int pointsForThisGame = numCorrectAnswers * PointsTracker.POINTS_FOR_CORRECT_ANSWER;
//            PostGameScreen.setPointsFromCrosswordGame(pointsForThisGame);
//            Toast.makeText(this, "Congratulations! All answers are correct! Your points: " + pointsForThisGame, Toast.LENGTH_LONG).show();
//            endGame();
//        }else{
//            Toast.makeText(this, "Score: " + numCorrectAnswers + "/" + totalCells + " correct", Toast.LENGTH_LONG).show();
//            endGame();
//        }
//    }

    private void checkAnswers() {
        boolean allCorrect = true;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (gridLogic[row][col] != '\0') {
                    String userInput = cells[row][col].getText().toString().toUpperCase();
                    String correctAnswer = String.valueOf(gridLogic[row][col]).toUpperCase();

                    if (userInput.equals(correctAnswer)) {
                        cells[row][col].setTextColor(Color.GREEN);
                    } else {
                        cells[row][col].setTextColor(Color.RED);
                        allCorrect = false;
                    }
                }
            }
        }

        // Just end the game - points will be calculated in endGame()
        endGame();
    }

    private void endGame(){
        stopStopwatch();

        // Set all data BEFORE starting the intent
        PostGameScreen.setActivity("Crossword");

        // Get the final time from the stopwatch
        String finalTime = stopwatchTimer.getText().toString();
        PostGameScreen.setTimePlayed(finalTime);

        // Calculate and set points
        int pointsForThisGame = calculatePoints();
        PointsTracker.setPointsFromGame(pointsForThisGame);

        // NOW start the intent
        Intent intent = new Intent(Crossword.this, PostGameScreen.class);
        startActivity(intent);
        finish(); // Prevent going back to the game
    }

    // ADD this helper method to Crossword.java:
    private int calculatePoints(){
        int numCorrectAnswers = 0;
        int numWrongAnswers = 0;

        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                if (gridLogic[row][col] != '\0') {
                    String userInput = cells[row][col].getText().toString().toUpperCase();
                    String correctAnswer = String.valueOf(gridLogic[row][col]).toUpperCase();

                    if (userInput.equals(correctAnswer)) {
                        numCorrectAnswers++;
                    } else {
                        numWrongAnswers++;
                    }
                }
            }
        }

        Log.i("Crossword Points", Integer.toString((numCorrectAnswers * PointsTracker.POINTS_FOR_CORRECT_ANSWER) +
                (numWrongAnswers * PointsTracker.POINTS_FOR_INCORRECT_ANSWER)));
        return (numCorrectAnswers * PointsTracker.POINTS_FOR_CORRECT_ANSWER) +
                (numWrongAnswers * PointsTracker.POINTS_FOR_INCORRECT_ANSWER);
    }

    /*
     * Retrieves 7 TermsAndDefinitions objects from the TermsAndDefinitions class
     *
     * 1) Checks if TsAndDs can accept items
     * 2) Clears the list of TsAndDs to minimise the chances of error
     * 3) Retrieves 7 TermsAndDefinitions objects
     *  3.1) Checks if the TermsAndDefinitions object already exists and checks to see if the length of the term is less than 10 (as the grid size is 10x10 cells)
     *      3.1.1) If so, then it is added to the list of TsAndDs
     * */
    private void getTsAndDs() {
        if (TermsAndDefinitions.TsAndDs.isEmpty()) {
            Toast.makeText(this, "Game data not loaded. Returning to menu.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //1)
        if (TsAndDs == null) {
            TsAndDs = new ArrayList<>();
        }
        TsAndDs.clear();
        TermsAndDefinitions termsAndDefinitionsTemp;
        while (TsAndDs.size() < NUM_TERMS_AND_DEFINITIONS) {
            termsAndDefinitionsTemp = TermsAndDefinitions.getTermAndDefinition(TermsAndDefinitions.generateRandomIndex());
            if (!TsAndDs.contains(termsAndDefinitionsTemp) && termsAndDefinitionsTemp.getTerm().length() < 10) {
                TsAndDs.add(termsAndDefinitionsTemp);
            }
        }
    }

    /*
     * Separates the terms and definitions into separate lists, and sets the terms to uppercase
     *
     * 1) Checks if terms and definitions can accept items (i.e. they are not null)
     * 2) Clears the terms and definitions lists
     * 3) Loops through the TsAndDs list
     *  3.1) If the TsAndDs object is not null
     *      3.1.1) Then the term is set to uppercase and added to the terms list
     *      3.1.2) The definition is added to the definitions list
     * */
    private void setTsAndDs() {
        //1)
        if (terms == null) {
            terms = new ArrayList<>();
        }
        if (definitions == null) {
            definitions = new ArrayList<>();
        }
        //2)
        terms.clear();
        definitions.clear();
        //3)
        for (TermsAndDefinitions td : TsAndDs) {
            //3.1)
            if (td != null) {
                //3.1.1)
                terms.add(td.getTerm().toUpperCase());
                //3.1.2)
                definitions.add(td.getDefinition());
            }
        }
    }

    /*
    * Creates a new level
    *
    * 1) Instantiates all lists, if they have not been instantiated already
    * 2) Clears all lists to avoid conflicts
    * 3) Retrieves the TsAndDs to be used
    * 4) Sets the TsAndDs list
    * 5) Determines if a word can be added to the grid, and later adds them
    * 6) Sets the hints for the words on the grid
    * 7) Blacks out the empty squares
    * 8) Starts the stop watch
    * */
    private void newLevel() {
        //1)
        if (TsAndDs == null) {
            TsAndDs = new ArrayList<>();
        }
        if (terms == null) {
            terms = new ArrayList<>();
        }
        if (definitions == null) {
            definitions = new ArrayList<>();
        }

        //2)
        TsAndDs.clear();
        terms.clear();
        definitions.clear();
        //3)
        getTsAndDs();
        //4)
        setTsAndDs();
        //5)
        determineDownAndAcross();
        //6)
        setHints();
        //7)
        blackoutEmptySquares();
        //8)
        startStopwatch();
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

    @Override
    protected void onDestroy(){
        super.onDestroy();
        stopStopwatch();
    }
}

/*
 * A lightweight class to store the data of a placed word
 * This class will be used to build the grid
 * */
class PlacedWord {
    String term;
    int row;
    int col;
    boolean isAcross;

    PlacedWord(String term, int row, int col, boolean isAcross) {
        this.term = term;
        this.row = row;
        this.col = col;
        this.isAcross = isAcross;
    }
}