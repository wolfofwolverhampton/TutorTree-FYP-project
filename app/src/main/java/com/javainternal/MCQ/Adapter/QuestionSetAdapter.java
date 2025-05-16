package com.javainternal.MCQ.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.MCQ.McqAttemptActivity;
import com.javainternal.MCQ.McqResultActivity;
import com.javainternal.MCQ.Model.QuestionSetModel;
import com.javainternal.R;

import java.util.List;

public class QuestionSetAdapter extends RecyclerView.Adapter<QuestionSetAdapter.QuestionSetViewHolder> {

    private final List<QuestionSetModel> questionSets;
    private final Context context;
    private final String studentUid;
    private ActivityResultLauncher<Intent> attemptLauncher;

    private OnAttemptRequestedListener listener;

    public interface OnAttemptRequestedListener {
        void onAttemptRequested(QuestionSetModel set, boolean submitted);
    }

    public QuestionSetAdapter(List<QuestionSetModel> questionSets, Context context, String studentUid) {
        this.questionSets = questionSets;
        this.context = context;
        this.studentUid = studentUid;
    }

    public void setOnAttemptRequestedListener(OnAttemptRequestedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionSetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_question_set, parent, false);
        return new QuestionSetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionSetViewHolder holder, int position) {
        QuestionSetModel questionSet = questionSets.get(position);
        holder.setData(questionSet);
    }

    @Override
    public int getItemCount() {
        return questionSets.size();
    }

    public void setAttemptLauncher(ActivityResultLauncher<Intent> launcher) {
        this.attemptLauncher = launcher;
    }

    class QuestionSetViewHolder extends RecyclerView.ViewHolder {
        private final TextView setTitleTextView, submittedStatusTextView, setDescriptionTextView;
        private final Button openButton;

        public QuestionSetViewHolder(@NonNull View itemView) {
            super(itemView);
            setTitleTextView = itemView.findViewById(R.id.setTitleTextView);
            setDescriptionTextView = itemView.findViewById(R.id.setDescriptionTextView);
            submittedStatusTextView = itemView.findViewById(R.id.submittedStatusTextView);
            openButton = itemView.findViewById(R.id.openButton);
        }

        public void setData(QuestionSetModel set) {
            setTitleTextView.setText(set.getTitle());
            setDescriptionTextView.setText(set.getDescription());

            DatabaseReference submissionsRef = FirebaseDatabase.getInstance()
                    .getReference("submissions")
                    .child(set.getId())
                    .child(studentUid);

            submissionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean submitted = snapshot.exists();

                    submittedStatusTextView.setText(submitted ? "Submitted" : "Not Submitted");
                    submittedStatusTextView.setTextColor(
                            context.getResources().getColor(
                                    submitted ? android.R.color.holo_green_dark : android.R.color.holo_red_dark
                            )
                    );

                    openButton.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onAttemptRequested(set, submitted);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    submittedStatusTextView.setText("Error checking status");
                }
            });
        }

    }
}
