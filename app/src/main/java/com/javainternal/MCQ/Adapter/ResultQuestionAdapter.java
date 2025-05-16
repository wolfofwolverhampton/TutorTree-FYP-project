package com.javainternal.MCQ.Adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.javainternal.MCQ.McqResultActivity;
import com.javainternal.MCQ.Model.MCQQuestion;
import com.javainternal.R;

import java.util.List;
import java.util.Map;

public class ResultQuestionAdapter extends RecyclerView.Adapter<ResultQuestionAdapter.ResultQuestionViewHolder> {

    private final List<MCQQuestion> questions;
    private final Map<String, McqResultActivity.SubmissionAnswer> userAnswers;

    public ResultQuestionAdapter(List<MCQQuestion> questions, Map<String, McqResultActivity.SubmissionAnswer> userAnswers) {
        this.questions = questions;
        this.userAnswers = userAnswers;
    }

    @NonNull
    @Override
    public ResultQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mcq_question_result, parent, false);
        return new ResultQuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultQuestionViewHolder holder, int position) {
        MCQQuestion question = questions.get(position);
        if (question != null) {
            McqResultActivity.SubmissionAnswer answer = userAnswers.get(question.getId());
            holder.bind(question, answer);
        }
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    class ResultQuestionViewHolder extends RecyclerView.ViewHolder {

        TextView questionTextView;
        RadioButton option1, option2, option3, option4;
        RadioGroup optionsGroup;

        public ResultQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.questionTextView);
            option1 = itemView.findViewById(R.id.option1RadioButton);
            option2 = itemView.findViewById(R.id.option2RadioButton);
            option3 = itemView.findViewById(R.id.option3RadioButton);
            option4 = itemView.findViewById(R.id.option4RadioButton);
            optionsGroup = itemView.findViewById(R.id.optionsRadioGroup);
        }

        public void bind(MCQQuestion question, McqResultActivity.SubmissionAnswer answer) {
            questionTextView.setText(question.getQuestion());

            resetOption(option1, question.getOption1());
            resetOption(option2, question.getOption2());
            resetOption(option3, question.getOption3());
            resetOption(option4, question.getOption4());

            option1.setClickable(false);
            option2.setClickable(false);
            option3.setClickable(false);
            option4.setClickable(false);
            optionsGroup.clearCheck();

            if (answer != null && answer.selectedOptionIndex != null) {
                switch (answer.selectedOptionIndex) {
                    case 0: option1.setChecked(true); break;
                    case 1: option2.setChecked(true); break;
                    case 2: option3.setChecked(true); break;
                    case 3: option4.setChecked(true); break;
                }
            }

            String correctAnswer = question.getCorrectAnswer();
            highlightOption(option1, question.getOption1(), correctAnswer, answer);
            highlightOption(option2, question.getOption2(), correctAnswer, answer);
            highlightOption(option3, question.getOption3(), correctAnswer, answer);
            highlightOption(option4, question.getOption4(), correctAnswer, answer);
        }

        private void resetOption(RadioButton optionButton, String optionText) {
            optionButton.setText(optionText != null ? optionText : "");
            optionButton.setTextColor(ContextCompat.getColor(optionButton.getContext(), android.R.color.black));
            optionButton.setChecked(false);
        }

        private void highlightOption(RadioButton optionButton, String optionText, String correctAnswerText, McqResultActivity.SubmissionAnswer answer) {
            if (optionText == null) return;

            optionButton.setText(optionText); // Reset to base text

            String tag = "";

            if (TextUtils.equals(optionText, correctAnswerText)) {
                tag = " ✔ (Correct)";
                optionButton.setTextColor(ContextCompat.getColor(optionButton.getContext(), android.R.color.holo_green_dark));
            } else if (answer != null && TextUtils.equals(optionText, answer.selectedOptionValue) && !answer.isCorrect) {
                tag = " ✘ (Your Answer)";
                optionButton.setTextColor(ContextCompat.getColor(optionButton.getContext(), android.R.color.holo_red_dark));
            }

            optionButton.setText(optionText + tag);
        }
    }
}