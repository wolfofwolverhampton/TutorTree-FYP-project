package com.javainternal.MCQ.Adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javainternal.MCQ.Model.MCQQuestion;
import com.javainternal.R;

import java.util.ArrayList;
import java.util.List;

public class CreateMCQAdapter extends RecyclerView.Adapter<CreateMCQAdapter.ViewHolder> {

    private List<MCQQuestion> mcqQuestionList;

    public CreateMCQAdapter() {
        this.mcqQuestionList = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_create_mcq, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MCQQuestion question = mcqQuestionList.get(position);

        holder.tvQuestionNumber.setText("Question " + (position + 1));
        holder.questionSubject.setText(question.getSubject());
        holder.etQuestion.setText(question.getQuestion());
        holder.etOption1.setText(question.getOption1());
        holder.etOption2.setText(question.getOption2());
        holder.etOption3.setText(question.getOption3());
        holder.etOption4.setText(question.getOption4());
        holder.etCorrectAnswer.setText(question.getCorrectAnswer());

        // Add TextWatchers to update the question data dynamically
        holder.questionSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                question.setSubject(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        holder.etQuestion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                question.setQuestion(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.etOption1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                question.setOption1(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.etOption2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                question.setOption2(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.etOption3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                question.setOption3(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.etOption4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                question.setOption4(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        holder.etCorrectAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                question.setCorrectAnswer(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Delete button click listener
        holder.btnDelete.setOnClickListener(v -> {
            mcqQuestionList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mcqQuestionList.size());
        });
    }

    @Override
    public int getItemCount() {
        return mcqQuestionList.size();
    }

    public void addNewQuestion() {
        mcqQuestionList.add(new MCQQuestion("", "", "", "", "", "", ""));
        notifyItemInserted(mcqQuestionList.size() - 1);
    }

    public List<MCQQuestion> getQuestions() {
        // Return the updated list of questions
        return mcqQuestionList;
    }

    public void clearQuestions() {
        mcqQuestionList.clear();
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvQuestionNumber;
        EditText etQuestion, etOption1, etOption2, etOption3, etOption4, etCorrectAnswer, questionSubject;
        Button btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQuestionNumber = itemView.findViewById(R.id.tvQuestionNumber);
            questionSubject = itemView.findViewById(R.id.questionSubject);
            etQuestion = itemView.findViewById(R.id.etQuestion);
            etOption1 = itemView.findViewById(R.id.etOption1);
            etOption2 = itemView.findViewById(R.id.etOption2);
            etOption3 = itemView.findViewById(R.id.etOption3);
            etOption4 = itemView.findViewById(R.id.etOption4);
            etCorrectAnswer = itemView.findViewById(R.id.etCorrectAnswer);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}