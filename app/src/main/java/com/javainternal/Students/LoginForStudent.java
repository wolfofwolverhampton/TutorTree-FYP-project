package com.javainternal.Students;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.Teachers.HomePageTeacher;
import com.javainternal.Utils.FirebaseUtils;
import com.javainternal.databinding.ActivityLoginForStudentsBinding;

public class LoginForStudent extends AppCompatActivity {

    private ActivityLoginForStudentsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserAuthContext authContext = UserAuthContext.getInstance(getApplicationContext());

        if (authContext.isLoggedIn()) {
            authContext.redirectToHome(authContext.getLoggedInPhone(), authContext.getLoggedInUserType());
            finish();
            return;
        }

        binding = ActivityLoginForStudentsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginForStudent.this, OTPNumberInputStudent.class);
                startActivity(intent);
            }
        });

        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = binding.phoneNumberEditText.getText().toString().trim();
                String password = binding.passwordEditText.getText().toString().trim();

                if (phoneNumber.isEmpty()) {
                    Toast.makeText(LoginForStudent.this, "Please enter your phone number.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (password.isEmpty()) {
                    Toast.makeText(LoginForStudent.this, "Please enter your password.", Toast.LENGTH_SHORT).show();
                    return;
                }

                authContext.performLogin("student", phoneNumber, password, new UserAuthContext.LoginCallback() {
                    @Override
                    public void onSuccess(String phoneNumber, String userType) {
                        Toast.makeText(LoginForStudent.this, "Login successful as " + userType + "!", Toast.LENGTH_SHORT).show();
                        FirebaseUtils.saveFcmToken(getApplicationContext(), phoneNumber);
                        authContext.redirectToHome(phoneNumber, userType);
                        finish();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(LoginForStudent.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}