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
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_edit_task, null);

        EditText nameInput = dialogView.findViewById(R.id.editTaskName);
        Button selectDateButton = dialogView.findViewById(R.id.selectDateButton);
        TextView selectedDateText = dialogView.findViewById(R.id.selectedDateText);

        nameInput.setText(task.getName());

        String initialDate = task.getDueDate();
        if (initialDate == null || initialDate.isEmpty()) {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH) + 1;
            int year = calendar.get(Calendar.YEAR);
            initialDate = day + "/" + month + "/" + year;
        }
        final String[] selectedDate = {initialDate};
        selectedDateText.setText("Selected Date: " + initialDate);

        final Calendar calendar = Calendar.getInstance();
        String[] dateParts = initialDate.split("/");
        int initialDay = Integer.parseInt(dateParts[0]);
        int initialMonth = Integer.parseInt(dateParts[1]) - 1;
        int initialYear = Integer.parseInt(dateParts[2]);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                context,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate[0] = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    selectedDateText.setText("Selected Date: " + selectedDate[0]);
                },
                initialYear, initialMonth, initialDay
        );

        selectDateButton.setOnClickListener(v -> datePickerDialog.show());

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Edit Task");
        builder.setView(dialogView);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = nameInput.getText().toString().trim();

            if (!newName.isEmpty()) {
                String newDate = selectedDate[0];

                Task updatedTask = new Task(task.getId(), newName, task.getStatus(), task.getAssignedDate(), newDate);

                listener.onTaskUpdated(updatedTask);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
