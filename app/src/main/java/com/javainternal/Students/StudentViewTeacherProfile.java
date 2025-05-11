package com.javainternal.Students;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.CallActivity;
import com.javainternal.ChatForFind;
import com.javainternal.MainActivity2;
import com.javainternal.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
public class StudentViewTeacherProfile extends AppCompatActivity {
    private TextView teacherName, teacherGmail, gradeCategory, subjectCategory;
    private Button chatButton;

    private DatabaseReference teachersRef;
    private DatabaseReference studentsRef;
    private String studentGmail;
    private String studentName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_view_teacher_profile);

        // Initialize Firebase Database reference
        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");
        studentsRef = FirebaseDatabase.getInstance().getReference("students");

        // Initialize UI components
        teacherName = findViewById(R.id.teacherName);
        teacherGmail = findViewById(R.id.teacherGmail);
        gradeCategory = findViewById(R.id.gradeCategory);
        subjectCategory = findViewById(R.id.subjectCategory);
        chatButton = findViewById(R.id.chatButton);

        // Retrieve the UID (phone number) from the Intent
        Intent intent = getIntent();
        String uid = intent.getStringExtra("uid");

        if (uid == null || uid.isEmpty()) {
            Toast.makeText(this, "UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Fetch the teacher's data from Firebase
        fetchTeacherData(uid);

        Button khaltiButton = findViewById(R.id.khaltiButton);
        khaltiButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payWithKhalti();
            }
        });

        // Set click listener for the "Chat" button
        chatButton.setOnClickListener(v -> {
            // Retrieve the student UID (sender) from the GlobalStudentUid singleton
            String studentUid = GlobalStudentUid.getInstance().getStudentUid();

            // Retrieve the teacher UID (receiver) from the intent
            String teacherUid = getIntent().getStringExtra("uid");

            // Validate the UIDs
            if (studentUid == null || studentUid.isEmpty() || teacherUid == null || teacherUid.isEmpty()) {
                Toast.makeText(this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create an Intent to navigate to ChatForFind activity
            Intent chatIntent = new Intent(this, ChatForFind.class);
            chatIntent.putExtra("name", teacherName.getText().toString()); // Pass the teacher's name
            chatIntent.putExtra("senderUid", studentUid); // Use the modified global student UID as sender
            chatIntent.putExtra("receiverUid", teacherUid); // Pass the teacher UID as receiver
            startActivity(chatIntent);
        });
        // Find the videoCall2 button by its ID
        Button videoCallButton = findViewById(R.id.videoCall);

// Set an OnClickListener for the button
        videoCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the student UID (username) from the GlobalStudentUid singleton
                String studentUid = GlobalStudentUid.getInstance().getStudentUid();
                // Retrieve the teacher UID (friendUsername) from the intent
                String teacherUid = getIntent().getStringExtra("uid");

                // Validate the UIDs
                if (studentUid == null || studentUid.isEmpty() || teacherUid == null || teacherUid.isEmpty()) {
                    Toast.makeText(StudentViewTeacherProfile.this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create an Intent to navigate to CallActivity
                Intent callIntent = new Intent(StudentViewTeacherProfile.this, CallActivity.class);
                callIntent.putExtra("username", studentUid); // Pass the student UID as username
                callIntent.putExtra("friendUsername", teacherUid); // Pass the teacher UID as friendUsername
                startActivity(callIntent);
            }
        });

    }
    // Method to fetch the student's Gmail
    private void fetchStudentGmail() {
        // Retrieve the student UID from the GlobalStudentUid singleton
        String studentUid = GlobalStudentUid.getInstance().getStudentUid();

        if (studentUid == null || studentUid.isEmpty()) {
            Toast.makeText(this, "Student UID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Query the Firebase database for the student's data
        studentsRef.child(studentUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the name and Gmail from the snapshot
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String gmail = dataSnapshot.child("gmail").getValue(String.class);

                    // Store the fetched data in the variables
                    studentName = name != null ? name : "N/A";
                    studentGmail = gmail != null ? gmail : "N/A";

                    // Log the fetched data for debugging purposes
                    Log.d("StudentData", "Fetched Name: " + studentName);
                    Log.d("StudentData", "Fetched Gmail: " + studentGmail);

                    // Optionally, display the fetched data in a Toast or UI component
                    Toast.makeText(StudentViewTeacherProfile.this, "Student Name: " + studentName, Toast.LENGTH_SHORT).show();
                    Toast.makeText(StudentViewTeacherProfile.this, "Student Gmail: " + studentGmail, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StudentViewTeacherProfile.this, "Student data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentViewTeacherProfile.this, "Failed to load student data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void payWithKhalti() {
        // Fetch the teacher's UID from the intent
        String teacherUid = getIntent().getStringExtra("uid");
        if (teacherUid == null || teacherUid.isEmpty()) {
            Toast.makeText(this, "Teacher UID not found. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch the teacher's data from Firebase
        teachersRef.child(teacherUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the teacher's name, email, and phone from Firebase
                    String teacherName = dataSnapshot.child("name").getValue(String.class);
                    String teacherEmail = dataSnapshot.child("gmail").getValue(String.class);
                    String teacherPhone = dataSnapshot.child("phoneNumber").getValue(String.class); // Assuming "phone" field exists in Firebase

                    // Proceed with Khalti payment initialization
                    initializeKhaltiPayment(teacherName, teacherEmail, teacherPhone);
                } else {
                    Toast.makeText(StudentViewTeacherProfile.this, "Teacher data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentViewTeacherProfile.this, "Failed to load teacher data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initializeKhaltiPayment(String teacherName, String teacherEmail, String teacherPhone) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://dev.khalti.com/api/v2/epayment/initiate/";
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("return_url", getString(R.string.khalti_redirect_url) + "/payment-success/");
            jsonBody.put("website_url", "https://example.com/");
            // TODO: Get amount from user, generate purchase_order_id and purchase_order_name dynamically
            jsonBody.put("amount", "1000");
            jsonBody.put("purchase_order_id", "Order01");
            jsonBody.put("purchase_order_name", "Test");

            // Populate customerInfo with teacher's details
            JSONObject customerInfo = new JSONObject();
            customerInfo.put("name", teacherName != null ? teacherName : "N/A");
            customerInfo.put("email", teacherEmail != null ? teacherEmail : "N/A");
            customerInfo.put("phone", teacherPhone != null ? teacherPhone : "N/A");

            jsonBody.put("customer_info", customerInfo);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String paymentUrl = response.optString("payment_url"); // Extract the payment URL
                        if (paymentUrl != null && !paymentUrl.isEmpty()) {
                            // Open the payment URL in the default browser
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                            startActivity(browserIntent);
                        } else {
                            Log.e("KhaltiError", "Payment URL not found in the response.");
                            Toast.makeText(StudentViewTeacherProfile.this, "Failed to initiate payment. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        if (error.networkResponse != null) {
                            Log.e("KhaltiError", "Status Code: " + error.networkResponse.statusCode);
                            Log.e("KhaltiError", "Response Data: " + new String(error.networkResponse.data));
                        } else {
                            Log.e("KhaltiError", "Error: " + error.getMessage());
                        }
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Key 7fba1a2e34324f9aa03d5e5710e01c1c");
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        queue.add(jsonObjectRequest);
    }



    private void fetchTeacherData(String uid) {
        teachersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Retrieve the teacher's data
                    String name = dataSnapshot.child("name").getValue(String.class);
                    String gmail = dataSnapshot.child("gmail").getValue(String.class);
                    String grade = dataSnapshot.child("category").getValue(String.class); // Assuming category includes grade
                    String subjects = dataSnapshot.child("subjects").getValue(String.class); // Add a "subjects" field in Firebase

                    // Populate the UI components
                    teacherName.setText(name != null ? name : "N/A");
                    teacherGmail.setText(gmail != null ? gmail : "N/A");
                    gradeCategory.setText("Grade: " + (grade != null ? grade : "N/A"));
                    subjectCategory.setText("Subject: " + (subjects != null ? subjects : "N/A"));
                } else {
                    Toast.makeText(StudentViewTeacherProfile.this, "Teacher data not found.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentViewTeacherProfile.this, "Failed to load teacher data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}