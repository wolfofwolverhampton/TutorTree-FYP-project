package com.javainternal.ApplicationContext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.Students.HomePageStudent;
import com.javainternal.Teachers.HomePageTeacher;

public class UserAuthContext {

    private static final String PREFS_NAME = "MyAppPrefs";
    private static final String KEY_PHONE = "loggedInUserPhone";
    private static final String KEY_TYPE = "loggedInUserType";

    private static UserAuthContext instance;

    private final DatabaseReference studentsRef;
    private final DatabaseReference teachersRef;
    private SharedPreferences prefs;
    private Context context;

    public interface LoginCallback {
        void onSuccess(String phoneNumber, String userType);
        void onFailure(String message);
    }

    private UserAuthContext(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.studentsRef = FirebaseDatabase.getInstance().getReference("students");
        this.teachersRef = FirebaseDatabase.getInstance().getReference("teachers");
    }

    public static synchronized UserAuthContext getInstance(Context context) {
        if (instance == null) {
            instance = new UserAuthContext(context);
        }
        return instance;
    }

    public void performLogin(String userType, String phoneNumber, String password, LoginCallback callback) {
        DatabaseReference userRef = "student".equals(userType) ? studentsRef : teachersRef;

        userRef.child(phoneNumber).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String storedPassword = snapshot.child("password").getValue(String.class);
                    if (storedPassword != null && storedPassword.equals(password)) {
                        saveLogin(phoneNumber, userType);
                        callback.onSuccess(phoneNumber, userType);
                    } else {
                        callback.onFailure("Invalid password. Please try again.");
                    }
                } else {
                    callback.onFailure("No account found for this phone number.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure("Database error: " + error.getMessage());
            }
        });
    }

    private void saveLogin(String phoneNumber, String userType) {
        prefs.edit()
                .putString(KEY_PHONE, phoneNumber)
                .putString(KEY_TYPE, userType)
                .apply();
    }

    public boolean isLoggedIn() {
        return prefs.contains(KEY_PHONE) && prefs.contains(KEY_TYPE);
    }

    public String getLoggedInPhone() {
        return prefs.getString(KEY_PHONE, null);
    }

    public String getLoggedInUserType() {
        return prefs.getString(KEY_TYPE, null);
    }

    public void logout() {
        String phone = getLoggedInPhone();
        prefs.edit().clear().apply();

        Log.d("Auth Context", phone);
        if (phone != null) {
            FirebaseDatabase.getInstance().getReference("user_tokens")
                    .child(phone)
                    .removeValue();

            context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .remove("fcm_token")
                    .apply();
        }

        FirebaseAuth.getInstance().signOut();
    }

    public void logoutAndRedirect(Class<?> targetActivity) {
        logout();
        Intent intent = new Intent(context, targetActivity);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public void redirectToHome(String phone, String userType) {
        Intent intent;
        if ("student".equals(userType)) {
            intent = new Intent(context, HomePageStudent.class);
        } else {
            intent = new Intent(context, HomePageTeacher.class);
        }
        intent.putExtra("uid", phone);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}
