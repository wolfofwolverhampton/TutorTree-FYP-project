package com.javainternal.MCQ;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.javainternal.MCQ.Adapter.ResultQuestionAdapter;
import com.javainternal.MCQ.Model.MCQQuestion;
import com.javainternal.MCQ.Model.QuestionSetModel;
import com.javainternal.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class McqResultActivity extends AppCompatActivity {

    private RecyclerView questionsRecyclerView;
    private ResultQuestionAdapter adapter;
    private List<MCQQuestion> questions;
    private Map<String, SubmissionAnswer> userAnswers;

    private double score;
    private String studentUid;
    private String setId;
    private TextView tvScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcq_result);

        questionsRecyclerView = findViewById(R.id.questionsRecyclerView);
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        studentUid = getIntent().getStringExtra("studentUid");
        setId = getIntent().getStringExtra("setId");

        loadQuestionsAndSubmission();
    }

    private void loadQuestionsAndSubmission() {
        DatabaseReference setRef = FirebaseDatabase.getInstance().getReference("questionSets").child(setId);
        DatabaseReference submissionRef = FirebaseDatabase.getInstance().getReference("submissions").child(setId).child(studentUid);
        DatabaseReference questionRefRoot = FirebaseDatabase.getInstance().getReference("mcqQuestions");

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        setRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                QuestionSetModel questionSet = snapshot.getValue(QuestionSetModel.class);
                if (questionSet == null || questionSet.getQuestionIds() == null) {
                    Toast.makeText(McqResultActivity.this, "No questions found in this set", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                    return;
                }

                List<String> questionIds = questionSet.getQuestionIds();
                questions = new ArrayList<>();

                for (String questionId : questionIds) {
                    questionRefRoot.child(questionId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot questionSnap) {
                            MCQQuestion question = questionSnap.getValue(MCQQuestion.class);
                            if (question != null) {
                                question.setId(questionSnap.getKey());
                                questions.add(question);
                            }

                            if (questions.size() == questionIds.size()) {
                                loadSubmissionAndShowResults(submissionRef);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Toast.makeText(McqResultActivity.this, "Failed to load question", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(McqResultActivity.this, "Failed to load question set", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadSubmissionAndShowResults(DatabaseReference submissionRef) {
        submissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot submissionSnapshot) {
                if (submissionSnapshot.exists()) {
                    score = submissionSnapshot.child("score").getValue(Double.class);

                    userAnswers = new HashMap<>();
                    DataSnapshot answersSnapshot = submissionSnapshot.child("answers");
                    for (DataSnapshot ansSnap : answersSnapshot.getChildren()) {
                        String qId = ansSnap.getKey();
                        Integer selectedOptionIndex = ansSnap.child("selectedOptionIndex").getValue(Integer.class);
                        Boolean isCorrect = ansSnap.child("isCorrect").getValue(Boolean.class);
                        String selectedOptionValue = ansSnap.child("selectedOptionValue").getValue(String.class);

                        userAnswers.put(qId, new SubmissionAnswer(selectedOptionIndex, selectedOptionValue, isCorrect));
                    }

                    adapter = new ResultQuestionAdapter(questions, userAnswers);
                    questionsRecyclerView.setAdapter(adapter);
                    questionsRecyclerView.setLayoutManager(new LinearLayoutManager(McqResultActivity.this));
                    adapter.notifyDataSetChanged();

                    ProgressBar progressBar = findViewById(R.id.progressBar);
                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(McqResultActivity.this, "Score: " + String.format("%.2f", score) + "%", Toast.LENGTH_LONG).show();
                    tvScore = findViewById(R.id.tvScore);
                    tvScore.setText("Score: " + String.format("%.2f", score) + "%");

                } else {
                    Toast.makeText(McqResultActivity.this, "No submission found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(McqResultActivity.this, "Failed to load submission", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    public static class SubmissionAnswer {
        public Integer selectedOptionIndex;
        public String selectedOptionValue;
        public Boolean isCorrect;

        public SubmissionAnswer(Integer idx, String val, Boolean correct) {
            this.selectedOptionIndex = idx;
            this.selectedOptionValue = val;
            this.isCorrect = correct;
        }
    }
}
