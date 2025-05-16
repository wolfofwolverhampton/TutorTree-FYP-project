package com.javainternal.Teachers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.R;
import com.javainternal.Utils.FirebaseUtils;

public class LoginForTeacher extends AppCompatActivity {

    private EditText phoneNumberEditText, passwordEditText;
    private Button loginButton, signUpButton;

    private DatabaseReference teachersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_for_teacher);

        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signUpButton = findViewById(R.id.signUpButton);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                if (phoneNumber.isEmpty()) {
                    Toast.makeText(LoginForTeacher.this, "Please enter your phone number.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(LoginForTeacher.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                    return;
                }

                authenticateUser(phoneNumber, password);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginForTeacher.this, OTPNumberInputTeacher.class);
                startActivity(intent);
            }
        });
    }

    private void authenticateUser(String phoneNumber, String password) {
        teachersRef.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String storedPassword = dataSnapshot.child("password").getValue(String.class);

                    if (storedPassword != null && storedPassword.equals(password)) {
                        Toast.makeText(LoginForTeacher.this, "Login successful!", Toast.LENGTH_SHORT).show();
                        FirebaseUtils.saveFcmToken(phoneNumber);

                        GlobalTeacherUid.getInstance().setTeacherUid(phoneNumber);
                        Intent intent = new Intent(LoginForTeacher.this, HomePageTeacher.class);
                        intent.putExtra("uid", phoneNumber); // Pass the phone number as uid
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginForTeacher.this, "Invalid phone number or password.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginForTeacher.this, "No account found with this phone number.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(LoginForTeacher.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}