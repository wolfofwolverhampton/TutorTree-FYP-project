package com.javainternal.Students;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javainternal.R;
import com.javainternal.Students.Model.StudentUserModel;

public class EditProfileStudent extends AppCompatActivity {

    private EditText nameEditText, gmailEditText, guardianGmailEditText, guardianNameEditText, passwordEditText;
    private Button saveButton;
    private DatabaseReference studentsRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_student);

        // Retrieve the UID (phone number) from the Intent
        uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Database reference
        studentsRef = FirebaseDatabase.getInstance().getReference("students").child(uid);

        // Initialize UI components
        nameEditText = findViewById(R.id.nameEditText);
        gmailEditText = findViewById(R.id.gmailEditText);
        guardianGmailEditText = findViewById(R.id.guardianGmailEditText);
        guardianNameEditText = findViewById(R.id.guardianNameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        saveButton = findViewById(R.id.saveButton);

        // Fetch and populate the current student's data
        fetchStudentData();

        // Set click listener for the "Save" button
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateStudentProfile();
            }
        });
    }

    private void fetchStudentData() {
        studentsRef.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the student's data
                    StudentUserModel student = dataSnapshot.getValue(StudentUserModel.class);
                    if (student != null) {
                        // Populate the EditText fields with the current data
                        nameEditText.setText(student.getName());
                        gmailEditText.setText(student.getGmail());
                        guardianGmailEditText.setText(student.getGuardianGmail());
                        guardianNameEditText.setText(student.getGuardianName());
                        passwordEditText.setText(student.getPassword());
                    }
                } else {
                    Toast.makeText(EditProfileStudent.this, "Failed to load student information.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(com.google.firebase.database.DatabaseError error) {
                Toast.makeText(EditProfileStudent.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateStudentProfile() {
        // Get updated values from the EditText fields
        String newName = nameEditText.getText().toString().trim();
        String newGmail = gmailEditText.getText().toString().trim();
        String newGuardianGmail = guardianGmailEditText.getText().toString().trim();
        String newGuardianName = guardianNameEditText.getText().toString().trim();
        String newPassword = passwordEditText.getText().toString().trim();

        // Validate inputs
        if (newName.isEmpty() || newGmail.isEmpty() || newGuardianGmail.isEmpty() || newGuardianName.isEmpty() || newPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update the student's data in Firebase
        studentsRef.child("name").setValue(newName);
        studentsRef.child("gmail").setValue(newGmail);
        studentsRef.child("guardianGmail").setValue(newGuardianGmail);
        studentsRef.child("guardianName").setValue(newGuardianName);
        studentsRef.child("password").setValue(newPassword)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfileStudent.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity after saving
                    } else {
                        Toast.makeText(EditProfileStudent.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}