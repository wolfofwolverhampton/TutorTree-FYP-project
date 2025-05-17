package com.javainternal.Teachers;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.Attendance.Decorator.AttendanceDecorator;
import com.javainternal.Attendance.Model.AttendanceModel;
import com.javainternal.R;
import com.javainternal.Utils.NotificationUtils;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.List;

public class TeacherStudentAttendanceActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private Switch switchPresent;
    private Button buttonSaveAttendance;

    private DatabaseReference attendanceRef;

    private String studentUid;
    private String teacherUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_student_attendance);

        calendarView = findViewById(R.id.calendarView);
        switchPresent = findViewById(R.id.switchPresent);
        buttonSaveAttendance = findViewById(R.id.buttonSaveAttendance);

        studentUid = getIntent().getStringExtra("studentUid");
        teacherUid = UserAuthContext.getInstance(this).getLoggedInPhone();

        attendanceRef = FirebaseDatabase.getInstance()
                .getReference("attendance")
                .child(studentUid);

        calendarView.setSelectedDate(CalendarDay.today());

        loadAndDecorateAttendance();

        buttonSaveAttendance.setOnClickListener(v -> {
            CalendarDay selectedDate = calendarView.getSelectedDate();
            if (selectedDate == null) {
                Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
                return;
            }

            long dateInMillis = getDateInMillis(selectedDate);

            boolean isPresent = switchPresent.isChecked();

            AttendanceModel attendance = new AttendanceModel(dateInMillis, isPresent, teacherUid);

            String dateKey = attendance.getFormattedDate();
            saveAttendanceWithCheck(attendanceRef, dateKey, attendance);
        });
    }

    private long getDateInMillis(@NonNull CalendarDay day) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.clear();
        cal.set(day.getYear(), day.getMonth() - 1, day.getDay());
        return cal.getTimeInMillis();
    }

    private void loadAndDecorateAttendance() {
        attendanceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<CalendarDay> presentDates = new ArrayList<>();
                List<CalendarDay> absentDates = new ArrayList<>();

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    AttendanceModel attendance = dateSnapshot.getValue(AttendanceModel.class);

                    if (attendance != null) {
                        long timestampMillis = attendance.getDate();
                        LocalDate date = Instant.ofEpochMilli(timestampMillis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();

                        CalendarDay day = CalendarDay.from(date);

                        if (attendance.isPresent()) {
                            presentDates.add(day);
                        } else {
                            absentDates.add(day);
                        }
                    }
                }

                calendarView.removeDecorators();
                calendarView.addDecorator(new AttendanceDecorator(presentDates, Color.parseColor("#4CAF50")));
                calendarView.addDecorator(new AttendanceDecorator(absentDates, Color.parseColor("#F44336")));
                calendarView.invalidateDecorators();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherStudentAttendanceActivity.this,
                        "Failed to load attendance", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveAttendanceWithCheck(DatabaseReference attendanceRef, String dateKey, AttendanceModel attendance) {
        attendanceRef.child(dateKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    new AlertDialog.Builder(TeacherStudentAttendanceActivity.this)
                            .setTitle("Overwrite Attendance?")
                            .setMessage("Attendance for this date already exists. Do you want to overwrite it?")
                            .setPositiveButton("Yes", (dialog, which) -> {
                                attendanceRef.child(dateKey).setValue(attendance)
                                        .addOnSuccessListener(aVoid -> loadAndDecorateAttendance())
                                        .addOnFailureListener(e -> Toast.makeText(TeacherStudentAttendanceActivity.this, "Failed to Save", Toast.LENGTH_SHORT).show());
                            })
                            .setNegativeButton("No", (dialog, which) -> {
                                dialog.dismiss();
                            })
                            .show();
                } else {
                    attendanceRef.child(dateKey).setValue(attendance)
                            .addOnSuccessListener(aVoid -> loadAndDecorateAttendance())
                            .addOnFailureListener(e -> Toast.makeText(TeacherStudentAttendanceActivity.this, "Failed to save", Toast.LENGTH_SHORT).show());
                }
                String attendanceText = "Absent";
                if (attendance.isPresent()) {
                    attendanceText = "Present";
                }
                NotificationUtils.sendNotification(TeacherStudentAttendanceActivity.this, teacherUid, studentUid, "Your attendance: " + attendanceText + " for the date: " + attendance.getFormattedDate() + "has been recorded by the teacher.");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherStudentAttendanceActivity.this, "Failed to save", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
