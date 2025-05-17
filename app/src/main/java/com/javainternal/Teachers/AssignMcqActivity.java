package com.javainternal.Teachers;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.MCQ.Model.QuestionSetModel;
import com.javainternal.R;
import com.javainternal.Utils.FirebaseUtils;
import com.javainternal.Utils.NotificationUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssignMcqActivity extends AppCompatActivity {

    private TextView studentNameTextView, alreadyAssignedMessageTextView;
    private LinearLayout checkboxContainer;
    private Button assignButton;

    private final Map<String, CheckBox> selectedCheckboxes = new HashMap<>();
    private DatabaseReference questionSetRef, studentsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assign_mcq);

        studentNameTextView = findViewById(R.id.studentNameTextView);
        alreadyAssignedMessageTextView = findViewById(R.id.alreadyAssignedMessageTextView);

        checkboxContainer = findViewById(R.id.checkboxContainer);
        assignButton = findViewById(R.id.assignButton);

        String studentUid = getIntent().getStringExtra("studentUid");
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        if (studentUid != null) {
            studentsRef.child(studentUid).child("name").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            String studentName = snapshot.getValue(String.class);
                            if (studentName != null) {
                                studentNameTextView.setText("Assign MCQ to: " + studentName);
                            } else {
                                studentNameTextView.setText("Assign MCQ to: [Unknown Student]");
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(AssignMcqActivity.this, "Failed to load student", Toast.LENGTH_SHORT).show();
                            studentNameTextView.setText("Assign MCQ to: [Error]");
                        }
                    }
            );
        } else {
            Toast.makeText(this, "No student selected", Toast.LENGTH_SHORT).show();
            studentNameTextView.setText("Assign MCQ to: [No UID]");
        }

        questionSetRef = FirebaseDatabase.getInstance().getReference("questionSets");
        loadQuestionSets();

        assignButton.setOnClickListener(v -> {
            List<String> selectedSetIds = new ArrayList<>();

            for (Map.Entry<String, CheckBox> entry : selectedCheckboxes.entrySet()) {
                if (entry.getValue().isChecked()) {
                    selectedSetIds.add(entry.getKey());
                }
            }

            if (selectedSetIds.isEmpty()) {
                Toast.makeText(this, "Please select at least one set", Toast.LENGTH_SHORT).show();
                return;
            }

            if (studentUid == null) {
                Toast.makeText(this, "No student selected", Toast.LENGTH_SHORT).show();
                return;
            }

            DatabaseReference assignmentRef = FirebaseDatabase.getInstance()
                    .getReference("studentAssignments")
                    .child(studentUid);

            Map<String, Object> data = new HashMap<>();
            for (String setId : selectedSetIds) {
                data.put(setId, true);
            }

            assignmentRef.updateChildren(data).addOnSuccessListener(unused -> {
                Toast.makeText(this, "Assigned successfully", Toast.LENGTH_SHORT).show();
                NotificationUtils.sendNotification(AssignMcqActivity.this, UserAuthContext.getInstance(AssignMcqActivity.this).getLoggedInPhone(), studentUid, "MCQ Questions has been assigned to you.");

                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to assign: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void loadQuestionSets() {
        String studentUid = getIntent().getStringExtra("studentUid");
        if (studentUid == null) {
            Toast.makeText(this, "Student UID is missing", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference assignmentRef = FirebaseDatabase.getInstance()
                .getReference("studentAssignments")
                .child(studentUid);

        assignmentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot assignedSnapshot) {
                List<String> assignedSetIds = new ArrayList<>();
                for (DataSnapshot snap : assignedSnapshot.getChildren()) {
                    assignedSetIds.add(snap.getKey());
                }

                questionSetRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        checkboxContainer.removeAllViews();
                        selectedCheckboxes.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            QuestionSetModel set = snap.getValue(QuestionSetModel.class);
                            String setId = snap.getKey();

                            if (set != null && setId != null && !assignedSetIds.contains(setId)) {
                                set.setId(setId);
                                CheckBox cb = new CheckBox(AssignMcqActivity.this);
                                cb.setText(set.getTitle());
                                checkboxContainer.addView(cb);
                                selectedCheckboxes.put(setId, cb);
                            }
                        }

                        if (selectedCheckboxes.isEmpty()) {
                            alreadyAssignedMessageTextView.setVisibility(View.VISIBLE);
                            assignButton.setVisibility(View.GONE);
                        } else {
                            alreadyAssignedMessageTextView.setVisibility(View.GONE);
                            assignButton.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AssignMcqActivity.this, "Failed to load sets", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignMcqActivity.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
