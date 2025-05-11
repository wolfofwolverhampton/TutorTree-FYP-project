package com.javainternal.Students;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javainternal.R;
import com.javainternal.Students.Model.StudentUserModel;

import java.util.Random;

public class OTPNumberInputStudent extends AppCompatActivity {

    private EditText phoneNumberEditText;
    private Button sendOTPButton;

    private static final int SMS_PERMISSION_CODE = 100;

    // Firebase Database reference
    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpnumber_input_student);

        // Initialize Firebase Database reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        // Initialize UI components
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        sendOTPButton = findViewById(R.id.sendOTPButton);

        // Set click listener for the "Send OTP" button
        sendOTPButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEditText.getText().toString().trim();

                // Validate phone number (exactly 10 digits)
                if (!isValidPhoneNumber(phoneNumber)) {
                    return;
                }

                // Check if the app has permission to send SMS
                if (ContextCompat.checkSelfPermission(OTPNumberInputStudent.this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                    generateAndSendOTP(phoneNumber);
                } else {
                    // Request permission to send SMS
                    ActivityCompat.requestPermissions(OTPNumberInputStudent.this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with sending OTP
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                if (isValidPhoneNumber(phoneNumber)) {
                    generateAndSendOTP(phoneNumber);
                }
            } else {
                // Permission denied
                Toast.makeText(this, "SMS permission is required to send OTP.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Validate that the phone number is exactly 10 digits
        if (phoneNumber.isEmpty()) {
            Toast.makeText(this, "Please enter a phone number.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (phoneNumber.length() != 10 || !phoneNumber.matches("\\d+")) { // Ensure it's numeric and 10 digits
            Toast.makeText(this, "Phone number must be exactly 10 digits.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void generateAndSendOTP(String phoneNumber) {
        // Generate a random 6-digit OTP
        String otp = generateRandomOTP();

        // Save the phone number and OTP to Firebase
        saveUserToFirebase(phoneNumber, otp);

        // Send the OTP via SMS
        try {
            SmsManager smsManager = SmsManager.getDefault();
            String message = otp + " is your verification code.";
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            // Navigate to OTPReceiveStudent activity
            Intent intent = new Intent(OTPNumberInputStudent.this, SignUpStudent.class);
            intent.putExtra("uid", phoneNumber); // Pass the phone number
            startActivity(intent);

            // Show success message to the user
            Toast.makeText(this, "OTP sent successfully to " + phoneNumber, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to send OTP: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String generateRandomOTP() {
        Random random = new Random();
        int otpValue = 100000 + random.nextInt(900000); // Generates a 6-digit number
        return String.valueOf(otpValue);
    }

    private void saveUserToFirebase(String phoneNumber, String otp) {
        // Create a unique key for the student using their phone number
        String userId = phoneNumber; // Phone number acts as the unique ID

        // Create a StudentUserModel object with default values for new fields
        StudentUserModel student = new StudentUserModel(
                userId, // UID (phone number)
                "", // Name (empty for now)
                phoneNumber, // Phone Number
                "", // Guardian Name (empty for now)
                ""  // Password (empty for now)
        );

        // Add the OTP to the model
        student.setOtpReceived(otp);

        // Initialize new fields (gmail and guardianGmail) with empty values
        student.setGmail(""); // Empty Gmail
        student.setGuardianGmail(""); // Empty Guardian Gmail

        // Save the student data to Firebase under the "students" node
        studentsRef.child(userId).setValue(student)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "User data saved to Firebase", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to save user data: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}