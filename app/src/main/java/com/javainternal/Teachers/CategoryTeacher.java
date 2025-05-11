package com.javainternal.Teachers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javainternal.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryTeacher extends AppCompatActivity {

    private RadioGroup gradeRadioGroup;
    private List<CheckBox> subjectCheckBoxes;
    private Button confirmButton;

    private DatabaseReference teachersRef;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_teacher);

        // Retrieve the UID (phone number) from the Intent
        uid = getIntent().getStringExtra("uid");
        if (uid == null) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize Firebase Database reference
        teachersRef = FirebaseDatabase.getInstance().getReference("teachers").child(uid);

        // Initialize UI components
        gradeRadioGroup = findViewById(R.id.gradeRadioGroup);
        confirmButton = findViewById(R.id.confirmButton);

        // Initialize subject CheckBoxes
        subjectCheckBoxes = new ArrayList<>();
        subjectCheckBoxes.add(findViewById(R.id.checkScience));
        subjectCheckBoxes.add(findViewById(R.id.checkMathematics));
        subjectCheckBoxes.add(findViewById(R.id.checkEnglish));
        subjectCheckBoxes.add(findViewById(R.id.checkAccountancy));
        subjectCheckBoxes.add(findViewById(R.id.checkNepali));
        subjectCheckBoxes.add(findViewById(R.id.checkBusiness));
        subjectCheckBoxes.add(findViewById(R.id.checkOptMath));

        // Set click listener for the "Confirm" button
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCategoryToFirebase();
            }
        });
    }

    private void saveCategoryToFirebase() {
        // Get the selected grade level
        int selectedGradeId = gradeRadioGroup.getCheckedRadioButtonId();
        if (selectedGradeId == -1) {
            Toast.makeText(this, "Please select a grade level.", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedGradeRadioButton = findViewById(selectedGradeId);
        String selectedGrade = selectedGradeRadioButton.getText().toString();

        // Get the selected subjects
        List<String> selectedSubjects = new ArrayList<>();
        for (CheckBox checkBox : subjectCheckBoxes) {
            if (checkBox.isChecked()) {
                selectedSubjects.add(checkBox.getText().toString());
            }
        }

        if (selectedSubjects.isEmpty()) {
            Toast.makeText(this, "Please select at least one subject.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save the category data to Firebase
        teachersRef.child("category").setValue(selectedGrade + ": " + selectedSubjects.toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CategoryTeacher.this, "Category saved successfully!", Toast.LENGTH_SHORT).show();
                        navigateToHomePageTeacher();
                    } else {
                        Toast.makeText(CategoryTeacher.this, "Failed to save category: " + task.getException(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToHomePageTeacher() {
        Intent intent = new Intent(CategoryTeacher.this, HomePageTeacher.class);
        intent.putExtra("uid", uid); // Pass the UID to the next activity
        startActivity(intent);
        finish(); // Close the current activity
    }
}