package com.javainternal;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.databinding.ActivityCallBinding;

import java.util.UUID;

public class CallActivity extends AppCompatActivity {

    private static final String TAG = "CallActivity";
    private ActivityCallBinding binding;
    private String username = "";
    private String friendsUsername = "";
    private boolean isPeerConnected = false;

    private DatabaseReference firebaseRef;
    private boolean isAudio = true;
    private boolean isVideo = true;
    private String uniqueId = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCallBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        firebaseRef = FirebaseDatabase.getInstance().getReference("users");

        username = getIntent().getStringExtra("username");
        friendsUsername = getIntent().getStringExtra("friendUsername");

        Log.d(TAG, "Starting call activity with username: " + username + ", friend: " + friendsUsername);

        if (username == null || username.isEmpty() || friendsUsername == null || friendsUsername.isEmpty()) {
            Toast.makeText(this, "UIDs not found. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.inputLayout.setVisibility(View.GONE);

        binding.toggleAudioBtn.setOnClickListener(v -> {
            isAudio = !isAudio;
            callJavascriptFunction("javascript:toggleAudio(\"" + isAudio + "\")");
            binding.toggleAudioBtn.setImageResource(isAudio ? R.drawable.baseline_mic_24 : R.drawable.baseline_mic_off_24);
        });

        binding.toggleVideoBtn.setOnClickListener(v -> {
            isVideo = !isVideo;
            callJavascriptFunction("javascript:toggleVideo(\"" + isVideo + "\")");
            binding.toggleVideoBtn.setImageResource(isVideo ? R.drawable.baseline_videocam_24 : R.drawable.baseline_videocam_off_24);
        });

        binding.flipCameraBtn.setOnClickListener(v -> {
            callJavascriptFunction("javascript:window.flipCamera()");
        });

        String filePath = "file:///android_asset/call.html";
        binding.webView.loadUrl(filePath);

        setupWebView();
    }

    private void setupWebView() {
        binding.webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        binding.webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebView", consoleMessage.message() + " -- From line " +
                        consoleMessage.lineNumber() + " of " + consoleMessage.sourceId());
                return true;
            }
        });

        WebSettings webSettings = binding.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        binding.webView.addJavascriptInterface(new CallJavascriptInterface(this), "Android");

        loadVideoCall();
    }

    private void loadVideoCall() {
        String filePath = "file:///android_asset/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "WebView page finished loading");
                initializePeer();
            }
        });
    }

    private void initializePeer() {
        uniqueId = getUniqueID();
        Log.d(TAG, "Initializing peer with ID: " + uniqueId);
        callJavascriptFunction("javascript:init(\"" + uniqueId + "\")");

        firebaseRef.child(username).child("isAvailable").setValue(true);
        firebaseRef.child(username).child("connId").setValue(uniqueId);

        setupIncomingCallListener();

        setupCallControls();
    }

    private void setupIncomingCallListener() {
        firebaseRef.child(username).child("incoming").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String caller = snapshot.getValue(String.class);
                Log.d(TAG, "Incoming call data changed: " + caller);

                if (caller != null && !caller.isEmpty()) {
                    onCallRequest(caller);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }

    private void setupCallControls() {
        binding.callControlLayout.setVisibility(View.VISIBLE);
        binding.callActionBtn.setEnabled(false);
        binding.callActionBtn.setText("Connecting...");

        binding.callActionBtn.setOnClickListener(v -> {
            if (isPeerConnected) {
                sendCallRequest();
            } else {
                Toast.makeText(this, "Still connecting to server...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendCallRequest() {
        if (!isPeerConnected) {
            Toast.makeText(this, "You are not connected. Please wait...", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Sending call request to: " + friendsUsername);

        firebaseRef.child(username).child("connId").setValue(uniqueId);

        firebaseRef.child(friendsUsername).child("incoming").setValue(username);

        listenForFriendAvailability();

        binding.callActionBtn.setVisibility(View.GONE);
    }

    private void listenForFriendAvailability() {
        firebaseRef.child(friendsUsername).child("isAvailable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                Boolean isAvailable = snapshot.getValue(Boolean.class);
                Log.d(TAG, "Friend availability: " + isAvailable);

                if (isAvailable != null && isAvailable) {
                    listenForConnId();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    private void listenForConnId() {
        firebaseRef.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String connId = snapshot.getValue(String.class);
                Log.d(TAG, "Friend's connection ID: " + connId);

                if (connId == null || connId.isEmpty()) return;

                switchToControls();
                callJavascriptFunction("javascript:startCall(\"" + connId + "\")");
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    private void onCallRequest(String caller) {
        Log.d(TAG, "Call request from: " + caller);

        binding.callLayout.setVisibility(View.VISIBLE);
        binding.incomingCallTxt.setText(caller + " is calling...");

        binding.acceptBtn.setOnClickListener(v -> {
            Log.d(TAG, "Call accepted, setting connId: " + uniqueId);

            firebaseRef.child(username).child("connId").setValue(uniqueId);
            firebaseRef.child(username).child("isAvailable").setValue(true);

            binding.callLayout.setVisibility(View.GONE);

            switchToControls();

            friendsUsername = caller;
        });

        binding.rejectBtn.setOnClickListener(v -> {
            Log.d(TAG, "Call rejected");

            firebaseRef.child(username).child("incoming").setValue(null);

            binding.callLayout.setVisibility(View.GONE);
        });
    }

    private void switchToControls() {
        binding.inputLayout.setVisibility(View.GONE);
        binding.callControlLayout.setVisibility(View.VISIBLE);
        binding.callActionBtn.setVisibility(View.GONE);
    }

    private String getUniqueID() {
        return UUID.randomUUID().toString();
    }

    private void callJavascriptFunction(String functionString) {
        binding.webView.post(() -> binding.webView.evaluateJavascript(functionString, null));
    }

    public void onPeerConnected() {
        isPeerConnected = true;
        Log.d(TAG, "Peer connection established");
        Toast.makeText(this, "Connected to server", Toast.LENGTH_SHORT).show();

        runOnUiThread(() -> {
            binding.callActionBtn.setText("Start Call");
            binding.callActionBtn.setEnabled(true);
        });
    }

    public void onPeerError(String error) {
        Log.e(TAG, "Peer error: " + error);
        Toast.makeText(this, "Connection error: " + error, Toast.LENGTH_LONG).show();
    }

    public void onMediaError(String error) {
        Log.e(TAG, "Media error: " + error);
        Toast.makeText(this, "Media error: " + error, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        if (username != null && !username.isEmpty()) {
            firebaseRef.child(username).child("incoming").setValue(null);
            firebaseRef.child(username).child("isAvailable").setValue(false);
            firebaseRef.child(username).child("connId").setValue(null);
        }
        binding.webView.loadUrl("about:blank");
        super.onDestroy();
    }
}