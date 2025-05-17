package com.javainternal.Students;

import android.os.Bundle;
import android.widget.Toast;

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
import com.javainternal.Assignments.Adapter.AssignmentAdapter;
import com.javainternal.Assignments.Model.Task;
import com.javainternal.R;

import java.util.ArrayList;
import java.util.List;

public class StudentAssignmentsActivity extends AppCompatActivity {

    private RecyclerView assignmentsRecyclerView;
    private AssignmentAdapter adapter;
    private List<Task> assignmentList;
    private final String studentUid = UserAuthContext.getInstance(this).getLoggedInPhone();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_assignments);

        assignmentsRecyclerView = findViewById(R.id.assignmentsRecyclerView);
        assignmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        assignmentList = new ArrayList<>();
        adapter = new AssignmentAdapter(getApplicationContext(), assignmentList, studentUid);
        assignmentsRecyclerView.setAdapter(adapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("student_tasks").child(studentUid);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assignmentList.clear();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Task assignment = child.getValue(Task.class);
                    assignmentList.add(assignment);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentAssignmentsActivity.this, "Failed to load assignments.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
