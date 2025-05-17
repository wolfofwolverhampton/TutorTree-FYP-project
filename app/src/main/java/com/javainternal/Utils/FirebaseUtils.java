package com.javainternal.Utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FirebaseUtils {
    public static void saveFcmToken(Context context, String userId) {
        if (userId == null) return;

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    FirebaseDatabase.getInstance().getReference("user_tokens")
                            .child(userId)
                            .setValue(token);

                    context.getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
                            .edit()
                            .putString("fcm_token", token)
                            .apply();
                });
    }

    public static void checkIfTokenValid(Context context, String userId, Runnable onValid, Runnable onInvalid) {
        if (userId == null) return;

        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(currentToken -> {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("user_tokens").child(userId);
            ref.get().addOnSuccessListener(snapshot -> {
                String storedToken = snapshot.getValue(String.class);

                if (storedToken != null && storedToken.equals(currentToken)) {
                    onValid.run();
                } else {
                    onInvalid.run();
                }
            });
        });
    }
}
