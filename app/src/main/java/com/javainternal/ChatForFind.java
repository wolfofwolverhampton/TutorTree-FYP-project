package com.javainternal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.Adapter.MessagesAdapter;
import com.javainternal.Model.Message;
import com.javainternal.databinding.ActivityChatForFindBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatForFind extends AppCompatActivity {

    private ActivityChatForFindBinding binding;
    private MessagesAdapter adapter;
    private ArrayList<Message> messages;

    private String senderRoom, receiverRoom;
    private DatabaseReference database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatForFindBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get data from Intent
        String name = getIntent().getStringExtra("name");
        String senderUid = getIntent().getStringExtra("senderUid"); // Sender UID (student)
        String receiverUid = getIntent().getStringExtra("receiverUid"); // Receiver UID (teacher)

        // Validate the UIDs
        if (senderUid == null || senderUid.isEmpty() || receiverUid == null || receiverUid.isEmpty()) {
            Toast.makeText(this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set Toolbar title and enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(name); // Set the recipient's name as the title
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Enable the back button
        }

        // Initialize UI components
        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages, senderUid); // Pass the senderUid to the adapter
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        // Create chat rooms
        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference();

        // Fetch messages
        database.child("findChats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        messages.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Message message = dataSnapshot.getValue(Message.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Handle error
                    }
                });
        binding.shareBtn.setOnClickListener(v -> {
            // Define the message text for sharing
            String shareMessageText = "The file is sent.";

            // Validate UIDs
            if (senderUid == null || senderUid.isEmpty() || receiverUid == null || receiverUid.isEmpty()) {
                Toast.makeText(this, "UIDs not found. Cannot send the file.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a new Message object
            Message shareMessage = new Message(shareMessageText, senderUid, new Date().getTime());

            // Push the message to both senderRoom and receiverRoom in Firebase
            database.child("findChats")
                    .child(senderRoom)
                    .child("messages")
                    .push()
                    .setValue(shareMessage);

            database.child("findChats")
                    .child(receiverRoom)
                    .child("messages")
                    .push()
                    .setValue(shareMessage);

            // Notify the user that the file has been shared
            Toast.makeText(this, "File shared successfully!", Toast.LENGTH_SHORT).show();

            // Open the Share Menu
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "These are the files");
            intent.setType("text/plain");

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        // Send message
        binding.sendBtn.setOnClickListener(v -> {
            String messageText = binding.messageBox.getText().toString();
            if (messageText.isEmpty()) {
                return;
            }

            // Create message object
            Message message = new Message(messageText, senderUid, new Date().getTime());

            // Clear input box
            binding.messageBox.setText("");

            // Push message to both senderRoom and receiverRoom
            database.child("findChats")
                    .child(senderRoom)
                    .child("messages")
                    .push()
                    .setValue(message);
            database.child("findChats")
                    .child(receiverRoom)
                    .child("messages")
                    .push()
                    .setValue(message);
            // Send notification to the receiver
            sendNotification(receiverUid, "New Message", message.getMessage());
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the Toolbar
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.callBtn) {
            // Get the senderUid and receiverUid from the Intent
            String senderUid = getIntent().getStringExtra("senderUid");
            String receiverUid = getIntent().getStringExtra("receiverUid");
            String receiverName = getIntent().getStringExtra("name");

            // Validate UIDs
            if (senderUid == null || senderUid.isEmpty() || receiverUid == null || receiverUid.isEmpty()) {
                Toast.makeText(this, "UIDs not found. Cannot initiate call.", Toast.LENGTH_SHORT).show();
                return true;
            }

            // Navigate to CallActivity to initiate the call
            Intent intent = new Intent(ChatForFind.this, CallActivity.class);
            intent.putExtra("callerUid", senderUid); // Current user's UID
            intent.putExtra("receiverUid", receiverUid); // Other user's UID
            intent.putExtra("callerName", FirebaseAuth.getInstance().getCurrentUser().getDisplayName()); // Caller's name
            intent.putExtra("receiverName", receiverName); // Receiver's name
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Mimic the behavior of the system back button
        return true;
    }

    @Override
    public void onBackPressed() {
        // Add any custom logic here if needed
        super.onBackPressed(); // Call the superclass method to finish the activity
    }
    // Send a notification using FCM API
    private void sendNotification(String receiverUid, String title, String body) {
        // Fetch the receiver's FCM token
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(receiverUid);
        userRef.child("fcmToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String receiverToken = snapshot.getValue(String.class);
                if (receiverToken == null || receiverToken.isEmpty()) {
                    Log.w("FCM", "Receiver FCM token not found");
                    return;
                }

                // Create the notification payload
                JSONObject notification = new JSONObject();
                JSONObject notificationBody = new JSONObject();
                try {
                    notificationBody.put("title", title);
                    notificationBody.put("body", body);

                    notification.put("to", receiverToken); // Receiver's FCM token
                    notification.put("notification", notificationBody);

                    // Create a POST request to FCM
                    JsonObjectRequest request = new JsonObjectRequest(
                            Request.Method.POST,
                            "https://fcm.googleapis.com/fcm/send",
                            notification,
                            response -> Log.d("FCM", "Notification sent successfully"),
                            error -> Log.e("FCM", "Failed to send notification", error)
                    ) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Authorization", "key=YOUR_SERVER_KEY"); // Replace with your FCM server key
                            headers.put("Content-Type", "application/json");
                            return headers;
                        }
                    };

                    // Add the request to a Volley request queue
                    Volley.newRequestQueue(ChatForFind.this).add(request);
                } catch (JSONException e) {
                    Log.e("FCM", "JSON Exception", e);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FCM", "Failed to fetch receiver FCM token", error.toException());
            }
        });
    }
}