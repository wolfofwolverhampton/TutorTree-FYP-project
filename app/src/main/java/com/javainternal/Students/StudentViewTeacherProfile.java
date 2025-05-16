package com.javainternal.Students;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.CallActivity;
import com.javainternal.ChatForFind;
import com.javainternal.MainActivity2;
import com.javainternal.R;
import com.javainternal.TuitionPackageActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class StudentViewTeacherProfile extends AppCompatActivity {
    private TextView teacherName, teacherGmail, gradeCategory, subjectCategory;
    private Button chatButton, videoCallButton, packagesButton;

    private DatabaseReference teachersRef;
    private DatabaseReference studentsRef;
    private String studentGmail;
    private String studentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view_teacher_profile);

        // Initialize Firebase Database reference
        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        // Initialize UI components
        teacherName = findViewById(R.id.teacherName);
        teacherGmail = findViewById(R.id.teacherGmail);
        gradeCategory = findViewById(R.id.gradeCategory);
        subjectCategory = findViewById(R.id.subjectCategory);
        chatButton = findViewById(R.id.chatButton);
        packagesButton = findViewById(R.id.tuitionPackages);

        // Retrieve the UID (phone number) from the Intent
        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");

        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch the teacher's data from Firebase
        fetchTeacherData(uid);

        // Set click listener for the "Chat" button
        chatButton.setOnClickListener(v -> {
            String studentUid = GlobalStudentUid.getInstance().getStudentUid();

            String teacherUid = getIntent().getStringExtra("uid");

            if (studentUid == null || studentUid.isEmpty() || teacherUid == null || teacherUid.isEmpty()) {
                Toast.makeText(this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent chatIntent = new Intent(this, ChatForFind.class);
            chatIntent.putExtra("name", teacherName.getText().toString()); // Pass the teacher's name
            chatIntent.putExtra("senderUid", studentUid); // Use the modified global student UID as sender
            chatIntent.putExtra("receiverUid", teacherUid); // Pass the teacher UID as receiver
            startActivity(chatIntent);
        });
        videoCallButton = findViewById(R.id.videoCall);

        videoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String studentUid = GlobalStudentUid.getInstance().getStudentUid();
                String teacherUid = getIntent().getStringExtra("uid");

                if (studentUid == null || studentUid.isEmpty() || teacherUid == null || teacherUid.isEmpty()) {
                    Toast.makeText(StudentViewTeacherProfile.this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent callIntent = new Intent(StudentViewTeacherProfile.this, CallActivity.class);
                callIntent.putExtra("username", studentUid);
                callIntent.putExtra("friendUsername", teacherUid);
                startActivity(callIntent);
            }
        });

        packagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentViewTeacherProfile.this, TuitionPackageActivity.class);
                intent.putExtra("teacherUid", getIntent().getStringExtra("uid"));
                intent.putExtra("studentUid", GlobalStudentUid.getInstance().getStudentUid());
                startActivity(intent);
            }
        });

    }
    private void fetchStudentGmail() {
        String studentUid = GlobalStudentUid.getInstance().getStudentUid();

        if (studentUid == null || studentUid.isEmpty()) {
            Toast.makeText(this, "Student UID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        studentsRef.child(studentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the name and Gmail from the snapshot
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String gmail = dataSnapshot.child("gmail").getValue(String.class);

                    // Store the fetched data in the variables
                    studentName = name != null ? name : "N/A";
                    studentGmail = gmail != null ? gmail : "N/A";

                    // Log the fetched data for debugging purposes
                    Log.d("StudentData", "Fetched Name: " + studentName);
                    Log.d("StudentData", "Fetched Gmail: " + studentGmail);

                    // Optionally, display the fetched data in a Toast or UI component
                    Toast.makeText(StudentViewTeacherProfile.this, "Student Name: " + studentName, Toast.LENGTH_SHORT).show();
                    Toast.makeText(StudentViewTeacherProfile.this, "Student Gmail: " + studentGmail, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StudentViewTeacherProfile.this, "Student data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentViewTeacherProfile.this, "Failed to load student data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchTeacherData(String uid) {
        teachersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String gmail = dataSnapshot.child("gmail").getValue(String.class);
                    String grade = dataSnapshot.child("category").getValue(String.class); // Assuming category includes grade
                    String subjects = dataSnapshot.child("subjects").getValue(String.class); // Add a "subjects" field in Firebase

                    // Populate the UI components
                    teacherName.setText(name != null ? name : "N/A");
                    teacherGmail.setText(gmail != null ? gmail : "N/A");
                    gradeCategory.setText("Grade: " + (grade != null ? grade : "N/A"));
                    subjectCategory.setText("Subject: " + (subjects != null ? subjects : "N/A"));
                } else {
                    Toast.makeText(StudentViewTeacherProfile.this, "Teacher data not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentViewTeacherProfile.this, "Failed to load teacher data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}