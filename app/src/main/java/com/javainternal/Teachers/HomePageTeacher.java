package com.javainternal.Teachers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

public class HomePageTeacher extends AppCompatActivity {

    private Button myStudentButton, findStudentButton, logoutButton, createAssignmentButton, createQuestionSetButton;
    private TextView titleTextView;
    private DatabaseReference teachersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page_teacher);

        // Initialize UI components
        titleTextView = findViewById(R.id.titleTextView);
        myStudentButton = findViewById(R.id.myStudentButton);
        findStudentButton = findViewById(R.id.findStudentButton);
        logoutButton = findViewById(R.id.logoutButton);
        createAssignmentButton = findViewById(R.id.createAssignmentButton);
        createQuestionSetButton = findViewById(R.id.createQuestionSetButton);

        // Get the UID (phone number) from the Intent
        String uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Database reference
        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");

        // Fetch and display the teacher's name
        fetchTeacherName(uid);

        // Set click listener for "My Student" button
        myStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTeacher.this, TeacherMyStudent.class);
                startActivity(intent);
            }
        });

        // Set click listener for "Find Student" button
        findStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTeacher.this, TeacherFindStudent.class);
                startActivity(intent);
            }
        });

        // Set click listener for "Log Out" button
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Sign out from Firebase Authentication
                FirebaseAuth.getInstance().signOut();

                // Navigate back to the login screen
                Intent intent = new Intent(HomePageTeacher.this, LoginForTeacher.class); // Replace with your login activity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        createAssignmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTeacher.this, CreateMCQ.class);
                startActivity(intent);
            }
        });

        createQuestionSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTeacher.this, CreateQuestionSetActivity.class);
                startActivity(intent);
            }
        });

        generateFcmToken();

    }

    private void fetchTeacherName(String uid) {
        teachersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String teacherName = dataSnapshot.child("name").getValue(String.class);
                    if (teacherName != null && !teacherName.isEmpty()) {
                        titleTextView.setText("Welcome, " + teacherName);
                    } else {
                        titleTextView.setText("Welcome, Teacher");
                    }
                } else {
                    titleTextView.setText("Welcome, Teacher");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomePageTeacher.this, "Failed to fetch teacher name: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                titleTextView.setText("Welcome, Teacher");
            }
        });
    }

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

                    // Save the token to Firebase Realtime Database under the 'teachers' node
                    DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("teachers").child(phoneNumber);
                    studentRef.child("fcmToken").setValue(token)
                            .addOnSuccessListener(aVoid -> Log.d("FCM", "FCM Token saved to database for UID: " + phoneNumber))
                            .addOnFailureListener(e -> Log.e("FCM", "Failed to save FCM Token to database for UID: " + phoneNumber, e));
                });
    }
}