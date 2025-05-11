package com.javainternal.Students;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.javainternal.R;
import com.javainternal.Students.Model.StudentUserModel;

public class StudentSetting extends AppCompatActivity {

    private TextView nameTextView, gmailTextView, guardianNameTextView, guardianGmailTextView;
    private Button editButton, logoutButton, categoryButton;
    private DatabaseReference studentsRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_setting);

        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");
        studentsRef = FirebaseDatabase.getInstance().getReference("students").child(uid);

        // Initialize UI components
        nameTextView = findViewById(R.id.nameTextView2);
        gmailTextView = findViewById(R.id.gmailTextView2);
        guardianNameTextView = findViewById(R.id.guardianNameTextView2);
        guardianGmailTextView = findViewById(R.id.guardianGmailTextView2);

        editButton = findViewById(R.id.editButton2);
        logoutButton = findViewById(R.id.logoutButton);
        categoryButton = findViewById(R.id.categoryButton2);

        // Fetch and display student information
        fetchStudentInformation();

        // Set click listener for "Edit" button
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to EditProfileStudent activity
                Intent intent = new Intent(StudentSetting.this, EditProfileStudent.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        // Set click listener for "Category" button
        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to CategoryStudent activity
                Intent intent = new Intent(StudentSetting.this, CategoryStudent.class);
                intent.putExtra("uid", uid);
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
                Intent intent = new Intent(StudentSetting.this, LoginForStudent.class); // Replace with your login activity
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish(); // Close the current activity
            }
        });
    }

    private void fetchStudentInformation() {
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve student data from Firebase
                    StudentUserModel student = dataSnapshot.getValue(StudentUserModel.class);

                    // Display student information in TextViews
                    nameTextView.setText(student.getName());
                    gmailTextView.setText(student.getGmail());
                    guardianNameTextView.setText(student.getGuardianName());
                    guardianGmailTextView.setText(student.getGuardianGmail());
                } else {
                    Toast.makeText(StudentSetting.this, "Failed to load student information.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentSetting.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}