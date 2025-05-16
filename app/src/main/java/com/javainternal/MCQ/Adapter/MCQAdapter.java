package com.javainternal.MCQ.Adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javainternal.MCQ.Model.MCQQuestion;
import com.javainternal.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MCQAdapter extends RecyclerView.Adapter<MCQAdapter.ViewHolder> {

    private List<MCQQuestion> mcqQuestionList;
    private Map<Integer, String> userAnswers = new HashMap<>();
    private boolean isSubmitted = false; // Tracks whether the user has submitted answers

    public MCQAdapter(List<MCQQuestion> mcqQuestionList) {
        this.mcqQuestionList = mcqQuestionList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_mcq, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MCQQuestion question = mcqQuestionList.get(position);
        holder.tvQuestion.setText(question.getQuestion());
        holder.rbOption1.setText(question.getOption1());
        holder.rbOption2.setText(question.getOption2());
        holder.rbOption3.setText(question.getOption3());
        holder.rbOption4.setText(question.getOption4());

        // Reset colors when binding
        resetOptionColors(holder);

        // Disable RadioGroup if the user has already answered or if answers are submitted
        if (userAnswers.containsKey(position) || isSubmitted) {
            holder.radioGroup.setEnabled(false);
        } else {
            holder.radioGroup.setEnabled(true);
        }

        // Highlight correct and incorrect answers if submitted
        if (isSubmitted) {
            highlightAnswers(holder, position, question);
        }

        // Handle user selection
        holder.radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton selectedRadioButton = group.findViewById(checkedId);
            String selectedAnswer = selectedRadioButton.getText().toString();

            // Save the user's answer
            userAnswers.put(position, selectedAnswer);

            // Disable further changes
            holder.radioGroup.setEnabled(false);
        });
    }

    private void resetOptionColors(ViewHolder holder) {
        holder.rbOption1.setTextColor(Color.BLACK);
        holder.rbOption2.setTextColor(Color.BLACK);
        holder.rbOption3.setTextColor(Color.BLACK);
        holder.rbOption4.setTextColor(Color.BLACK);
    }

    private void highlightAnswers(ViewHolder holder, int position, MCQQuestion question) {
        String correctAnswer = question.getCorrectAnswer();
        String userAnswer = userAnswers.get(position);

        // Highlight correct answer in green
        if (holder.rbOption1.getText().toString().equals(correctAnswer)) {
            holder.rbOption1.setTextColor(Color.GREEN);
        }
        if (holder.rbOption2.getText().toString().equals(correctAnswer)) {
            holder.rbOption2.setTextColor(Color.GREEN);
        }
        if (holder.rbOption3.getText().toString().equals(correctAnswer)) {
            holder.rbOption3.setTextColor(Color.GREEN);
        }
        if (holder.rbOption4.getText().toString().equals(correctAnswer)) {
            holder.rbOption4.setTextColor(Color.GREEN);
        }

        // Highlight user's incorrect answer in red
        if (userAnswer != null && !userAnswer.equals(correctAnswer)) {
            if (holder.rbOption1.getText().toString().equals(userAnswer)) {
                holder.rbOption1.setTextColor(Color.RED);
            }
            if (holder.rbOption2.getText().toString().equals(userAnswer)) {
                holder.rbOption2.setTextColor(Color.RED);
            }
            if (holder.rbOption3.getText().toString().equals(userAnswer)) {
                holder.rbOption3.setTextColor(Color.RED);
            }
            if (holder.rbOption4.getText().toString().equals(userAnswer)) {
                holder.rbOption4.setTextColor(Color.RED);
            }
        }
    }

    public void submitAnswers() {
        isSubmitted = true;
        notifyDataSetChanged(); // Refresh the RecyclerView to apply changes
    }

    @Override
    public int getItemCount() {
        return mcqQuestionList.size();
    }

    public int calculateScore() {
        int score = 0;
        for (int i = 0; i < mcqQuestionList.size(); i++) {
            if (userAnswers.containsKey(i)) {
                String userAnswer = userAnswers.get(i);
                String correctAnswer = mcqQuestionList.get(i).getCorrectAnswer();
                if (userAnswer.equals(correctAnswer)) {
                    score++;
                }
            }
        }
        return score;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestion;
        RadioGroup radioGroup;
        RadioButton rbOption1, rbOption2, rbOption3, rbOption4;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestion = itemView.findViewById(R.id.tvQuestion);
            radioGroup = itemView.findViewById(R.id.radioGroup);
            rbOption1 = itemView.findViewById(R.id.rbOption1);
            rbOption2 = itemView.findViewById(R.id.rbOption2);
            rbOption3 = itemView.findViewById(R.id.rbOption3);
            rbOption4 = itemView.findViewById(R.id.rbOption4);
        }
    }
}