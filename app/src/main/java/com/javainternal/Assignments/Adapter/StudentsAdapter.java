package com.javainternal.Assignments.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javainternal.R;
import com.javainternal.Students.Model.StudentUserModel;

import java.util.List;

public class StudentsAdapter extends RecyclerView.Adapter<StudentsAdapter.ViewHolder> {

    private final List<StudentUserModel> students;
    private final List<String> selectedUids;

    public StudentsAdapter(List<StudentUserModel> students, List<String> selectedUids) {
        this.students = students;
        this.selectedUids = selectedUids;
    }

    @NonNull
    @Override
    public StudentsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.student_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentsAdapter.ViewHolder holder, int position) {
        StudentUserModel student = students.get(position);
        holder.studentName.setText(student.getName());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedUids.contains(student.getUid()));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && !selectedUids.contains(student.getUid())) {
                selectedUids.add(student.getUid());
            } else {
                selectedUids.remove(student.getUid());
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView studentName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox);
            studentName = itemView.findViewById(R.id.studentName);
        }
    }
}

