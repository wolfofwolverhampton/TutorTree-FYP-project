package com.javainternal.Students;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.R;
import com.javainternal.Students.Model.StudentUserModel;
import com.mukeshsolanki.OtpView;

public class SignUpStudent extends AppCompatActivity {

    private EditText nameEditText, gmailEditText, guardianNameEditText, guardianGmailEditText, passwordEditText;
    private Button signUpButton;
    private OtpView otpView; // Added OTP View
    private DatabaseReference studentsRef;
    private String uid;
    private CountDownTimer otpExpiryTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_student);

        // Retrieve the uid (phone number) from the Intent
        uid = getIntent().getStringExtra("uid");
        if (uid == null) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Database reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students").child(uid);

        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        gmailEditText = findViewById(R.id.gmailEditText);
        guardianNameEditText = findViewById(R.id.guardianNameEditText);
        guardianGmailEditText = findViewById(R.id.guardianGmailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        otpView = findViewById(R.id.otp_view); // Initialize OTP View
        signUpButton = findViewById(R.id.signUpButton);

        // Start a 5-minute countdown timer to delete the user if OTP is not verified
        startOtpExpiryTimer();

        // Set click listener for the "Sign Up" button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSignUp();
            }
        });
    }

    private void startOtpExpiryTimer() {
        otpExpiryTimer = new CountDownTimer(5 * 60 * 1000, 1000) { // 5 minutes
            @Override
            public void onTick(long millisUntilFinished) {
                // Optional: Display remaining time to the user
            }

            @Override
            public void onFinish() {
                // Delete the user from Firebase if OTP is not verified within 5 minutes
                studentsRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(SignUpStudent.this, "OTP expired. User data deleted.", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity
                    } else {
                        Toast.makeText(SignUpStudent.this, "Failed to delete user data.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }.start();
    }

    private void validateAndSignUp() {
        // Get user input
        String name = nameEditText.getText().toString().trim();
        String gmail = gmailEditText.getText().toString().trim();
        String guardianName = guardianNameEditText.getText().toString().trim();
        String guardianGmail = guardianGmailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String enteredOtp = otpView.getText().toString().trim();

        // Validate input fields
        if (name.isEmpty() || gmail.isEmpty() || guardianName.isEmpty() || guardianGmail.isEmpty() || password.isEmpty() || enteredOtp.isEmpty()) {
            Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate Gmail format
        if (!Patterns.EMAIL_ADDRESS.matcher(gmail).matches()) {
            Toast.makeText(this, "Invalid Gmail address.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password requirements
        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Must be 8 digit and add numbers and Special character", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the OTP from Firebase and compare it with the entered OTP
        studentsRef.child("otpReceived").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String storedOtp = dataSnapshot.getValue(String.class);

                if (storedOtp == null || !storedOtp.equals(enteredOtp)) {
                    Toast.makeText(SignUpStudent.this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // OTP matches, proceed to update user info in Firebase
                updateUserInfoInFirebase(name, gmail, guardianName, guardianGmail, password);

                // Cancel the OTP expiry timer
                if (otpExpiryTimer != null) {
                    otpExpiryTimer.cancel();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(SignUpStudent.this, "Failed to fetch OTP: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    private void updateUserInfoInFirebase(String name, String gmail, String guardianName, String guardianGmail, String password) {
        // Create a StudentUserModel object with updated information
        StudentUserModel student = new StudentUserModel(
                uid, // UID (phone number)
                name,
                uid, // Phone number (already stored as UID)
                guardianName,
                password
        );
        student.setGmail(gmail);
        student.setGuardianGmail(guardianGmail);

        // Update the student data in Firebase
        studentsRef.setValue(student).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Navigate to CategoryStudent activity
                Intent intent = new Intent(SignUpStudent.this, CategoryStudent.class);
                intent.putExtra("uid", uid); // Pass the phone number as uid
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(SignUpStudent.this, "Failed to update user data: " + task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the timer when the activity is destroyed
        if (otpExpiryTimer != null) {
            otpExpiryTimer.cancel();
        }
    }
}