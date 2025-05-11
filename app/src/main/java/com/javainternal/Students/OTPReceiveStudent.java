package com.javainternal.Students;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.R;
import com.mukeshsolanki.OtpView;

public class OTPReceiveStudent extends AppCompatActivity {

    private TextView phoneLbl;
    private OtpView otpView;
    private Button continueBtn;

    private DatabaseReference studentsRef;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otpreceive_student);

        // Initialize UI components
        phoneLbl = findViewById(R.id.phoneLbl);
        otpView = findViewById(R.id.otp_view);
        continueBtn = findViewById(R.id.continueBtn);

        // Retrieve the phone number from the Intent
        phoneNumber = getIntent().getStringExtra("phoneNumber");
        if (phoneNumber != null) {
            phoneLbl.setText("Verify +" + phoneNumber); // Display the phone number
        }

        // Initialize Firebase Database reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students").child(phoneNumber);

        // Set click listener for the "Continue" button
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredOTP = otpView.getText().toString().trim();

                // Validate OTP input
                if (enteredOTP.isEmpty() || enteredOTP.length() != 6) {
                    Toast.makeText(OTPReceiveStudent.this, "Please enter a valid 6-digit OTP.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Verify OTP with Firebase
                verifyOTP(enteredOTP);
            }
        });
    }

    private void verifyOTP(String enteredOTP) {
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the OTP from Firebase
                    String otpReceived = dataSnapshot.child("otpReceived").getValue(String.class);

                    if (otpReceived != null && otpReceived.equals(enteredOTP)) {
                        // OTP matches, navigate to SignUpStudent activity
                        Toast.makeText(OTPReceiveStudent.this, "OTP verified successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(OTPReceiveStudent.this, SignUpStudent.class);
                        intent.putExtra("uid", phoneNumber); // Pass the phone number as uid
                        startActivity(intent);
                        finish(); // Close the current activity
                    } else {
                        // OTP does not match
                        Toast.makeText(OTPReceiveStudent.this, "Invalid OTP. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // No data found for the phone number
                    Toast.makeText(OTPReceiveStudent.this, "No user data found for this phone number.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(OTPReceiveStudent.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}