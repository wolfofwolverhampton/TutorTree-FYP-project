package com.javainternal.Assignments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
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
import com.javainternal.Assignments.Adapter.TaskAdapter;
import com.javainternal.Assignments.Model.Task;
import com.javainternal.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AssignmentActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private List<Task> taskList;

    private String selectedDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        databaseReference = FirebaseDatabase.getInstance().getReference("tasks");

        taskRecyclerView = findViewById(R.id.taskRecyclerView);
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        taskList = new ArrayList<>();
        taskAdapter = new TaskAdapter(taskList);
        taskRecyclerView.setAdapter(taskAdapter);

        fetchTasksFromFirebase();

        EditText taskNameInput = findViewById(R.id.taskNameInput);
        Button selectDateButton = findViewById(R.id.selectDateButton);
        Button addTaskButton = findViewById(R.id.addTaskButton);

        selectDateButton.setOnClickListener(v -> showDatePickerDialog());

        addTaskButton.setOnClickListener(v -> {
            String taskName = taskNameInput.getText().toString().trim();

            if (taskName.isEmpty()) {
                taskNameInput.setError("Task name cannot be empty");
                return;
            }

            if (selectedDate.isEmpty()) {
                Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentDate = getCurrentDateString();

            String taskId = databaseReference.push().getKey();
            Task newTask = new Task(taskId, taskName, "In Progress", currentDate, selectedDate);

            if (taskId != null) {
                databaseReference.child(taskId).setValue(newTask)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                taskNameInput.setText("");
                                selectedDate = "";
                                Toast.makeText(this, "Task added successfully!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Failed to add task", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    Toast.makeText(this, "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void fetchTasksFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                taskList.clear();

                new Thread(() -> {
                    List<Task> updatedTaskList = new ArrayList<>();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Task task = dataSnapshot.getValue(Task.class);
                        if (task != null) {
                            task.setId(dataSnapshot.getKey());
                            updatedTaskList.add(task);
                        }
                    }

                    runOnUiThread(() -> {
                        taskList.addAll(updatedTaskList);
                        taskAdapter.notifyDataSetChanged();
                    });
                }).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AssignmentActivity.this, "Failed to fetch tasks", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getCurrentDateString() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        return String.format("%02d/%02d/%d", day, month, year);
    }
}