package com.javainternal.Assignments.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.Assignments.Model.Task;
import com.javainternal.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssignAssignmentsDialog {

    public static void showAssignDialog(Context context, Task task) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_assign_task, null);

        Spinner groupSpinner = dialogView.findViewById(R.id.groupSpinner);

        final List<String> groupNames = new ArrayList<>();
        final List<String> groupIds = new ArrayList<>();

        FirebaseDatabase.getInstance().getReference("student_groups")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot groupSnap : snapshot.getChildren()) {
                            groupIds.add(groupSnap.getKey());
                            groupNames.add(groupSnap.child("groupName").getValue(String.class));
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, groupNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        groupSpinner.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(context, "Failed to load groups", Toast.LENGTH_SHORT).show();
                    }
                });

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Assign Task");
        builder.setView(dialogView);

        builder.setPositiveButton("Assign", (dialog, which) -> {
            int selectedPosition = groupSpinner.getSelectedItemPosition();
            if (selectedPosition >= 0) {
                String selectedGroupId = groupIds.get(selectedPosition);

                FirebaseDatabase.getInstance().getReference("student_groups")
                        .child(selectedGroupId).child("studentUids")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                for (DataSnapshot uidSnap : snapshot.getChildren()) {
                                    String studentUid = uidSnap.getValue(String.class);
                                    FirebaseDatabase.getInstance()
                                            .getReference("student_tasks")
                                            .child(studentUid)
                                            .child(task.getId())
                                            .setValue(task);
                                }
                                Toast.makeText(context, "Task assigned to group!", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(context, "Assignment failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}
