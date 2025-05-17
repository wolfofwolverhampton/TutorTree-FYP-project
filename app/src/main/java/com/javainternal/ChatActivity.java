package com.javainternal;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.Adapter.MessagesAdapter;
import com.javainternal.Model.Message;
import com.javainternal.Utils.NotificationUtils;
import com.javainternal.databinding.ActivityChatForFindBinding;

import java.util.ArrayList;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(Color.BLACK);
        setSupportActionBar(toolbar);

        String name = getIntent().getStringExtra("name");
        String senderUid = getIntent().getStringExtra("senderUid");
        String receiverUid = getIntent().getStringExtra("receiverUid");

        if (senderUid == null || senderUid.isEmpty() || receiverUid == null || receiverUid.isEmpty()) {
            Toast.makeText(this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        messages = new ArrayList<>();
        adapter = new MessagesAdapter(this, messages, senderUid);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        senderRoom = senderUid + receiverUid;
        receiverRoom = receiverUid + senderUid;

        database = FirebaseDatabase.getInstance().getReference();

        database.child("findChats")
                .child(senderRoom)
                .child("messages")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int lastVisiblePosition = ((LinearLayoutManager) binding.recyclerView.getLayoutManager())
                                .findLastVisibleItemPosition();

                        boolean isAtBottom = lastVisiblePosition >= messages.size() - 2;

                        messages.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Message message = dataSnapshot.getValue(Message.class);
                            if (message != null) {
                                messages.add(message);
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (isAtBottom) {
                            binding.recyclerView.smoothScrollToPosition(messages.size() - 1);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
        binding.shareBtn.setOnClickListener(v -> {
            String shareMessageText = "The file is sent.";

            if (senderUid == null || senderUid.isEmpty() || receiverUid == null || receiverUid.isEmpty()) {
                Toast.makeText(this, "UIDs not found. Cannot send the file.", Toast.LENGTH_SHORT).show();
                return;
            }

            Message shareMessage = new Message(shareMessageText, senderUid, new Date().getTime());
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

            Toast.makeText(this, "File shared successfully!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "These are the files");
            intent.setType("text/plain");

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });

        binding.sendBtn.setOnClickListener(v -> {
            String messageText = binding.messageBox.getText().toString();
            if (messageText.isEmpty()) {
                return;
            }

            Message message = new Message(messageText, senderUid, new Date().getTime());

            binding.messageBox.setText("");

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
            NotificationUtils.sendNotification(getApplicationContext(), senderUid, receiverUid, message.getMessage());
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.callBtn) {
            String senderUid = getIntent().getStringExtra("senderUid");
            String receiverUid = getIntent().getStringExtra("receiverUid");

            if (senderUid == null || senderUid.isEmpty() || receiverUid == null || receiverUid.isEmpty()) {
                Toast.makeText(this, "UIDs not found. Cannot initiate call.", Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent intent = new Intent(ChatActivity.this, CallActivity.class);
            intent.putExtra("username", senderUid);
            intent.putExtra("friendUsername", receiverUid);
            startActivity(intent);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}