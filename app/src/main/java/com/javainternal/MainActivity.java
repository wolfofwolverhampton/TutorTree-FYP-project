package com.javainternal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.javainternal.Students.LoginForStudent;
import com.javainternal.Teachers.LoginForTeacher;

public class MainActivity extends AppCompatActivity {

    // Define required permissions
    private static final String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };
    private static final int requestCode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Check and request permissions if not granted
        if (!isPermissionGranted()) {
            askPermission();
        }

        // Find the Student Button by its ID
        Button studentButton = findViewById(R.id.studentButton);

        // Set an OnClickListener for the Student Button
        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginForStudent Activity
                Intent intent = new Intent(MainActivity.this, LoginForStudent.class);
                startActivity(intent);
            }
        });

        // Find the Teacher Button by its ID
        Button teacherButton = findViewById(R.id.teacherButton);

        // Set an OnClickListener for the Teacher Button
        teacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to LoginForTeacher Activity
                Intent intent = new Intent(MainActivity.this, LoginForTeacher.class);
                startActivity(intent);
            }
        });
    }

    // Method to check if permissions are granted
    private boolean isPermissionGranted() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    // Method to request permissions
    private void askPermission() {
        ActivityCompat.requestPermissions(this, permissions, requestCode);
    }

    // Handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @androidx.annotation.NonNull String[] permissions, @androidx.annotation.NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MainActivity.requestCode) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            if (!allPermissionsGranted) {
                Toast.makeText(this, "Permissions are required to use the app.", Toast.LENGTH_SHORT).show();
                finish(); // Close the app if permissions are denied
            }
        }
    }

}