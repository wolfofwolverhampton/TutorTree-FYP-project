package com.javainternal.Students.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.javainternal.R;
import com.javainternal.Students.StudentViewTeacherProfile;
import com.javainternal.databinding.RowConversationBinding;
import com.javainternal.Teachers.Model.TeacherUserModel;

import java.util.ArrayList;
import java.util.List;

public class StudentFindTeacherAdapter extends RecyclerView.Adapter<StudentFindTeacherAdapter.TeacherViewHolder> {

    private Context context;
    private ArrayList<TeacherUserModel> teachers;

    public StudentFindTeacherAdapter (){}

    public StudentFindTeacherAdapter(Context context, ArrayList<TeacherUserModel> teachers) {
        this.context = context;
        this.teachers = teachers;
    }

    @NonNull
    @Override
    public TeacherViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_conversation, parent, false);
        return new TeacherViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeacherViewHolder holder, int position) {
        TeacherUserModel teacher = teachers.get(position);

        holder.binding.username.setText(teacher.getName());
        holder.binding.category.setText(teacher.getCategory());

        Glide.with(holder.itemView.getContext())
                .load(context.getString(R.string.backend_url) + teacher.getProfilePicture())
                .placeholder(R.drawable.ic_teacher)
                .error(R.drawable.ic_teacher)
                .circleCrop()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .skipMemoryCache(false)
                .into(holder.binding.profileIcon);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, StudentViewTeacherProfile.class);
                intent.putExtra("name", teacher.getName());
                intent.putExtra("uid", teacher.getUid());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return teachers.size();
    }

    public static class TeacherViewHolder extends RecyclerView.ViewHolder {

        RowConversationBinding binding;

        public TeacherViewHolder(@NonNull View itemView) {
            super(itemView);
            binding = RowConversationBinding.bind(itemView);
        }
    }

    public void updateData(List<TeacherUserModel> newList) {
        this.teachers.clear();
        this.teachers.addAll(newList);
        notifyDataSetChanged();
    }
}