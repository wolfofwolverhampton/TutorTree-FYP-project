package com.javainternal.Students;

public class GlobalStudentUid {
    private static GlobalStudentUid instance;
    private String studentUid;

    private GlobalStudentUid() {}

    public static synchronized GlobalStudentUid getInstance() {
        if (instance == null) {
            instance = new GlobalStudentUid();
        }
        return instance;
    }

    public String getStudentUid() {
        return studentUid;
    }

    public void setStudentUid(String studentUid) {
        this.studentUid = studentUid;
    }
}