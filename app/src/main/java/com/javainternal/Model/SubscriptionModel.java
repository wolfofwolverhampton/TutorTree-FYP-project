package com.javainternal.Model;

import com.javainternal.Constants.SubscriptionStatus;

public class SubscriptionModel {
    private String subscriptionId;
    private String packageTitle;
    private double packagePrice;
    private int packageDuration;
    private String teacherUid;
    private String studentUid;
    private long subscribedAt;
    private SubscriptionStatus status;

    private String teacherName;
    private String studentName;

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public SubscriptionModel() {
    }

    public SubscriptionModel(String subscriptionId, String packageTitle, double packagePrice, int packageDuration, String teacherUid, String studentUid, long subscribedAt, SubscriptionStatus status) {
        this.packageTitle = packageTitle;
        this.packagePrice = packagePrice;
        this.packageDuration = packageDuration;
        this.teacherUid = teacherUid;
        this.studentUid = studentUid;
        this.subscribedAt = subscribedAt;
        this.subscriptionId = subscriptionId;
        this.status = status;
    }
    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getPackageTitle() { return packageTitle; }
    public double getPackagePrice() { return packagePrice; }
    public String getTeacherUid() { return teacherUid; }
    public String getStudentUid() { return studentUid; }
    public long getSubscribedAt() { return subscribedAt; }

    public void setPackageTitle(String packageTitle) { this.packageTitle = packageTitle; }
    public void setTeacherUid(String teacherUid) { this.teacherUid = teacherUid; }
    public void setStudentUid(String studentUid) { this.studentUid = studentUid; }
    public void setSubscribedAt(long subscribedAt) { this.subscribedAt = subscribedAt; }

    public void setPackagePrice(double packagePrice) {
        this.packagePrice = packagePrice;
    }

    public int getPackageDuration() {
        return packageDuration;
    }

    public void setPackageDuration(int packageDuration) {
        this.packageDuration = packageDuration;
    }

    public SubscriptionStatus getStatusEnum() {
        return status;
    }

    public String getStatus() {
        return status != null ? status.name() : null;
    }

    public void setStatus(String statusString) {
        if (statusString != null) {
            try {
                this.status = SubscriptionStatus.valueOf(statusString.toUpperCase());
            } catch (IllegalArgumentException e) {
                this.status = null;  // or some default, e.g. PENDING
            }
        }
    }
}
