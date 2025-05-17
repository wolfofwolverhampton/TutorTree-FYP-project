package com.javainternal.Assignments.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javainternal.Assignments.Model.Task;
import com.javainternal.R;

import java.util.List;

public class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {

    private Context context;
    private List<Task> taskList;
    private String studentUid;

    public AssignmentAdapter(Context context, List<Task> taskList, String studentUid) {
        this.context = context;
        this.taskList = taskList;
        this.studentUid = studentUid;
    }

    @NonNull
    @Override
    public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
        return new AssignmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.name.setText(task.getName());
        holder.date.setText("Due Date: " + task.getDueDate());

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                context, R.array.status_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(adapter);

        int spinnerPosition = adapter.getPosition(task.getStatus());
        if (spinnerPosition >= 0) {
            holder.statusSpinner.setSelection(spinnerPosition);
        }

        holder.submitButton.setOnClickListener(v -> {
            String selectedStatus = holder.statusSpinner.getSelectedItem().toString();

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("student_tasks")
                    .child(studentUid)
                    .child(task.getId());

            ref.child("status").setValue(selectedStatus).addOnSuccessListener(unused ->
                    Toast.makeText(context, "Status updated!", Toast.LENGTH_SHORT).show()
            ).addOnFailureListener(e ->
                    Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
            );
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class AssignmentViewHolder extends RecyclerView.ViewHolder {
        TextView name, date;
        Spinner statusSpinner;
        Button submitButton;

        public AssignmentViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.assignmentName);
            date = itemView.findViewById(R.id.assignmentDate);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
            submitButton = itemView.findViewById(R.id.submitStatusButton);
        }
    }
}
