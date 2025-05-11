package com.javainternal.Students;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.javainternal.R;
import com.javainternal.Teachers.HomePageTeacher;
import com.javainternal.Teachers.LoginForTeacher;

public class HomePageStudent extends AppCompatActivity {

    private Button myTeacherButton, findTeacherButton;
    private TextView titleTextView; // Add a TextView for the welcome message
    private DatabaseReference studentsRef;

    private ImageView settingButton; // Change from Button to ImageView
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page_student);

        // Initialize UI components
        titleTextView = findViewById(R.id.titleTextView);
        myTeacherButton = findViewById(R.id.myTeacherButton);
        findTeacherButton = findViewById(R.id.findTeacherButton);
        settingButton = findViewById(R.id.settingButton); // Now an ImageView


        // Get the UID (phone number) from the Intent
        String uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Database reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        // Fetch and display the student's name
        fetchStudentName(uid);

        // Set click listener for "My Teacher" button
        myTeacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to StudentMyTeacher activity
                Intent intent = new Intent(HomePageStudent.this, StudentMyTeacher.class);
                startActivity(intent);
            }
        });

        // Set click listener for "Find Teacher" button
        findTeacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to StudentFindTeacher activity
                Intent intent = new Intent(HomePageStudent.this, StudentFindTeacher.class);
                startActivity(intent);
            }
        });
        // Set click listener for "Log Out" button
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Navigate back to the login screen
                Intent intent = new Intent(HomePageStudent.this, StudentSetting.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });

        generateFcmToken();
    }

    private void fetchStudentName(String uid) {
        // Query Firebase to fetch the student's name
        studentsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the student's name
                    String studentName = dataSnapshot.child("name").getValue(String.class);

                    // Update the welcome message with the student's name
                    if (studentName != null && !studentName.isEmpty()) {
                        titleTextView.setText("Welcome, " + studentName);
                    } else {
                        titleTextView.setText("Welcome, Student"); // Default message if name is missing
                    }
                } else {
                    // No data found for the UID
                    titleTextView.setText("Welcome, Student"); // Default message
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
                Toast.makeText(HomePageStudent.this, "Failed to fetch student name: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                titleTextView.setText("Welcome, Student"); // Default message
            }
        });

    }

    // Method to generate FCM token
    // Method to generate FCM token
    private void generateFcmToken() {
        // Retrieve the phone number (UID) from the Intent or any other source
        String phoneNumber = getIntent().getStringExtra("uid"); // Assuming phone number is passed via Intent

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.w("FCM", "Phone number (UID) is null or empty. Cannot save FCM Token.");
            return;
        }

        // Generate FCM token
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get the FCM token
                    String token = task.getResult();
                    Log.d("FCM", "FCM Token: " + token);

                    // Save the token to Firebase Realtime Database under the 'students' node
                    DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students").child(phoneNumber);
                    studentRef.child("fcmToken").setValue(token)
                            .addOnSuccessListener(aVoid -> Log.d("FCM", "FCM Token saved to database for UID: " + phoneNumber))
                            .addOnFailureListener(e -> Log.e("FCM", "Failed to save FCM Token to database for UID: " + phoneNumber, e));
                });
    }
}