package com.example.cognify;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.IOException;
import java.io.InputStream;

public class PdfLoader {

    // 1. Create an interface to act as a listener (callback)
    public interface PdfLoaderListener {
        void onPdfLoaded(String courseName, int termCount);
        void onPdfLoadFailed(String errorMessage);
    }

    // 2. The main public method that activities will call
    public void loadLastSelectedPdf(Context context, PdfLoaderListener listener) {
        SharedPreferences settings = context.getSharedPreferences(AddAndViewInformation.PREFS_NAME, Context.MODE_PRIVATE);
        String uriString = settings.getString(AddAndViewInformation.PREF_LAST_SELECTED_URI, null);
        String courseName = settings.getString(AddAndViewInformation.PREF_LAST_SELECTED_NAME, null);

        if (uriString != null && courseName != null) {
            Uri uri = Uri.parse(uriString);

            // Use a background thread to prevent freezing the UI
            new Thread(() -> {
                try {
                    // Use the context to get an InputStream
                    InputStream inputStream = context.getContentResolver().openInputStream(uri);
                    if (inputStream == null) {
                        throw new IOException("Unable to open URI: " + uri);
                    }

                    String textFromPDF = readPdfFromStream(inputStream);
                    loadToTsAndDsList(textFromPDF);

                    // Use a Handler to post results back to the main UI thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        AddAndViewInformation.courseIsSelected = true;
                        AddAndViewInformation.courseName = courseName;
                        // Use the listener to notify the calling activity of success
                        listener.onPdfLoaded(courseName, TermsAndDefinitions.TsAndDs.size());
                    });

                } catch (IOException | SecurityException e) {
                    Log.e("PdfLoader", "Error loading PDF", e);
                    // Post failure result back to the main UI thread
                    new Handler(Looper.getMainLooper()).post(() -> {
                        listener.onPdfLoadFailed("Failed to load PDF. Please select it again.");
                    });
                }
            }).start();
        } else {
            // No PDF was previously selected
            listener.onPdfLoadFailed("Please select a course to start.");

            Intent intent = new Intent(context, AddAndViewInformation.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }
    }

    // 3. Helper methods are now private within this class
    private String readPdfFromStream(InputStream inputStream) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        PdfReader reader = new PdfReader(inputStream);
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

        TermsAndDefinitions.TsAndDs.clear(); // Clear previous data

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
}

