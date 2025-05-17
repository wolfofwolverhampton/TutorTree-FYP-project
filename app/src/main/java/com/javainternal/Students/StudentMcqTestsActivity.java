package com.javainternal.Students;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.MCQ.Adapter.QuestionSetAdapter;
import com.javainternal.MCQ.McqAttemptActivity;
import com.javainternal.MCQ.McqResultActivity;
import com.javainternal.MCQ.Model.QuestionSetModel;
import com.javainternal.R;

import java.util.ArrayList;
import java.util.List;

public class StudentMcqTestsActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> attemptLauncher;
    private RecyclerView assignmentsRecyclerView;
    private QuestionSetAdapter adapter;
    private final List<QuestionSetModel> assignedSets = new ArrayList<>();

    private DatabaseReference assignmentsRef, questionSetsRef;
    private String studentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcq_tests);

        assignmentsRecyclerView = findViewById(R.id.assignmentsRecyclerView);
        assignmentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentUid = GlobalStudentUid.getInstance().getStudentUid();

        assignmentsRef = FirebaseDatabase.getInstance().getReference("studentAssignments").child(studentUid);
        questionSetsRef = FirebaseDatabase.getInstance().getReference("questionSets");

        loadAssignedQuestionSets();

        attemptLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadAssignedQuestionSets();
                    }
                }
        );

        adapter = new QuestionSetAdapter(assignedSets, StudentMcqTestsActivity.this, studentUid);
        adapter.setAttemptLauncher(attemptLauncher);

        adapter.setOnAttemptRequestedListener((set, submitted) -> {
            Intent intent;
            if (submitted) {
                intent = new Intent(this, McqResultActivity.class);
                intent.putExtra("setId", set.getId());
                intent.putExtra("studentUid", studentUid);
                startActivity(intent);
            } else {
                intent = new Intent(this, McqAttemptActivity.class);
                intent.putExtra("set", set);
                intent.putExtra("studentUid", studentUid);
                attemptLauncher.launch(intent);
            }
        });

        assignmentsRecyclerView.setAdapter(adapter);
    }

    private void loadAssignedQuestionSets() {
        assignmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot assignmentSnapshot) {
                if (!assignmentSnapshot.exists()) return;

                assignedSets.clear();

                for (DataSnapshot child : assignmentSnapshot.getChildren()) {
                    String setId = child.getKey();

                    questionSetsRef.child(setId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot setSnapshot) {
                            QuestionSetModel set = setSnapshot.getValue(QuestionSetModel.class);
                            if (set != null) {
                                set.setId(setSnapshot.getKey());
                                assignedSets.add(set);
                                adapter.notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(StudentMcqTestsActivity.this, "Failed to fetch set: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentMcqTestsActivity.this, "Failed to fetch assignments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
