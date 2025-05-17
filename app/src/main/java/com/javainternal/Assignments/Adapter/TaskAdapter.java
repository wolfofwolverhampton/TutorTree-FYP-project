package com.javainternal.Assignments.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javainternal.Assignments.Dialogs.AssignAssignmentsDialog;
import com.javainternal.Assignments.Dialogs.EditTaskDialog;
import com.javainternal.Assignments.Model.Task;
import com.javainternal.R;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final List<Task> taskList;

    public TaskAdapter(List<Task> taskList) {
        this.taskList = taskList;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.taskName.setText(task.getName());
        holder.taskDate.setText(task.getDueDate());

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                holder.itemView.getContext(),
                R.array.status_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        holder.statusSpinner.setAdapter(adapter);

        String currentStatus = task.getStatus();
        if (currentStatus != null) {
            int spinnerPosition = adapter.getPosition(currentStatus);
            holder.statusSpinner.setSelection(spinnerPosition);
        }

        holder.statusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int spinnerPosition, long id) {
                String newStatus = parent.getItemAtPosition(spinnerPosition).toString();

                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks").child(task.getId());
                taskRef.child("status").setValue(newStatus);

                task.setStatus(newStatus);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        holder.editButton.setOnClickListener(v -> {
            EditTaskDialog.showEditDialog(holder.itemView.getContext(), task, updatedTask -> {
                DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks").child(task.getId());
                taskRef.setValue(updatedTask);

                taskList.set(position, updatedTask);
                notifyItemChanged(position);
            });
        });

        holder.deleteButton.setOnClickListener(v -> {
            DatabaseReference taskRef = FirebaseDatabase.getInstance().getReference("tasks").child(task.getId());
            taskRef.removeValue();

            taskList.remove(position);
            notifyItemRemoved(position);
        });

        holder.assignBUtton.setOnClickListener(v -> {
            AssignAssignmentsDialog.showAssignDialog(holder.itemView.getContext(), task);
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView taskName, taskDate;
        Spinner statusSpinner;
        ImageButton editButton, deleteButton, assignBUtton;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskName = itemView.findViewById(R.id.taskName);
            taskDate = itemView.findViewById(R.id.taskDate);
            statusSpinner = itemView.findViewById(R.id.statusSpinner);
            editButton = itemView.findViewById(R.id.editButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            assignBUtton = itemView.findViewById(R.id.assignButton);
        }
    }
}
