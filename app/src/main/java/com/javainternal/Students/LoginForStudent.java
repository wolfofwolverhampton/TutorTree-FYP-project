package com.javainternal.Students;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.Utils.FirebaseUtils;
import com.javainternal.databinding.ActivityLoginForStudentsBinding;

public class LoginForStudent extends AppCompatActivity {

    private ActivityLoginForStudentsBinding binding; // Declare the binding object

    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize View Binding
        binding = ActivityLoginForStudentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Database reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        // Set an OnClickListener for the Sign Up Button
        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to OTPNumberInputStudent Activity
                Intent intent = new Intent(LoginForStudent.this, OTPNumberInputStudent.class);
                startActivity(intent);
            }
        });

        // Set an OnClickListener for the Login Button
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve user input
                String phoneNumber = binding.phoneNumberEditText.getText().toString().trim();
                String password = binding.passwordEditText.getText().toString().trim();

                // Validate input fields
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(LoginForStudent.this, "Please enter your phone number.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(LoginForStudent.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Perform login logic
                performLogin(phoneNumber, password);
            }
        });
    }

    private void performLogin(String phoneNumber, String password) {
        // Query Firebase to find the student with the given phone number
        studentsRef.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);

                    if (storedPassword != null && storedPassword.equals(password)) {
                        Toast.makeText(LoginForStudent.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        FirebaseUtils.saveFcmToken(getApplicationContext(), phoneNumber);

                        GlobalStudentUid.getInstance().setStudentUid(phoneNumber);

                        Intent intent = new Intent(LoginForStudent.this, HomePageStudent.class);
                        intent.putExtra("uid", phoneNumber); // Pass the phone number as uid
                        startActivity(intent);
                        finish(); // Close the current activity
                    } else {
                        // Password does not match
                        Toast.makeText(LoginForStudent.this, "Invalid password. Please try again.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // No data found for the phone number
                    Toast.makeText(LoginForStudent.this, "No account found for this phone number.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(LoginForStudent.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}