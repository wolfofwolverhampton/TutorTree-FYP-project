package com.javainternal;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class PaymentResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_result);

        // Get the deep link data
        Uri data = getIntent().getData();
        if (data != null) {
            String status = data.getQueryParameter("status"); // e.g., "success" or "failure"
            String transactionId = data.getQueryParameter("transaction_id");

            // Display the payment result
            if ("Completed".equals(status)) {
                Toast.makeText(this, "Payment Successful!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Payment Failed!", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid return URL.", Toast.LENGTH_SHORT).show();
        }
    }
}