package com.javainternal.Students;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.Attendance.Decorator.AttendanceDecorator;
import com.javainternal.Attendance.Model.AttendanceModel;
import com.javainternal.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

import java.util.ArrayList;
import java.util.List;

public class StudentAttendanceActivity extends AppCompatActivity {

    private MaterialCalendarView calendarView;
    private TextView tvSummary;
    private RadioGroup filterGroup;

    private List<AttendanceModel> allAttendance = new ArrayList<>();
    private List<CalendarDay> presentDates = new ArrayList<>();
    private List<CalendarDay> absentDates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_attendance);

        calendarView = findViewById(R.id.calendarView);
        tvSummary = findViewById(R.id.tvSummary);
        filterGroup = findViewById(R.id.filterGroup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load attendance from Firebase
        loadAttendance();

        filterGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.weekFilter) {
                calendarView.state().edit().setCalendarDisplayMode(CalendarMode.WEEKS).commit();
            } else {
                calendarView.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS).commit();
            }
        });
    }

    private void loadAttendance() {
        String studentId = GlobalStudentUid.getInstance().getStudentUid();

        FirebaseDatabase.getInstance().getReference("student_attendance")
                .child(studentId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        presentDates.clear();
                        absentDates.clear();
                        allAttendance.clear();

                        for (DataSnapshot snap : snapshot.getChildren()) {
                            AttendanceModel model = snap.getValue(AttendanceModel.class);
                            allAttendance.add(model);

                            long timestampMillis = model.getDate();
                            LocalDate date = Instant.ofEpochMilli(timestampMillis)
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate();

                            CalendarDay day = CalendarDay.from(date);
                            if (model.isPresent()) {
                                presentDates.add(day);
                            } else {
                                absentDates.add(day);
                            }
                        }

                        decorateCalendar();
                        updateSummary();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentAttendanceActivity.this, "Error loading attendance", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void decorateCalendar() {
        calendarView.removeDecorators();
        calendarView.addDecorator(new AttendanceDecorator(presentDates, Color.parseColor("#A5D6A7"))); // green
        calendarView.addDecorator(new AttendanceDecorator(absentDates, Color.parseColor("#EF9A9A")));  // red
    }

    private void updateSummary() {
        int total = allAttendance.size();
        int present = presentDates.size();
        tvSummary.setText("Present " + present + " out of " + total + " days");
    }
}

