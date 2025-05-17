package com.javainternal.Attendance.Model;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;

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
        LocalDate localDate = Instant.ofEpochMilli(date)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return localDate.toString();
    }

    public String getTeacherUid() {
        return teacherUid;
    }

    public void setTeacherUid(String teacherUid) {
        this.teacherUid = teacherUid;
    }
}



