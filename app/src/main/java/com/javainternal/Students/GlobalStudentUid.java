package com.javainternal.Students;

public class GlobalStudentUid {
    private static GlobalStudentUid instance;
    private String studentUid;

    // Private constructor to prevent instantiation
    private GlobalStudentUid() {}

    // Method to get the singleton instance
    public static synchronized GlobalStudentUid getInstance() {
        if (instance == null) {
            instance = new GlobalStudentUid();
        }
        return instance;
    }

    // Getter for student UID
    public String getStudentUid() {
        return studentUid;
    }

    // Setter for student UID
    public void setStudentUid(String studentUid) {
        this.studentUid = studentUid;
    }
}