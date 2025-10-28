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
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ResetConfirmationActivity extends AppCompatActivity {

    private TextView descriptionText, resendEmail;
    private ImageButton backButton;
    private MaterialButton loginButton;
    private String userEmail;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reset_confirmation);
        getWindow().setStatusBarColor(Color.BLACK);

        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        descriptionText = findViewById(R.id.descriptionText);
        resendEmail = findViewById(R.id.resendEmail);
        backButton = findViewById(R.id.backBtn);
        loginButton = findViewById(R.id.loginBtn);

        // Get email from previous screen
        userEmail = getIntent().getStringExtra("email");
        descriptionText.setText("We sent a reset link to " + userEmail + "\nPlease check your inbox.");

        // Handle Back Arrow
        backButton.setOnClickListener(v -> onBackPressed());

        // Handle "Back to Login" button
        loginButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResetConfirmationActivity.this, GamesScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish(); // Close this activity so user can’t go back here
        });

        // Resend email logic
        resendEmail.setOnClickListener(v -> {
            mAuth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(this, "Reset email resent!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Error: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }
}
