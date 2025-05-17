package com.javainternal.Students;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;
import com.javainternal.R;

import java.util.Calendar;

public class StudentResutlActivity extends AppCompatActivity {

    private TextView attendanceSummaryTv, mcqSummaryTv, assignmentSummaryTv;
    private String teacherUid, studentUid;
    private long subscribedAt;
    private int durationInMonths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_result);

        attendanceSummaryTv = findViewById(R.id.attendanceSummaryTv);
        mcqSummaryTv = findViewById(R.id.mcqSummaryTv);
        assignmentSummaryTv = findViewById(R.id.assignmentSummaryTv);

        studentUid = GlobalStudentUid.getInstance().getStudentUid();
        teacherUid = getIntent().getStringExtra("teacherUid");
        subscribedAt = getIntent().getLongExtra("subscribedAt", 0);
        durationInMonths = getIntent().getIntExtra("durationInMonths", 1);

        computeAttendance();
        fetchMCQSummary();
        fetchAssignmentSummary();
    }

    private void resetToStartOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void computeAttendance() {
        Calendar subscribedDate = Calendar.getInstance();
        subscribedDate.setTimeInMillis(subscribedAt);
        resetToStartOfDay(subscribedDate);

        Calendar now = Calendar.getInstance();
        resetToStartOfDay(now);

        Calendar endDate = (Calendar) subscribedDate.clone();
        endDate.add(Calendar.MONTH, durationInMonths);
        resetToStartOfDay(endDate);

        if (now.before(endDate)) {
            endDate = now;
        }

        // Debug logs for start and end date
        Log.d("Attendance", "SubscribedDate: " + subscribedDate.getTime());
        Log.d("Attendance", "EndDate: " + endDate.getTime());

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("attendance").child(studentUid);
        Calendar finalEndDate = endDate;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int presentDays = 0;
                int totalDays = 0;

                for (DataSnapshot entry : snapshot.getChildren()) {
                    Long dateMillis = entry.child("date").getValue(Long.class);
                    Boolean present = entry.child("present").getValue(Boolean.class);

                    if (dateMillis != null) {
                        Calendar date = Calendar.getInstance();
                        date.setTimeInMillis(dateMillis);
                        resetToStartOfDay(date);

                        boolean inRange = !date.before(subscribedDate) && !date.after(finalEndDate);
                        Log.d("Attendance", "Date: " + date.getTime() + ", Present: " + present + ", InRange: " + inRange);

                        if (inRange) {
                            totalDays++;
                            if (present != null && present) {
                                presentDays++;
                            }
                        }
                    }
                }

                attendanceSummaryTv.setText("Attendance: " + presentDays + "/" + totalDays + " days");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Attendance", "Database error: " + error.getMessage());
            }
        });
    }

    private void fetchAssignmentSummary() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("student_tasks").child(studentUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int total = 0;
                int completed = 0;

                for (DataSnapshot taskSnap : snapshot.getChildren()) {
                    total++;
                    String status = taskSnap.child("status").getValue(String.class);
                    if ("Completed".equalsIgnoreCase(status)) {
                        completed++;
                    }
                }

                assignmentSummaryTv.setText("Assignments: " + completed + "/" + total + " completed");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void fetchMCQSummary() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("submissions");
        ref.orderByChild("studentUid").equalTo(studentUid)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int totalSubmissions = 0;
                        int totalCorrect = 0;
                        int totalQuestions = 0;

                        for (DataSnapshot submissionSnap : snapshot.getChildren()) {
                            totalSubmissions++;

                            DataSnapshot answersSnap = submissionSnap.child("answers");
                            for (DataSnapshot answerSnap : answersSnap.getChildren()) {
                                Boolean isCorrect = answerSnap.child("isCorrect").getValue(Boolean.class);
                                if (isCorrect != null) {
                                    totalQuestions++;
                                    if (isCorrect) totalCorrect++;
                                }
                            }
                        }

                        double avg = (totalQuestions > 0) ? ((totalCorrect * 100.0) / totalQuestions) : 0.0;
                        mcqSummaryTv.setText("MCQs: " + totalSubmissions + " sets\nAvg: " + String.format("%.1f", avg) + "% correct");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }
}
