package com.javainternal.Utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.Model.SubscriptionModel;
import com.javainternal.Model.TuitionPackageModel;
import com.javainternal.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class KhaltiUtils {
    public static void payWithKhalti(Context context, TuitionPackageModel tuitionPackage, SubscriptionModel subscription) {
        if (subscription.getTeacherUid() == null || subscription.getTeacherUid().isEmpty()) {
            Toast.makeText(context, "Teacher UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference teachersRef = FirebaseDatabase.getInstance().getReference("teachers");
        teachersRef.child(subscription.getTeacherUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String teacherName = dataSnapshot.child("name").getValue(String.class);
                    String teacherEmail = dataSnapshot.child("gmail").getValue(String.class);
                    String teacherPhone = dataSnapshot.child("phoneNumber").getValue(String.class);

                    initializeKhaltiPayment(context, tuitionPackage, teacherName, teacherEmail, teacherPhone, subscription);
                } else {
                    Toast.makeText(context, "Teacher data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(context, "Failed to load teacher data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static void initializeKhaltiPayment(Context context, TuitionPackageModel tuitionPackage, String teacherName, String teacherEmail, String teacherPhone, SubscriptionModel subscription) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = context.getString(R.string.backend_url) + "/payment/khalti/initiate";
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("amount", tuitionPackage.getPrice());
            jsonBody.put("subscriptionId", subscription.getSubscriptionId());
            jsonBody.put("orderName", "Tuition: " + tuitionPackage.getTitle());
            jsonBody.put("teacherUid", subscription.getTeacherUid());
            jsonBody.put("studentUid", subscription.getStudentUid());
            jsonBody.put("teacherName", teacherName);
            jsonBody.put("teacherEmail", teacherEmail);
            jsonBody.put("teacherPhone", teacherPhone);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                response -> {
                    String paymentUrl = response.optString("paymentUrl");
                    if (paymentUrl != null && !paymentUrl.isEmpty()) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                        context.startActivity(browserIntent);
                    } else {
                        Toast.makeText(context, "Failed to get payment URL.", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        Log.e("KhaltiError", "Status Code: " + error.networkResponse.statusCode);
                        Log.e("KhaltiError", "Response Data: " + new String(error.networkResponse.data));
                    } else {
                        Log.e("KhaltiError", "Error: " + error.getMessage());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }
}