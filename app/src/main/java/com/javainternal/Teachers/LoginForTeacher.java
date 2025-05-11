package com.javainternal.Teachers;

import android.content.Intent;
import android.os.Bundle;
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
import com.javainternal.R;

public class LoginForTeacher extends AppCompatActivity {

    private EditText phoneNumberEditText, passwordEditText;
    private Button loginButton, signUpButton;

    private DatabaseReference teachersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_for_teacher);

        // Initialize Firebase Database reference
        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");

        // Initialize UI components
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        // Set click listener for the "Login" button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve input values
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Validate inputs
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(LoginForTeacher.this, "Please enter your phone number.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(LoginForTeacher.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Authenticate the user
                authenticateUser(phoneNumber, password);
            }
        });

        // Set click listener for the "Sign Up" button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to OTPNumberInputTeacher activity
                Intent intent = new Intent(LoginForTeacher.this, OTPNumberInputTeacher.class);
                startActivity(intent);
            }
        });
    }

    private void authenticateUser(String phoneNumber, String password) {
        // Query the database for the teacher with the given phone number
        teachersRef.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the password from Firebase
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);

                    // Compare the entered password with the stored password
                    if (storedPassword != null && storedPassword.equals(password)) {
                        // Password matches, navigate to HomePageTeacher activity
                        Toast.makeText(LoginForTeacher.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        // Set the teacher UID in the GlobalTeacherUid singleton
                        GlobalTeacherUid.getInstance().setTeacherUid(phoneNumber);
                        Intent intent = new Intent(LoginForTeacher.this, HomePageTeacher.class);
                        intent.putExtra("uid", phoneNumber); // Pass the phone number as uid
                        startActivity(intent);
                        finish(); // Close the current activity
                    } else {
                        // Password does not match
                        Toast.makeText(LoginForTeacher.this, "Invalid phone number or password.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // No data found for the phone number
                    Toast.makeText(LoginForTeacher.this, "No account found with this phone number.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginForTeacher.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}