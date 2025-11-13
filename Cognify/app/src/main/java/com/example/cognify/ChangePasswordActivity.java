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

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException;
import com.google.firebase.auth.FirebaseUser;

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currentPasswordInput;
    private EditText newPasswordInput;
    private EditText confirmPasswordInput;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        getWindow().setStatusBarColor(Color.BLACK);

        ImageView backButton = findViewById(R.id.iv_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { onBackPressed(); }
        });

        currentPasswordInput = findViewById(R.id.et_current_password);
        newPasswordInput = findViewById(R.id.et_new_password);
        confirmPasswordInput = findViewById(R.id.et_confirm_password);

        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Please wait...");

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        findViewById(R.id.btn_update_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { updatePassword(); }
        });
    }

    private boolean validatePasswordInput(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updatePassword() {
        final String currentPassword = currentPasswordInput.getText().toString().trim();
        final String newPassword = newPasswordInput.getText().toString().trim();
        final String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (!validatePasswordInput(currentPassword, newPassword, confirmPassword)) return;

        if (currentUser == null || currentUser.getEmail() == null) {
            Toast.makeText(this, "No signed-in user found", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Re-authenticating...");
        progressDialog.show();

        // Reauthenticate user with current email + current password
        AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPassword);
        currentUser.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> reauthTask) {
                        if (reauthTask.isSuccessful()) {
                            // Now update password
                            progressDialog.setMessage("Updating password...");
                            currentUser.updatePassword(newPassword)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> updateTask) {
                                            progressDialog.dismiss();
                                            if (updateTask.isSuccessful()) {
                                                Toast.makeText(ChangePasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                finish();
                                            } else {
                                                // Handle update errors (e.g., weak-password)
                                                Exception e = updateTask.getException();
                                                if (e instanceof FirebaseAuthRecentLoginRequiredException) {
                                                    Toast.makeText(ChangePasswordActivity.this, "Please sign in again and try.", Toast.LENGTH_LONG).show();
                                                } else {
                                                    String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Failed to update password";
                                                    Toast.makeText(ChangePasswordActivity.this, msg, Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        }
                                    });
                        } else {
                            progressDialog.dismiss();
                            Exception e = reauthTask.getException();
                            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                                // Wrong current password
                                Toast.makeText(ChangePasswordActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                            } else {
                                // Could be requires-recent-login or other issues
                                String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "Re-authentication failed";
                                Toast.makeText(ChangePasswordActivity.this, msg, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }
}
