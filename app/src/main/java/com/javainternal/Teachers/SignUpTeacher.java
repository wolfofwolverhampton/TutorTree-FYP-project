package com.javainternal.Teachers;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mukeshsolanki.OtpView;
import com.javainternal.R;
import com.javainternal.Teachers.Model.TeacherUserModel;

public class SignUpTeacher extends AppCompatActivity {

    private EditText nameEditText, gmailEditText, passwordEditText;
    private Button signUpButton;
    private OtpView otpView;

    private DatabaseReference teachersRef;
    private String uid;
    private CountDownTimer otpExpiryTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_teacher);

        // Retrieve the uid (phone number) from the Intent
        uid = getIntent().getStringExtra("uid");
        if (uid == null) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Database reference
        teachersRef = FirebaseDatabase.getInstance().getReference("teachers").child(uid);

        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        gmailEditText = findViewById(R.id.gmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        otpView = findViewById(R.id.otp_view3);
        signUpButton = findViewById(R.id.signUpButton);

        // Start a 5-minute timer to delete the user if OTP is not verified
        startOtpExpiryTimer();

        // Set click listener for the "Sign Up" button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve input values
                String name = nameEditText.getText().toString().trim();
                String gmail = gmailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String enteredOtp = otpView.getText().toString().trim();

                // Validate input fields
                if (!validateInputs(name, gmail, password, enteredOtp)) {
                    return; // Stop further execution if validation fails
                }

                // Verify OTP and proceed with sign-up
                verifyOtpAndSignUp(enteredOtp, name, gmail, password);
            }
        });
    }

    private boolean validateInputs(String name, String gmail, String password, String otp) {
        // Validate Name
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate Gmail format
        if (!Patterns.EMAIL_ADDRESS.matcher(gmail).matches()) {
            Toast.makeText(this, "Please enter a valid Gmail address.", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate password requirements
        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Must be 8 digit and add numbers and Special character", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate OTP
        if (otp.isEmpty() || otp.length() != 6) {
            Toast.makeText(this, "Please enter a valid 6-digit OTP.", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true; // All validations passed
    }
    // Helper method to validate the password
    private boolean isPasswordValid(String password) {
        // Check if password length is greater than 8
        if (password.length() <= 8) {
            return false;
        }

        // Check for at least one letter, one number, and one special character
        boolean hasLetter = false, hasDigit = false, hasSpecialChar = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            } else if (!Character.isLetterOrDigit(c)) { // Special character check
                hasSpecialChar = true;
            }
        }

        return hasLetter && hasDigit && hasSpecialChar;
    }

    private void verifyOtpAndSignUp(String enteredOtp, String name, String gmail, String password) {
        // Fetch the OTP from Firebase and compare it with the entered OTP
        teachersRef.child("otpReceived").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String storedOtp = dataSnapshot.getValue(String.class);

                if (storedOtp == null || !storedOtp.equals(enteredOtp)) {
                    Toast.makeText(SignUpTeacher.this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }



                // OTP matches, proceed to update teacher info in Firebase
                updateTeacherInfoInFirebase(name, gmail, password);

                // Cancel the OTP expiry timer
                cancelOtpExpiryTimer();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignUpTeacher.this, "Failed to fetch OTP: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTeacherInfoInFirebase(String name, String gmail, String password) {
        // Create a TeacherUserModel object with updated information
        TeacherUserModel teacher = new TeacherUserModel(
                uid, // UID (phone number)
                name,
                uid, // Phone number (already stored as UID)
                password,
                "", // OTP Received (not needed here)
                gmail,
                ""  // Category (empty for now)
        );

        // Update the teacher data in Firebase
        teachersRef.setValue(teacher)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpTeacher.this, "Sign up successful!", Toast.LENGTH_SHORT).show();

                        // Navigate to CategoryTeacher activity
                        Intent intent = new Intent(SignUpTeacher.this, CategoryTeacher.class);
                        intent.putExtra("uid", uid); // Pass the phone number as uid
                        startActivity(intent);

                        finish(); // Close the current activity
                    } else {
                        Toast.makeText(SignUpTeacher.this, "Failed to update teacher data: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startOtpExpiryTimer() {
        otpExpiryTimer = new CountDownTimer(5 * 60 * 1000, 1000) { // 5 minutes timer
            @Override
            public void onTick(long millisUntilFinished) {
                // Optional: Show remaining time to the user
            }

            @Override
            public void onFinish() {
                // Delete the user if OTP is not verified within 5 minutes
                teachersRef.removeValue()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(SignUpTeacher.this, "OTP verification timed out. User deleted.", Toast.LENGTH_SHORT).show();
                                finish(); // Close the activity
                            } else {
                                Toast.makeText(SignUpTeacher.this, "Failed to delete user.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        }.start();
    }

    private void cancelOtpExpiryTimer() {
        if (otpExpiryTimer != null) {
            otpExpiryTimer.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelOtpExpiryTimer(); // Ensure the timer is canceled when the activity is destroyed
    }
}