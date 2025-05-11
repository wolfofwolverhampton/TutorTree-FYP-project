package com.javainternal.Teachers;

public class GlobalTeacherUid {
    private static GlobalTeacherUid instance;
    private String teacherUid;

    // Private constructor to prevent instantiation
    private GlobalTeacherUid() {}

    // Method to get the singleton instance
    public static synchronized GlobalTeacherUid getInstance() {
        if (instance == null) {
            instance = new GlobalTeacherUid();
        }
        return instance;
    }

    // Getter for teacher UID
    public String getTeacherUid() {
        return teacherUid;
    }

    // Setter for teacher UID
    public void setTeacherUid(String teacherUid) {
        this.teacherUid = teacherUid;
    }
}