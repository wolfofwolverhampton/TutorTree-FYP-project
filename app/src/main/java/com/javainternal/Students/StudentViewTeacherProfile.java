package com.javainternal.Students;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.CallActivity;
import com.javainternal.ChatActivity;
import com.javainternal.R;
import com.javainternal.TuitionPackageActivity;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentViewTeacherProfile extends AppCompatActivity {
    private TextView teacherName, teacherGmail, gradeCategory, subjectCategory;
    private Button chatButton, videoCallButton, packagesButton;

    private DatabaseReference teachersRef;
    private DatabaseReference studentsRef;
    private String studentGmail;
    private String studentName;
    private CircleImageView profileImage;
    private String studentUid;
    private String teacherUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view_teacher_profile);

        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        profileImage = findViewById(R.id.profileImage);
        teacherName = findViewById(R.id.teacherName);
        teacherGmail = findViewById(R.id.teacherGmail);
        gradeCategory = findViewById(R.id.gradeCategory);
        subjectCategory = findViewById(R.id.subjectCategory);
        chatButton = findViewById(R.id.chatButton);
        packagesButton = findViewById(R.id.tuitionPackages);

        Intent intent = getIntent();
        teacherUid = intent.getStringExtra("uid");

        studentUid = UserAuthContext.getInstance(this).getLoggedInPhone();

        if (teacherUid == null || teacherUid.isEmpty()) {
            Toast.makeText(this, "Teacher UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchTeacherData(teacherUid);

        chatButton.setOnClickListener(v -> {
            if (studentUid == null || studentUid.isEmpty() || teacherUid == null || teacherUid.isEmpty()) {
                Toast.makeText(this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra("name", teacherName.getText().toString());
            chatIntent.putExtra("senderUid", studentUid);
            chatIntent.putExtra("receiverUid", teacherUid);
            startActivity(chatIntent);
        });
        videoCallButton = findViewById(R.id.videoCall);

        videoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                intent.putExtra("teacherUid", teacherUid);
                intent.putExtra("studentUid", studentUid);
                startActivity(intent);
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
                    String grade = dataSnapshot.child("category").getValue(String.class);
                    String subjects = dataSnapshot.child("subjects").getValue(String.class);
                    String profileImageUrl = dataSnapshot.child("profilePicture").getValue(String.class);

                    teacherName.setText(name != null ? name : "N/A");
                    teacherGmail.setText(gmail != null ? gmail : "N/A");
                    gradeCategory.setText("Grade: " + (grade != null ? grade : "N/A"));
                    subjectCategory.setText("Subject: " + (subjects != null ? subjects : "N/A"));

                    if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                        Glide.with(StudentViewTeacherProfile.this)
                                .load(getString(R.string.backend_url) + profileImageUrl + "?t=" + System.currentTimeMillis())
                                .placeholder(R.drawable.ic_person)
                                .into(profileImage);
                    }
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