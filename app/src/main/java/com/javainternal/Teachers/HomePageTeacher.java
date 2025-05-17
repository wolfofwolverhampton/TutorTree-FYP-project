package com.javainternal.Teachers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.Assignments.AssignmentActivity;
import com.javainternal.R;

public class HomePageTeacher extends AppCompatActivity {

    private MaterialButton myStudentButton, findStudentButton, createAssignmentButton, createQuestionSetButton, studentAssignmentsButton, studentGroupsButton;
    private TextView titleTextView;
    private DatabaseReference teachersRef;
    private ImageView settingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page_teacher);

        titleTextView = findViewById(R.id.titleTextView);
        myStudentButton = findViewById(R.id.myStudentButton);
        findStudentButton = findViewById(R.id.findStudentButton);
        createAssignmentButton = findViewById(R.id.createAssignmentButton);
        createQuestionSetButton = findViewById(R.id.createQuestionSetButton);
        settingButton = findViewById(R.id.settingButton);
        studentAssignmentsButton = findViewById(R.id.studentAssignmentsButton);
        studentGroupsButton = findViewById(R.id.studentGroupsButton);

        String uid = getIntent().getStringExtra("uid");
        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");

        fetchTeacherName(uid);

        myStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTeacher.this, TeacherMyStudent.class);
                startActivity(intent);
            }
        });

        findStudentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTeacher.this, TeacherFindStudent.class);
                startActivity(intent);
            }
        });

        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomePageTeacher.this, TeacherSetting.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
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

        studentAssignmentsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTeacher.this, AssignmentActivity.class);
                startActivity(intent);
            }
        });

        studentGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageTeacher.this, StudentGroupsActivity.class);
                startActivity(intent);
            }
        });
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
}