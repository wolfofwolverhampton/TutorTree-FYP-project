package com.javainternal.Teachers;

public class GlobalTeacherUid {
    private static GlobalTeacherUid instance;
    private String teacherUid;

    private GlobalTeacherUid() {}

    public static synchronized GlobalTeacherUid getInstance() {
        if (instance == null) {
            instance = new GlobalTeacherUid();
        }
        return instance;
    }

    public String getTeacherUid() {
        return teacherUid;
    }

    public void setTeacherUid(String teacherUid) {
        this.teacherUid = teacherUid;
    }
}