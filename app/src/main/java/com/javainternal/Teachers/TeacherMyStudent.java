package com.javainternal.Teachers;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.ChatActivity;
import com.javainternal.Constants.SubscriptionStatus;
import com.javainternal.Model.SubscriptionModel;
import com.javainternal.R;
import com.javainternal.Teachers.Adapter.TeacherMyStudentAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherMyStudent extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TeacherMyStudentAdapter adapter;
    private final List<SubscriptionModel> allSubscriptions = new ArrayList<>();
    private String currentTeacherUid;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_my_student);

        recyclerView = findViewById(R.id.studentRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TeacherMyStudentAdapter(this, allSubscriptions, new TeacherMyStudentAdapter.OnRequestActionListener() {
            @Override
            public void onAccept(SubscriptionModel subscription) {
                updateSubscriptionStatus(subscription, SubscriptionStatus.ACCEPTED);
            }

            @Override
            public void onReject(SubscriptionModel subscription) {
                updateSubscriptionStatus(subscription, SubscriptionStatus.CANCELLED);
            }

            @Override
            public void onAssign(String studentUid) {
                showAssignAssignmentView(studentUid);
            }

            @Override
            public void onChat(String name, String senderUid, String receiverUid) {
                Intent intent = new Intent(TeacherMyStudent.this, ChatActivity.class);
                intent.putExtra("name", name);
                intent.putExtra("senderUid", senderUid);
                intent.putExtra("receiverUid", receiverUid);
                startActivity(intent);
            }

            @Override
            public void onTakeAttendance(String studentUid) {
                Intent intent = new Intent(TeacherMyStudent.this, TeacherStudentAttendanceActivity.class);
                intent.putExtra("studentUid", studentUid);
                startActivity(intent);
            }
        });

        recyclerView.setAdapter(adapter);

        currentTeacherUid = UserAuthContext.getInstance(this).getLoggedInPhone();

        Log.d("Teacher My Student", "Teacher UID" + currentTeacherUid);

        ref = FirebaseDatabase.getInstance().getReference("subscriptions");
        Query query = ref.orderByChild("teacherUid").equalTo(currentTeacherUid);

        query.get()
                .addOnSuccessListener(dataSnapshot -> {
                    allSubscriptions.clear();
                    DatabaseReference studentsRef = FirebaseDatabase.getInstance().getReference("students");

                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        SubscriptionModel model = snap.getValue(SubscriptionModel.class);
                        if (model != null) {
                            allSubscriptions.add(model);

                            String studentUid = model.getStudentUid();
                            studentsRef.child(studentUid).get().addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    String studentName = snapshot.child("name").getValue(String.class);
                                    model.setStudentName(studentName);
                                    adapter.notifyDataSetChanged();
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("MyTeacher", "Failed to load student details", e);
                            });
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("MyTeacher", "Failed to load subscriptions", e);
                });
    }

    private void updateSubscriptionStatus(SubscriptionModel subscription, SubscriptionStatus status) {
        if (subscription.getSubscriptionId() == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", status);
        updates.put("statusEnum", status);

        ref.child(subscription.getSubscriptionId())
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Marked as " + status, Toast.LENGTH_SHORT).show();
                    subscription.setStatus(status.name());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showAssignAssignmentView(String studentUid) {
        Intent intent = new Intent(TeacherMyStudent.this, AssignMcqActivity.class);
        intent.putExtra("studentUid", studentUid);
        startActivity(intent);
    }
}