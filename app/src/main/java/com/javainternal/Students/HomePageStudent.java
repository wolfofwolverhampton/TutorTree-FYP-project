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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.javainternal.R;
import com.javainternal.TuitionPackageActivity;

public class HomePageStudent extends AppCompatActivity {

    private Button myTeacherButton, findTeacherButton, assignmentsButton;
    private TextView titleTextView;
    private DatabaseReference studentsRef;

    private ImageView settingButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page_student);

        titleTextView = findViewById(R.id.titleTextView);
        myTeacherButton = findViewById(R.id.myTeacherButton);
        findTeacherButton = findViewById(R.id.findTeacherButton);
        settingButton = findViewById(R.id.settingButton); // Now an ImageView


        String uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        fetchStudentName(uid);

        myTeacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to StudentMyTeacher activity
                Intent intent = new Intent(HomePageStudent.this, MyTeacher.class);
                startActivity(intent);
            }
        });

        findTeacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to StudentFindTeacher activity
                Intent intent = new Intent(HomePageStudent.this, StudentFindTeacher.class);
                startActivity(intent);
            }
        });

        assignmentsButton = findViewById(R.id.assignmentsButton);
        assignmentsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageStudent.this, StudentAssignmentsActivity.class);
            startActivity(intent);
        });


        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomePageStudent.this, StudentSetting.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        generateFcmToken();
    }

    private void fetchStudentName(String uid) {
        studentsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String studentName = dataSnapshot.child("name").getValue(String.class);

                    if (studentName != null && !studentName.isEmpty()) {
                        titleTextView.setText("Welcome, " + studentName);
                    } else {
                        titleTextView.setText("Welcome, Student");
                    }
                } else {
                    titleTextView.setText("Welcome, Student");
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

    private void generateFcmToken() {
        String phoneNumber = getIntent().getStringExtra("uid");

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.w("FCM", "Phone number (UID) is null or empty. Cannot save FCM Token.");
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM", "FCM Token: " + token);

                    DatabaseReference studentRef = FirebaseDatabase.getInstance().getReference("students").child(phoneNumber);
                    studentRef.child("fcmToken").setValue(token)
                            .addOnSuccessListener(aVoid -> Log.d("FCM", "FCM Token saved to database for UID: " + phoneNumber))
                            .addOnFailureListener(e -> Log.e("FCM", "Failed to save FCM Token to database for UID: " + phoneNumber, e));
                });
    }
}