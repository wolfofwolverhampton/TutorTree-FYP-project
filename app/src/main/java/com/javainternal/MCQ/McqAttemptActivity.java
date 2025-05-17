package com.javainternal.MCQ;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.*;
import com.javainternal.MCQ.Model.MCQQuestion;
import com.javainternal.MCQ.Model.QuestionSetModel;
import com.javainternal.R;

import java.util.*;

public class McqAttemptActivity extends AppCompatActivity {

    private QuestionSetModel questionSet;
    private String studentUid;
    private final List<MCQQuestion> questions = new ArrayList<>();
    private int currentIndex = 0;
    private final Map<String, Integer> selectedAnswers = new HashMap<>();

    private TextView questionText;
    private RadioGroup optionsGroup;
    private RadioButton option1, option2, option3, option4;
    private Button nextButton, submitButton;

    private DatabaseReference questionRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcq_attempt);

        questionText = findViewById(R.id.questionText);
        optionsGroup = findViewById(R.id.optionsGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        nextButton = findViewById(R.id.nextButton);
        submitButton = findViewById(R.id.submitButton);

        questionRef = FirebaseDatabase.getInstance().getReference("mcqQuestions");

        questionSet = (QuestionSetModel) getIntent().getSerializableExtra("set");
        studentUid = getIntent().getStringExtra("studentUid");

        if (questionSet == null || studentUid == null) {
            Toast.makeText(this, "Invalid data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        List<String> questionIds = questionSet.getQuestionIds();
        if (questionIds == null || questionIds.isEmpty()) {
            Toast.makeText(this, "No questions in set", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkIfAlreadySubmitted();
    }

    private void checkIfAlreadySubmitted() {
        DatabaseReference submissionRef = FirebaseDatabase.getInstance()
                .getReference("submissions")
                .child(questionSet.getId())
                .child(studentUid);

        submissionRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(McqAttemptActivity.this, "You have already submitted this set", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    fetchQuestions(questionSet.getQuestionIds());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(McqAttemptActivity.this, "Error checking submission", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchQuestions(List<String> questionIds) {
        questions.clear();
        List<com.google.android.gms.tasks.Task<DataSnapshot>> tasks = new ArrayList<>();

        for (String qid : questionIds) {
            com.google.android.gms.tasks.Task<DataSnapshot> task = questionRef.child(qid).get();
            tasks.add(task);
        }

        Tasks.whenAllSuccess(tasks).addOnSuccessListener(results -> {
            for (Object snap : results) {
                DataSnapshot ds = (DataSnapshot) snap;
                MCQQuestion q = ds.getValue(MCQQuestion.class);
                if (q != null) {
                    q.setId(ds.getKey());
                    questions.add(q);
                }
            }

            if (!questions.isEmpty()) {
                Collections.shuffle(questions); // 🔀 Shuffle
                loadQuestion();
            } else {
                Toast.makeText(this, "No questions found", Toast.LENGTH_SHORT).show();
                finish();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load questions", Toast.LENGTH_SHORT).show();
            finish();
        });

        nextButton.setOnClickListener(v -> {
            saveSelectedOption();
            if (currentIndex < questions.size() - 1) {
                currentIndex++;
                loadQuestion();
            }
        });

        submitButton.setOnClickListener(v -> {
            saveSelectedOption();
            submitAnswers();
        });
    }

    private void loadQuestion() {
        optionsGroup.clearCheck();

        MCQQuestion q = questions.get(currentIndex);
        questionText.setText(q.getQuestion());
        option1.setText(q.getOption1());
        option2.setText(q.getOption2());
        option3.setText(q.getOption3());
        option4.setText(q.getOption4());

        if (selectedAnswers.containsKey(q.getId())) {
            int selectedIdx = selectedAnswers.get(q.getId());
            ((RadioButton) optionsGroup.getChildAt(selectedIdx)).setChecked(true);
        }

        nextButton.setVisibility(currentIndex < questions.size() - 1 ? View.VISIBLE : View.GONE);
        submitButton.setVisibility(currentIndex == questions.size() - 1 ? View.VISIBLE : View.GONE);
    }

    private void saveSelectedOption() {
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId == -1) return;

        int index = optionsGroup.indexOfChild(findViewById(selectedId));
        selectedAnswers.put(questions.get(currentIndex).getId(), index);
    }

    private void submitAnswers() {
        int totalQuestions = questions.size();
        int correctCount = 0;

        Map<String, Object> answersMap = new HashMap<>();

        for (MCQQuestion q : questions) {
            int selectedIdx = selectedAnswers.getOrDefault(q.getId(), -1);

            String selectedOptionValue = null;
            switch (selectedIdx) {
                case 0: selectedOptionValue = q.getOption1(); break;
                case 1: selectedOptionValue = q.getOption2(); break;
                case 2: selectedOptionValue = q.getOption3(); break;
                case 3: selectedOptionValue = q.getOption4(); break;
            }

            boolean isCorrect = selectedOptionValue != null && selectedOptionValue.equals(q.getCorrectAnswer());

            if (isCorrect) correctCount++;

            Map<String, Object> answerDetails = new HashMap<>();
            answerDetails.put("selectedOptionIndex", selectedIdx);
            answerDetails.put("selectedOptionValue", selectedOptionValue);
            answerDetails.put("isCorrect", isCorrect);

            answersMap.put(q.getId(), answerDetails);
        }

        double scorePercent = ((double) correctCount / totalQuestions) * 100;

        Map<String, Object> submission = new HashMap<>();
        submission.put("score", scorePercent);
        submission.put("submittedAt", System.currentTimeMillis());
        submission.put("answers", answersMap);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("submissions")
                .child(questionSet.getId())
                .child(studentUid);

        ref.setValue(submission).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Submission successful. Score: " + String.format("%.2f", scorePercent) + "%", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to submit", Toast.LENGTH_SHORT).show();
            }
        });
    }
}