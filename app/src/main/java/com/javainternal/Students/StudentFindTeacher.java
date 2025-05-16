package com.javainternal.Students;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.R;
import com.javainternal.Students.Adapter.StudentFindTeacherAdapter;
import com.javainternal.Teachers.Model.TeacherUserModel;

import java.util.ArrayList;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;

public class StudentFindTeacher extends AppCompatActivity {
    private ShimmerRecyclerView recyclerView;
    private DatabaseReference teachersRef;
    private ArrayList<TeacherUserModel> teachersList;
    private StudentFindTeacherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_find_teacher);

        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");

        recyclerView = findViewById(R.id.recyclerView);
        teachersList = new ArrayList<>();

        fetchTeachersFromFirebase();
    }

    private void fetchTeachersFromFirebase() {
        teachersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                teachersList.clear(); // Clear the list before adding new data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TeacherUserModel teacher = snapshot.getValue(TeacherUserModel.class);
                    if (teacher != null) {
                        teachersList.add(teacher);
                    }
                }

                adapter = new StudentFindTeacherAdapter(StudentFindTeacher.this, teachersList);
                recyclerView.setAdapter(adapter);
                recyclerView.showShimmerAdapter(); // Show shimmer effect while loading
                recyclerView.hideShimmerAdapter(); // Hide shimmer effect after data is loaded
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database errors
                System.out.println("Failed to load teachers: " + error.getMessage());
            }
        });
    }
}