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
import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.R;
import com.javainternal.Students.GlobalStudentUid;
import com.javainternal.Students.LoginForStudent;
import com.javainternal.Utils.FirebaseUtils;

public class LoginForTeacher extends AppCompatActivity {

    private EditText phoneNumberEditText, passwordEditText;
    private Button loginButton, signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_for_teacher);

        UserAuthContext authContext = UserAuthContext.getInstance(getApplicationContext());

        if (authContext.isLoggedIn()) {
            authContext.redirectToHome(authContext.getLoggedInPhone(), authContext.getLoggedInUserType());
            finish();
            return;
        }

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

                authContext.performLogin("teacher", phoneNumber, password, new UserAuthContext.LoginCallback() {
                    @Override
                    public void onSuccess(String phoneNumber, String userType) {
                        Toast.makeText(LoginForTeacher.this, "Login successful as " + userType + "!", Toast.LENGTH_SHORT).show();
                        FirebaseUtils.saveFcmToken(getApplicationContext(), phoneNumber);
                        authContext.redirectToHome(phoneNumber, userType);
                        finish();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(LoginForTeacher.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
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
}