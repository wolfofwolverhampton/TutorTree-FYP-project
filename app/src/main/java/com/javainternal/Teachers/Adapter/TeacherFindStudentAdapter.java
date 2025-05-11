package com.javainternal.Teachers.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javainternal.R;
import com.javainternal.Students.Model.StudentUserModel;
import com.javainternal.Teachers.TeacherViewStudentProfile;
import com.javainternal.databinding.RowConversationBinding;

import java.util.ArrayList;

public class TeacherFindStudentAdapter extends RecyclerView.Adapter<TeacherFindStudentAdapter.StudentViewHolder> {

    private Context context;
    private ArrayList<StudentUserModel> students;

    public TeacherFindStudentAdapter(Context context, ArrayList<StudentUserModel> students) {
        this.context = context;
        this.students = students;
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new StudentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        StudentUserModel student = students.get(position);

        // Bind the student's name and category to the TextViews
        holder.binding.username.setText(student.getName());
        holder.binding.lastMsg.setText(student.getCategory());

        // Set click listener for the item view
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the TeacherChatActivity or another relevant activity
                Intent intent = new Intent(context, TeacherViewStudentProfile.class); // Replace with your target activity
                intent.putExtra("name", student.getName());
                intent.putExtra("uid", student.getUid()); // Pass the UID (phone number)
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return students.size();
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public StudentViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }
}