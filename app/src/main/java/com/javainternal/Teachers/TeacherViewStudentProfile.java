package com.javainternal.Teachers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.CallActivity;
import com.javainternal.ChatForFind;
import com.javainternal.MainActivity2;
import com.javainternal.R;

public class TeacherViewStudentProfile extends AppCompatActivity {

    private TextView studentName, gradeCategory, subjectCategory, studentGmail;
    private Button chatButton;

    private DatabaseReference studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_view_student_profile);

        // Initialize Firebase Database reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        // Initialize UI components
        studentName = findViewById(R.id.studentName);
        gradeCategory = findViewById(R.id.gradeCategory); // Ensure this matches the XML ID
        subjectCategory = findViewById(R.id.subjectCategory); // Ensure this matches the XML ID
        studentGmail = findViewById(R.id.studentGmail); // Ensure this matches the XML ID
        chatButton = findViewById(R.id.chatButton);

        // Retrieve the UID (phone number) from the Intent
        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");

        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch the student's data from Firebase
        fetchStudentData(uid);
        // Set click listener for the "Chat" button
        chatButton.setOnClickListener(v -> {
            // Retrieve the teacher UID (sender) from the GlobalTeacherUid singleton
            String teacherUid = GlobalTeacherUid.getInstance().getTeacherUid();

            // Retrieve the student UID (receiver) from the Intent
            String studentUid = getIntent().getStringExtra("uid");
            studentUid = studentUid + "2";
            
            // Validate the UIDs
            if (teacherUid == null || teacherUid.isEmpty() || studentUid == null || studentUid.isEmpty()) {
                Toast.makeText(this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create an Intent to navigate to ChatForFind activity
            Intent chatIntent = new Intent(this, ChatForFind.class);
            chatIntent.putExtra("name", studentName.getText().toString()); // Pass the student's name
            chatIntent.putExtra("senderUid", teacherUid); // Use the global teacher UID as sender
            chatIntent.putExtra("receiverUid", studentUid); // Pass the student UID as receiver
            startActivity(chatIntent);
        });
        // Find the videoCall2 button by its ID
        Button videoCallButton = findViewById(R.id.videoCall2);

// Set an OnClickListener for the button
        videoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the teacher UID (username) from the GlobalTeacherUid singleton
                String teacherUid = GlobalTeacherUid.getInstance().getTeacherUid();

                // Retrieve the student UID (friendUsername) from the Intent
                String studentUid = getIntent().getStringExtra("uid");

                // Validate the UIDs
                if (teacherUid == null || teacherUid.isEmpty() || studentUid == null || studentUid.isEmpty()) {
                    Toast.makeText(TeacherViewStudentProfile.this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create an Intent to navigate to CallActivity
                Intent callIntent = new Intent(TeacherViewStudentProfile.this, CallActivity.class);
                callIntent.putExtra("username", teacherUid); // Pass the teacher UID as username
                callIntent.putExtra("friendUsername", studentUid); // Pass the student UID as friendUsername
                startActivity(callIntent);
            }
        });
    }

    private void fetchStudentData(String uid) {
        studentsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the student's data
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String category = dataSnapshot.child("category").getValue(String.class);
                    String gmail = dataSnapshot.child("gmail").getValue(String.class);

                    // Parse the category into Grade and Subjects
                    String grade = "N/A";
                    String subjects = "N/A";

                    if (category != null && !category.isEmpty()) {
                        int colonIndex = category.indexOf(":");
                        if (colonIndex != -1) {
                            grade = category.substring(0, colonIndex).trim(); // "Grade 9-10"
                            subjects = category.substring(colonIndex + 1).replace("[", "").replace("]", "").trim(); // "Science, Mathematics"
                        } else {
                            grade = category.trim();
                        }
                    }

                    // Populate the UI components
                    studentName.setText(name != null ? name : "N/A");
                    gradeCategory.setText("Grade: " + grade);
                    subjectCategory.setText("Subjects: " + subjects);
                    studentGmail.setText(gmail != null ? gmail : "Gmail: N/A");
                } else {
                    Toast.makeText(TeacherViewStudentProfile.this, "Student data not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherViewStudentProfile.this, "Failed to load student data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}