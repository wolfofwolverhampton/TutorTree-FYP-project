package com.javainternal.Teachers;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.MCQ.Model.MCQQuestion;
import com.javainternal.MCQ.Model.QuestionSetModel;
import com.javainternal.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CreateQuestionSetActivity extends AppCompatActivity {

    private Spinner subjectSpinner;
    private LinearLayout questionContainer;
    private Button saveButton;
    private EditText title, description;

    private final List<MCQQuestion> allQuestions = new ArrayList<>();
    private final Map<String, CheckBox> selectedCheckboxes = new HashMap<>();
    private DatabaseReference mcqRef, questionSetRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_question_set);

        subjectSpinner = findViewById(R.id.subjectSpinner);
        questionContainer = findViewById(R.id.questionContainer);
        saveButton = findViewById(R.id.saveQuestionSetBtn);

        title = findViewById(R.id.questionSetTitle);
        description = findViewById(R.id.questionSetDescription);

        mcqRef = FirebaseDatabase.getInstance().getReference("mcqQuestions");
        questionSetRef = FirebaseDatabase.getInstance().getReference("questionSets");

        setupSpinners();
        loadMCQsFromFirebase();

        saveButton.setOnClickListener(v -> saveSelectedQuestions());
    }

    private void setupSpinners() {
        subjectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterQuestions();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadMCQsFromFirebase() {
        mcqRef.addListenerForSingleValueEvent(new ValueEventListener() {
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allQuestions.clear();
                Set<String> subjects = new HashSet<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    MCQQuestion question = snap.getValue(MCQQuestion.class);
                    if (question != null && !question.isAssigned()) {
                        question.setId(snap.getKey());
                        if (!question.isAssigned()) {
                            allQuestions.add(question);
                            if (question.getSubject() != null)
                                subjects.add(question.getSubject());
                        }
                    }
                }

                ArrayList<String> subjectList = new ArrayList<>(subjects);
                subjectList.add("All");

                ArrayAdapter<String> subjectAdapter = new ArrayAdapter<>(CreateQuestionSetActivity.this, android.R.layout.simple_spinner_item, subjectList);
                subjectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                subjectSpinner.setAdapter(subjectAdapter);
                subjectSpinner.setSelection(0);

                filterQuestions();
            }

            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CreateQuestionSetActivity.this, "Failed to load MCQs", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterQuestions() {
        String selectedSubject = (String) subjectSpinner.getSelectedItem();
        questionContainer.removeAllViews();
        selectedCheckboxes.clear();

        for (MCQQuestion question : allQuestions) {
            if ("All".equals(selectedSubject) || question.getSubject().equals(selectedSubject)) {
                CheckBox checkBox = new CheckBox(this);
                checkBox.setText(question.getQuestion());
                questionContainer.addView(checkBox);
                selectedCheckboxes.put(question.getId(), checkBox);
            }
        }
    }

    private void saveSelectedQuestions() {
        List<String> selectedQuestionIds = new ArrayList<>();
        for (Map.Entry<String, CheckBox> entry : selectedCheckboxes.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedQuestionIds.add(entry.getKey());
            }
        }

        if (selectedQuestionIds.isEmpty()) {
            Toast.makeText(this, "No questions selected", Toast.LENGTH_SHORT).show();
            return;
        }

        String setId = questionSetRef.push().getKey();
        QuestionSetModel set = new QuestionSetModel(setId, title.getText().toString(), description.getText().toString(), selectedQuestionIds);

        questionSetRef.child(setId).setValue(set).addOnSuccessListener(aVoid -> {
            for (String qid : selectedQuestionIds) {
                mcqRef.child(qid).child("isAssigned").setValue(true);
            }
            Toast.makeText(this, "Question Set Saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}

