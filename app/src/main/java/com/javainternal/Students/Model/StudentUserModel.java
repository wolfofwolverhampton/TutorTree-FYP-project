package com.javainternal.Students.Model;

public class StudentUserModel {

    private String uid;
    private String name;
    private String phoneNumber;
    private String guardianName;
    private String password;
    private String otpReceived;
    private String gmail; // New field
    private String guardianGmail; // New field
    private String category;

    // Default Constructor (required for Firebase)
    public StudentUserModel() {}

    // Parameterized Constructor
    public StudentUserModel(String uid, String name, String phoneNumber, String guardianName, String password) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.guardianName = guardianName;
        this.password = password;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getOtpReceived() { return otpReceived; }
    public void setOtpReceived(String otpReceived) { this.otpReceived = otpReceived; }

    public String getGmail() { return gmail; }
    public void setGmail(String gmail) { this.gmail = gmail; }

    public String getGuardianGmail() { return guardianGmail; }
    public void setGuardianGmail(String guardianGmail) { this.guardianGmail = guardianGmail; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}