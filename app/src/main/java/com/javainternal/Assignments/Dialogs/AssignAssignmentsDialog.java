package com.javainternal.Assignments.Dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.Assignments.Model.Task;
import com.javainternal.R;
import com.javainternal.Teachers.Model.TeacherUserModel;
import com.javainternal.Utils.NotificationUtils;

import java.util.ArrayList;
import java.util.List;

public class AssignAssignmentsDialog {
    public interface TeacherDataCallback {
        void onTeacherLoaded(TeacherUserModel teacher);

        void onError(String error);
    }

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

                getTeacherUser(context, new TeacherDataCallback() {
                    @Override
                    public void onTeacherLoaded(TeacherUserModel teacher) {
                        FirebaseDatabase.getInstance().getReference("student_groups")
                                .child(selectedGroupId).child("studentUids")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        for (DataSnapshot uidSnap : snapshot.getChildren()) {
                                            String studentUid = uidSnap.getValue(String.class);
                                            if (studentUid != null) {
                                                FirebaseDatabase.getInstance()
                                                        .getReference("student_tasks")
                                                        .child(studentUid)
                                                        .child(task.getId())
                                                        .setValue(task);

                                                NotificationUtils.sendNotification(
                                                        context,
                                                        UserAuthContext.getInstance(context).getLoggedInPhone(),
                                                        studentUid,
                                                        task.getName() + " has been assigned to you by " + teacher.getName()
                                                );
                                            }
                                        }
                                        Toast.makeText(context, "Task assigned to group!", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        Toast.makeText(context, "Assignment failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }

                    @Override
                    public void onError(String error) {
                        Log.e("Teacher", "Error fetching teacher data: " + error);
                        Toast.makeText(context, "Failed to get teacher info", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    public static void getTeacherUser(Context context, TeacherDataCallback callback) {
        DatabaseReference teacherRef = FirebaseDatabase.getInstance()
                .getReference("teachers")
                .child(UserAuthContext.getInstance(context).getLoggedInPhone());

        teacherRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    TeacherUserModel teacher = snapshot.getValue(TeacherUserModel.class);
                    callback.onTeacherLoaded(teacher);
                } else {
                    callback.onError("No data found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
}
