package com.javainternal;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.FirebaseDatabase;
import com.javainternal.Constants.SubscriptionStatus;
import com.javainternal.Model.TransactionModel;

import java.util.HashMap;
import java.util.Map;

public class PaymentResultActivity extends AppCompatActivity {
    private static final String TAG = "KhaltiPayment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        Uri data = getIntent().getData();
        if (data != null) {
            String status = data.getQueryParameter("status");
            String transactionId = data.getQueryParameter("transaction_id");
            String pidx = data.getQueryParameter("pidx");
            String mobile = data.getQueryParameter("mobile");
            int amount = Integer.parseInt(data.getQueryParameter("amount"));
            String subscriptionId = data.getQueryParameter("subscription_id");
            String orderName = data.getQueryParameter("order_name");
            String teacherUid = data.getQueryParameter("teacher_uid");
            String studentUid = data.getQueryParameter("student_uid");

            TransactionModel transaction = new TransactionModel(
                    transactionId,
                    subscriptionId,
                    pidx,
                    studentUid,
                    teacherUid,
                    status,
                    amount,
                    mobile,
                    orderName,
                    System.currentTimeMillis()
            );

            FirebaseDatabase.getInstance()
                    .getReference("khalti_payments")
                    .push()
                    .setValue(transaction)
                    .addOnSuccessListener(aVoid -> {
                        if ("Completed".equalsIgnoreCase(status)) {
                            Toast.makeText(this, "Payment Successful!", Toast.LENGTH_LONG).show();
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("status", SubscriptionStatus.PAID.toString());
                            updates.put("statusEnum", SubscriptionStatus.PAID.toString());

                            FirebaseDatabase.getInstance()
                                    .getReference("subscriptions")
                                    .child(transaction.getSubscriptionId())
                                    .updateChildren(updates)
                                    .addOnSuccessListener(unused -> Log.d(TAG, "Subscription status updated to PAID"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to update subscription status", e));
                        } else {
                            Toast.makeText(this, "Payment recorded with status: " + status, Toast.LENGTH_LONG).show();
                        }

                        TextView amountPaid = findViewById(R.id.amountPaid);
                        TextView statusText = findViewById(R.id.statusText);
                        TextView transactionIdText = findViewById(R.id.transactionIdText);
                        TextView orderIdText = findViewById(R.id.orderIdText);
                        TextView orderNameText = findViewById(R.id.orderNameText);
                        TextView teacherUidText = findViewById(R.id.teacherUidText);

                        amountPaid.setText("Amount: Rs. " + transaction.getAmount());
                        statusText.setText("Status: " + transaction.getStatus());
                        transactionIdText.setText("Transaction ID: " + transaction.getTransactionId());
                        orderIdText.setText("Subscription ID: " + transaction.getSubscriptionId());
                        orderNameText.setText("Subscription Package Name: " + transaction.getPurchaseOrderName());
                        teacherUidText.setText("Teacher UID: " + transaction.getTeacherUid());

                    })

                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Invalid payment return URL.", Toast.LENGTH_SHORT).show();
        }
    }
}