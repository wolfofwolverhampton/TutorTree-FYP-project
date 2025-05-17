package com.javainternal.Teachers;

import android.os.Bundle;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.Assignments.Adapter.StudentsAdapter;
import com.javainternal.Constants.SubscriptionStatus;
import com.javainternal.R;
import com.javainternal.Students.Model.StudentUserModel;

import java.util.*;

public class StudentGroupsActivity extends AppCompatActivity {
    private EditText groupNameEditText;
    private RecyclerView studentsRecyclerView;
    private Button createGroupButton;
    private List<StudentUserModel> studentList = new ArrayList<>();
    private List<String> selectedStudentUids = new ArrayList<>();
    private StudentsAdapter adapter;
    private DatabaseReference databaseRef;
    private String teacherUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_groups);

        groupNameEditText = findViewById(R.id.groupNameEditText);
        studentsRecyclerView = findViewById(R.id.studentsRecyclerView);
        createGroupButton = findViewById(R.id.createGroupButton);

        teacherUid = UserAuthContext.getInstance(this).getLoggedInPhone();
        databaseRef = FirebaseDatabase.getInstance().getReference();

        adapter = new StudentsAdapter(studentList, selectedStudentUids);
        studentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        studentsRecyclerView.setAdapter(adapter);

        loadSubscribedStudents();

        createGroupButton.setOnClickListener(v -> createGroup());
    }

    private void loadSubscribedStudents() {
        databaseRef.child("subscriptions")
                .orderByChild("teacherUid").equalTo(teacherUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        studentList.clear();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            SubscriptionStatus status = data.child("statusEnum").getValue(SubscriptionStatus.class);
                            if (SubscriptionStatus.PAID != status) continue;

                            String studentUid = data.child("studentUid").getValue(String.class);
                            if (studentUid != null) {
                                databaseRef.child("students").child(studentUid)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot studentSnap) {
                                                StudentUserModel student = studentSnap.getValue(StudentUserModel.class);
                                                if (student != null) {
                                                    student.setUid(studentSnap.getKey());
                                                    studentList.add(student);
                                                    adapter.notifyDataSetChanged();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) { }
                                        });
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });
    }

    private void createGroup() {
        String groupName = groupNameEditText.getText().toString().trim();

        if (groupName.isEmpty()) {
            groupNameEditText.setError("Group name required");
            return;
        }

        if (selectedStudentUids.isEmpty()) {
            Toast.makeText(this, "Select at least one student", Toast.LENGTH_SHORT).show();
            return;
        }

        String groupId = databaseRef.child("student_groups").push().getKey();
        Map<String, Object> groupData = new HashMap<>();
        groupData.put("groupName", groupName);
        groupData.put("teacherId", teacherUid);
        groupData.put("studentUids", selectedStudentUids);

        databaseRef.child("student_groups").child(groupId).setValue(groupData)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Group created", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
