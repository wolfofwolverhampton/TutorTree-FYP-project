package com.javainternal.Model;

public class TransactionModel {
    private String transactionId;
    private String subscriptionId;
    private String pidx;
    private String studentUid;
    private String teacherUid;
    private String status;
    private int amount;
    private String mobile;
    private String purchaseOrderName;
    private long timestamp;

    public TransactionModel() {}

    public TransactionModel(String transactionId, String subscriptionId, String pidx, String userUid, String teacherUid, String status,
                            int amount, String mobile, String purchaseOrderName, long timestamp) {
        this.transactionId = transactionId;
        this.subscriptionId = subscriptionId;
        this.pidx = pidx;
        this.studentUid = userUid;
        this.teacherUid = teacherUid;
        this.status = status;
        this.amount = amount;
        this.mobile = mobile;
        this.purchaseOrderName = purchaseOrderName;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getPidx() { return pidx; }
    public void setPidx(String pidx) { this.pidx = pidx; }

    public String getStudentUid() { return studentUid; }
    public void setStudentUid(String studentUid) { this.studentUid = studentUid; }

    public String getTeacherUid() { return teacherUid; }
    public void setTeacherUid(String teacherUid) { this.teacherUid = teacherUid; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getAmount() { return amount; }
    public void setAmount(int amount) { this.amount = amount; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getPurchaseOrderName() { return purchaseOrderName; }
    public void setPurchaseOrderName(String purchaseOrderName) { this.purchaseOrderName = purchaseOrderName; }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
