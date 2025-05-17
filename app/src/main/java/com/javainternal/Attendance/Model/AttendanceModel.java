package com.javainternal.Attendance.Model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AttendanceModel {
    private long date;
    private boolean present;
    private String teacherUid;

    public AttendanceModel() {
    }

    public AttendanceModel(long date, boolean present, String teacherUid) {
        this.date = date;
        this.present = present;
        this.teacherUid = teacherUid;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(date));
    }

    public String getTeacherUid() {
        return teacherUid;
    }

    public void setTeacherUid(String teacherUid) {
        this.teacherUid = teacherUid;
    }
}



