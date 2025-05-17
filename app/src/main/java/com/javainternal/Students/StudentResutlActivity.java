package com.javainternal.Students;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class StudentResutlActivity extends AppCompatActivity {

    private TextView attendanceSummaryTv, mcqSummaryTv, assignmentSummaryTv;
    private PieChart attendanceChart;
    private BarChart mcqChart;
    private ProgressBar assignmentProgress;

    private String teacherUid, studentUid;
    private long subscribedAt;
    private int durationInMonths;

    // Hold fetched data
    private int presentDays = 0;
    private int totalAttendanceDays = 0;

    private int totalMCQSubmissions = 0;
    private int totalCorrectAnswers = 0;
    private int totalMCQQuestions = 0;

    private int totalAssignments = 0;
    private int completedAssignments = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_result);

        attendanceSummaryTv = findViewById(R.id.attendanceSummaryTv);
        mcqSummaryTv = findViewById(R.id.mcqSummaryTv);
        assignmentSummaryTv = findViewById(R.id.assignmentSummaryTv);

        attendanceChart = findViewById(R.id.attendanceChart);
        mcqChart = findViewById(R.id.mcqChart);
        assignmentProgress = findViewById(R.id.assignmentProgress);

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

        Calendar endDate = (Calendar) subscribedDate.clone();
        endDate.add(Calendar.MONTH, durationInMonths);
        resetToStartOfDay(endDate);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("attendance").child(studentUid);
        Calendar finalEndDate = endDate;
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                presentDays = 0;
                totalAttendanceDays = 0;

                for (DataSnapshot entry : snapshot.getChildren()) {
                    Long dateMillis = entry.child("date").getValue(Long.class);
                    Boolean present = entry.child("present").getValue(Boolean.class);
                    String currentTeacherUid = entry.child("teacherUid").getValue(String.class);

                    if (dateMillis != null && currentTeacherUid != null && currentTeacherUid.equals(teacherUid)) {
                        Calendar date = Calendar.getInstance();
                        date.setTimeInMillis(dateMillis);
                        resetToStartOfDay(date);

                        boolean inRange = !date.before(subscribedDate) && !date.after(finalEndDate);
                        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);

                        if (inRange && dayOfWeek != Calendar.SATURDAY) {
                            totalAttendanceDays++;
                            if (present != null && present) {
                                presentDays++;
                            }
                        }
                    }
                }

                attendanceSummaryTv.setText("Attendance: " + presentDays + "/" + totalAttendanceDays + " days");
                renderAttendanceChart(presentDays, totalAttendanceDays);
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
                totalAssignments = 0;
                completedAssignments = 0;

                for (DataSnapshot taskSnap : snapshot.getChildren()) {
                    totalAssignments++;
                    String status = taskSnap.child("status").getValue(String.class);
                    if ("Completed".equalsIgnoreCase(status)) {
                        completedAssignments++;
                    }
                }

                assignmentSummaryTv.setText("Assignments: " + completedAssignments + "/" + totalAssignments + " completed");
                updateAssignmentProgress(completedAssignments, totalAssignments);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Assignments", "Database error: " + error.getMessage());
            }
        });
    }

    private void fetchMCQSummary() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("submissions");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalMCQSubmissions = 0;
                totalCorrectAnswers = 0;
                totalMCQQuestions = 0;

                for (DataSnapshot submissionSnap : snapshot.getChildren()) {
                    for (DataSnapshot studentSnap : submissionSnap.getChildren()) {
                        if (studentSnap.getKey().equals(studentUid)) {
                            totalMCQSubmissions++;

                            DataSnapshot answersSnap = studentSnap.child("answers");
                            for (DataSnapshot answerSnap : answersSnap.getChildren()) {
                                Boolean isCorrect = answerSnap.child("isCorrect").getValue(Boolean.class);
                                if (isCorrect != null) {
                                    totalMCQQuestions++;
                                    if (isCorrect) totalCorrectAnswers++;
                                }
                            }
                        }
                    }
                }

                double avg = (totalMCQQuestions > 0) ? ((totalCorrectAnswers * 100.0) / totalMCQQuestions) : 0.0;
                mcqSummaryTv.setText("MCQs: " + totalMCQSubmissions + " sets\nAvg: " + String.format("%.1f", avg) + "% correct");
                renderMCQChart(totalCorrectAnswers, totalMCQQuestions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("MCQSummary", "Database error: " + error.getMessage());
            }
        });
    }

    // Chart rendering functions

    private void renderAttendanceChart(int present, int total) {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(present, "Present"));
        entries.add(new PieEntry(total - present, "Absent"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);

        PieData data = new PieData(dataSet);

        attendanceChart.setData(data);
        attendanceChart.setCenterText("Attendance");
        attendanceChart.setEntryLabelColor(Color.BLACK);
        attendanceChart.getDescription().setEnabled(false);
        attendanceChart.invalidate();
    }

    private void renderMCQChart(int correct, int total) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, correct));
        entries.add(new BarEntry(1, total - correct));

        BarDataSet dataSet = new BarDataSet(entries, "MCQ Accuracy");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        dataSet.setValueTextSize(14f);

        BarData data = new BarData(dataSet);

        mcqChart.setData(data);
        mcqChart.getXAxis().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (Math.round(value) == 0) {
                    return "Correct";
                } else if (Math.round(value) == 1) {
                    return "Wrong";
                } else {
                    return "";
                }
            }
        });
        mcqChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        mcqChart.getXAxis().setGranularity(1f);
        mcqChart.getXAxis().setGranularityEnabled(true);
        mcqChart.getDescription().setEnabled(false);
        mcqChart.invalidate();
    }

    private void updateAssignmentProgress(int completed, int total) {
        if (total == 0) total = 1;
        int progress = (int) ((completed * 100.0f) / total);
        assignmentProgress.setProgress(progress);
    }
}