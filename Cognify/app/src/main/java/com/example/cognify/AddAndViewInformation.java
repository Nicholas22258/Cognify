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

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import android.provider.OpenableColumns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddAndViewInformation extends AppCompatActivity {

    private TextView displayView;
    private Button getPDFButton;
    private Spinner listOfPDFs;

    private ArrayAdapter<String> spinnerAdapter;
    private List<String> pdfNames;
    private List<String> pdfUris;

    public static final String PREFS_NAME = "PdfReaderPrefs";
    private static final String PREF_PDF_NAMES = "pdfNames";
    private static final String PREF_PDF_URIS = "pdfUris";
    public static final String PREF_LAST_SELECTED_URI = "lastSelectedUri";
    public static final String PREF_LAST_SELECTED_NAME = "lastSelectedName";

    public static String courseName;
    public static boolean courseIsSelected = false;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    final int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                    getContentResolver().takePersistableUriPermission(uri, takeFlags);

                    String fileName = getFileNameFromUri(uri);
                    courseName = fileName;

                    if (!pdfUris.contains(uri.toString())) {
                        pdfNames.add(fileName);
                        pdfUris.add(uri.toString());
                        savePdfLists();
                        spinnerAdapter.notifyDataSetChanged();

                        // Select and load the newly added PDF
                        listOfPDFs.setSelection(pdfNames.size() - 1);
                        loadPdfFromUri(uri);
                        saveLastSelectedPdf(uri.toString(), fileName);

                        TermsAndDefinitions.loadDataToDB();
                    } else {
                        int index = pdfUris.indexOf(uri.toString());
                        listOfPDFs.setSelection(index);
                        loadPdfFromUri(uri);
                        saveLastSelectedPdf(uri.toString(), fileName);
                        Toast.makeText(this, "PDF already in list", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_and_view_information);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        displayView = findViewById(R.id.displayView);
        getPDFButton = findViewById(R.id.getPDFButton);
        listOfPDFs = findViewById(R.id.pdfSelector);

        pdfNames = new ArrayList<>();
        pdfUris = new ArrayList<>();

        loadPdfLists();

        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, pdfNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
        listOfPDFs.setAdapter(spinnerAdapter);

        listOfPDFs.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < pdfUris.size()) {
                    Uri uri = Uri.parse(pdfUris.get(position));
                    String fileName = pdfNames.get(position);
                    courseName = fileName;

                    loadPdfFromUri(uri);

                    // Save as the currently selected PDF
                    saveLastSelectedPdf(uri.toString(), fileName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_books);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull android.view.MenuItem item) {
                if (item.getItemId() == R.id.nav_home) {
                    // Already on home page, do nothing or refresh
                    startActivity(new Intent(AddAndViewInformation.this, HomePage.class));
//                    Toast.makeText(AddAndViewInformation.this, "Home Page", Toast.LENGTH_SHORT).show();

                    return true;
                } else if (item.getItemId() == R.id.nav_books) {
                    // Navigate to Books Activity
//                    startActivity(new Intent(AddAndViewInformation.this, AddAndViewInformation.class));
//                    Toast.makeText(HomePage.this, "Books (Not Implemented)", Toast.LENGTH_SHORT).show(); // Example toast
                    Toast.makeText(AddAndViewInformation.this, "Home Page", Toast.LENGTH_SHORT).show();
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    // Navigate to Profile Activity
                    startActivity(new Intent(AddAndViewInformation.this, ProfileActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_games) {
                    // Navigate to Games Activity
                    startActivity(new Intent(AddAndViewInformation.this, GamesScreen.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_settings) {
                    // Navigate to Settings Activity
                    startActivity(new Intent(AddAndViewInformation.this, HelpActivity.class));
                    return true;
                } else {
                    return false;
                }
            }
        });

        getPDFButton.setOnClickListener(v -> openFilePicker());

        // Load the last selected PDF if available
        loadLastSelectedPdf();
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");
        filePickerLauncher.launch(intent);
    }

    private void loadPdfFromUri(Uri uri) {
        try {
            String textFromPDF = readPdfFromUri(uri);
            loadToTsAndDsList(textFromPDF);
            readToTextView();
            Toast.makeText(this, "PDF loaded successfully", Toast.LENGTH_SHORT).show();
        } catch (IOException | SecurityException e) {
            Log.e("AddAndViewInformation", "Error reading PDF", e);
            displayView.setText(R.string.informationHelper);
//            Toast.makeText(this, "Failed to read PDF file.", Toast.LENGTH_SHORT).show();
//Failed to read PDF file. It might have been moved or deleted.
            int currentPosition = listOfPDFs.getSelectedItemPosition();
            if (currentPosition >= 0 && currentPosition < pdfUris.size()) {
                pdfNames.remove(currentPosition);
                pdfUris.remove(currentPosition);
                savePdfLists();
                spinnerAdapter.notifyDataSetChanged();
            }
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

    public static String getCourseName(){
        return courseName;
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = "Unknown PDF";

        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    String displayName = cursor.getString(nameIndex);
                    if (displayName != null && !displayName.isEmpty()) {
                        fileName = displayName;
                        Log.i("AddAndViewInformation", "Filename from DISPLAY_NAME: " + fileName);
                        return fileName;
                    }
                }
            }
        } catch (Exception e) {
            Log.e("AddAndViewInformation", "Error querying DISPLAY_NAME", e);
        }

        String path = uri.getPath();
        if (path != null) {
            Log.i("AddAndViewInformation", "Full URI path: " + path);

            int colonIndex = path.lastIndexOf(':');
            if (colonIndex != -1) {
                String afterColon = path.substring(colonIndex + 1);
                int lastSlash = afterColon.lastIndexOf('/');
                if (lastSlash != -1) {
                    fileName = afterColon.substring(lastSlash + 1);
                } else {
                    fileName = afterColon;
                }
                Log.i("AddAndViewInformation", "Filename from path parsing: " + fileName);
                return fileName;
            }

            int lastSlash = path.lastIndexOf('/');
            if (lastSlash != -1) {
                fileName = path.substring(lastSlash + 1);
            }
        }

        if (fileName.equals("Unknown PDF")) {
            String lastSegment = uri.getLastPathSegment();
            if (lastSegment != null) {
                int colonIndex = lastSegment.indexOf(':');
                if (colonIndex != -1) {
                    fileName = lastSegment.substring(colonIndex + 1);
                } else {
                    fileName = lastSegment;
                }
                Log.i("AddAndViewInformation", "Filename from lastPathSegment: " + fileName);
            }
        }

        Log.i("AddAndViewInformation", "Final filename: " + fileName);
        return fileName;
    }

    private void saveLastSelectedPdf(String uriString, String fileName) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_LAST_SELECTED_URI, uriString);
        editor.putString(PREF_LAST_SELECTED_NAME, fileName);
        editor.apply();

        Log.i("AddAndViewInformation", "Saved last selected: " + fileName);
    }

    private void loadLastSelectedPdf() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String lastUri = settings.getString(PREF_LAST_SELECTED_URI, null);

        if (lastUri != null && pdfUris.contains(lastUri)) {
            int index = pdfUris.indexOf(lastUri);
            listOfPDFs.setSelection(index);
        } else if (!pdfUris.isEmpty()) {
            listOfPDFs.setSelection(0);
        }
    }

    private void savePdfLists() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_PDF_NAMES, String.join("|||", pdfNames));
        editor.putString(PREF_PDF_URIS, String.join("|||", pdfUris));
        editor.apply();
    }

    private void loadPdfLists() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        String namesString = settings.getString(PREF_PDF_NAMES, "");
        String urisString = settings.getString(PREF_PDF_URIS, "");

        pdfNames.clear();
        pdfUris.clear();

        if (!namesString.isEmpty() && !urisString.isEmpty()) {
            String[] names = namesString.split("\\|\\|\\|");
            String[] uris = urisString.split("\\|\\|\\|");

            if (names.length == uris.length) {
                for (int i = 0; i < names.length; i++) {
                    pdfNames.add(names[i]);
                    pdfUris.add(uris[i]);
                }
            }
        }
    }

    private void loadToTsAndDsList(String textFromPDF){
        final String pairDelimiter = ";";
        final String termDefinitionDelimiter = ":";

        String term;
        String definition;
        String[] termDefPair;

        TermsAndDefinitions.TsAndDs.clear();

        String[] TsAndDs = textFromPDF.split(pairDelimiter);
        for (int i = 0; i < TsAndDs.length; i++) {
            termDefPair = TsAndDs[i].trim().split(termDefinitionDelimiter, 2);

            if (termDefPair.length > 1){
                term = termDefPair[0].trim();
                definition = termDefPair[1].trim();

                if (!term.isEmpty() && !definition.isEmpty()){
                    TermsAndDefinitions termDefinition = new TermsAndDefinitions(i, term, definition);
                    TermsAndDefinitions.TsAndDs.add(termDefinition);
                }
            }
        }
        courseIsSelected = true;
    }

    private void readToTextView(){
        StringBuilder sb = new StringBuilder();
        for (TermsAndDefinitions td : TermsAndDefinitions.TsAndDs){
            sb.append(td.term).append(": ").append(td.getDefinition()).append("\n\n");
        }
        displayView.setText(sb.toString());
    }
}