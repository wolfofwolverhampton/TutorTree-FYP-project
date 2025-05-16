package com.javainternal.Utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.javainternal.R;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationUtils {
    public static void sendNotification(Context context, String senderUid, String receiverUid, String message) {
        String url = context.getString(R.string.backend_url) + "/send-notification";

        JSONObject payload = new JSONObject();
        try {
            payload.put("senderUid", senderUid);
            payload.put("receiverUid", receiverUid);
            payload.put("message", message);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                payload,
                response -> Log.d("Notification", "Sent successfully via Node.js"),
                error -> Log.e("Notification", "Failed to send via Node.js", error)
        );

        Volley.newRequestQueue(context).add(request);
    }
}

