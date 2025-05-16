package com.javainternal.Assignments.Dialogs;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.javainternal.Assignments.Model.Task;
import com.javainternal.R;

import java.util.Calendar;

public class EditTaskDialog {
    public interface OnTaskUpdatedListener {
        void onTaskUpdated(Task updatedTask);
    }

    public static void showEditDialog(Context context, Task task, OnTaskUpdatedListener listener) {
        // Inflate the custom dialog layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_task, null);

        // Initialize views
        EditText nameInput = dialogView.findViewById(R.id.editTaskName);
        Button selectDateButton = dialogView.findViewById(R.id.selectDateButton);
        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);

        // Set initial values
        nameInput.setText(task.getName());

        // Initialize selectedDate with the task's current date or default to today's date
        String initialDate = task.getDate();
        if (initialDate == null || initialDate.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1; // Month is 1-based for display
            int year = calendar.get(Calendar.YEAR);
            initialDate = day + "/" + month + "/" + year; // Default to today's date
        }
        final String[] selectedDate = {initialDate}; // Store the selected date
        selectedDateText.setText("Selected Date: " + initialDate);

        // Set up DatePickerDialog
        final Calendar calendar = Calendar.getInstance();
        String[] dateParts = initialDate.split("/");
        int initialDay = Integer.parseInt(dateParts[0]);
        int initialMonth = Integer.parseInt(dateParts[1]) - 1; // Month is 0-based for DatePicker
        int initialYear = Integer.parseInt(dateParts[2]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate[0] = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    selectedDateText.setText("Selected Date: " + selectedDate[0]);
                },
                initialYear, initialMonth, initialDay
        );

        // Open DatePickerDialog when the Select Date button is clicked
        selectDateButton.setOnClickListener(v -> datePickerDialog.show());

        // Build the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Task");
        builder.setView(dialogView);

        // Save Button
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();

            if (!newName.isEmpty()) {
                // Use the selected date (defaults to initialDate if not changed)
                String newDate = selectedDate[0];

                // Update the task object with the selected date
                Task updatedTask = new Task(task.getId(), newName, task.getStatus(), newDate);

                // Notify the listener with the updated task
                listener.onTaskUpdated(updatedTask);
            }
        });

        // Cancel Button
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }
}
