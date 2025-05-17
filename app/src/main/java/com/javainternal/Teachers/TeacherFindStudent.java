package com.javainternal.Teachers;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.R;
import com.javainternal.Students.Model.StudentUserModel;
import com.javainternal.Teachers.Adapter.TeacherFindStudentAdapter;

import java.util.ArrayList;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;

public class TeacherFindStudent extends AppCompatActivity {

    private ShimmerRecyclerView recyclerView;
    private DatabaseReference studentsRef;
    private ArrayList<StudentUserModel> studentsList;
    private TeacherFindStudentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_find_student);

        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        recyclerView = findViewById(R.id.recyclerView);
        studentsList = new ArrayList<>();

        fetchStudentsFromFirebase();
    }

    private void fetchStudentsFromFirebase() {
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentsList.clear(); // Clear the list before adding new data
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    StudentUserModel student = snapshot.getValue(StudentUserModel.class);
                    if (student != null) {
                        studentsList.add(student);
                    }
                }

                // Initialize the adapter and set it to the RecyclerView
                adapter = new TeacherFindStudentAdapter(TeacherFindStudent.this, studentsList);
                recyclerView.setAdapter(adapter);
                recyclerView.showShimmerAdapter(); // Show shimmer effect while loading
                recyclerView.hideShimmerAdapter(); // Hide shimmer effect after data is loaded
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database errors
                System.out.println("Failed to load students: " + error.getMessage());
            }
        });
    }
}