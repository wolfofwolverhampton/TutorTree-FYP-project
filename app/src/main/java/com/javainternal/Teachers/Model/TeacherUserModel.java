package com.javainternal.Teachers.Model;

public class TeacherUserModel {

    private String uid;
    private String name;
    private String phoneNumber;
    private String password;
    private String otpReceived;
    private String gmail; // New field
    private String category;

    // No-Argument Constructor (REQUIRED FOR FIREBASE)
    public TeacherUserModel() {
    }

    // Parameterized Constructor
    public TeacherUserModel(String uid, String name, String phoneNumber, String password, String otpReceived, String gmail, String category) {
        this.uid = uid;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.otpReceived = otpReceived;
        this.gmail = gmail;
        this.category = category;
    }

    // Getter and Setter for UID
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    // Getter and Setter for Name
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // Getter and Setter for Phone Number
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // Getter and Setter for Password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Getter and Setter for OTP Received
    public String getOtpReceived() {
        return otpReceived;
    }

    public void setOtpReceived(String otpReceived) {
        this.otpReceived = otpReceived;
    }

    // Getter and Setter for Gmail
    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    // Getter and Setter for Category
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}