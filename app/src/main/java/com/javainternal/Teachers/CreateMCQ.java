package com.javainternal.Teachers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
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
import com.javainternal.MCQ.Adapter.CreateMCQAdapter;
import com.javainternal.MCQ.Model.MCQQuestion;
import com.javainternal.MainActivity;
import com.javainternal.R;

import java.util.List;

public class CreateMCQ extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CreateMCQAdapter adapter;
    private Button btnSave, btnTakeQuiz, btnDeleteAll;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mcq);

        databaseReference = FirebaseDatabase.getInstance().getReference("mcqQuestions");

        recyclerView = findViewById(R.id.recyclerView);
        btnSave = findViewById(R.id.btnSave);
        btnTakeQuiz = findViewById(R.id.btnTakeQuiz);
        btnDeleteAll = findViewById(R.id.btnDeleteAll);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CreateMCQAdapter();
        recyclerView.setAdapter(adapter);

        loadQuestionsFromFirebase();

        Button btnAddQuestion = findViewById(R.id.btnAddQuestion);
        btnAddQuestion.setOnClickListener(v -> adapter.addNewQuestion());

        btnSave.setOnClickListener(v -> saveQuestions());

        btnTakeQuiz.setOnClickListener(v -> startActivity(new Intent(CreateMCQ.this, MainActivity.class)));

        btnDeleteAll.setOnClickListener(v -> deleteAllQuestions());
    }

    private void loadQuestionsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adapter.clearQuestions(); // Clear existing questions
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MCQQuestion question = snapshot.getValue(MCQQuestion.class);
                    if (question != null) {
                        // Set the ID of the question
                        question.setId(snapshot.getKey());
                        adapter.getQuestions().add(question);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(CreateMCQ.this, "Error loading questions: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveQuestions() {
        List<MCQQuestion> questions = adapter.getQuestions();

        if (questions.isEmpty()) {
            Toast.makeText(this, "No questions to save", Toast.LENGTH_SHORT).show();
            return;
        }

        for (MCQQuestion question : questions) {
            if (question.getId() == null || question.getId().isEmpty()) {
                String newId = databaseReference.push().getKey();
                question.setId(newId);
            }

            databaseReference.child(question.getId()).setValue(question);
        }

        Toast.makeText(CreateMCQ.this, "Questions saved successfully!", Toast.LENGTH_SHORT).show();
    }

    private void deleteAllQuestions() {
        databaseReference.removeValue()
                .addOnSuccessListener(aVoid -> {
                    adapter.clearQuestions();
                    Toast.makeText(CreateMCQ.this, "All questions deleted successfully!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateMCQ.this, "Failed to delete questions: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}