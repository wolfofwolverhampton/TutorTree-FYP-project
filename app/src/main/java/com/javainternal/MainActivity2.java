package com.javainternal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity2 extends AppCompatActivity {

    private static final String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private static final int requestCode = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Find views using findViewById
        EditText usernameEdit = findViewById(R.id.usernameEdit);
        Button loginBtn = findViewById(R.id.loginBtn);

        // Check and request permissions
        if (!isPermissionGranted()) {
            askPermission();
        }

        // Set click listener for the login button
        loginBtn.setOnClickListener(v -> {
            String username = usernameEdit.getText().toString();
            Intent intent = new Intent(this, CallActivity.class);
            intent.putExtra("username", username); // Pass the username to CallActivity
            startActivity(intent);
        });
    }

    private void askPermission() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    private boolean isPermissionGranted() {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}