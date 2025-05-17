package com.javainternal;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javainternal.ApplicationContext.UserAuthContext;

import java.util.HashMap;
import java.util.Map;

public class SystemFeedbackActivity extends AppCompatActivity {

    private TextInputLayout feedbackInputLayout;
    private TextInputEditText feedbackEditText;
    private MaterialButton submitFeedbackButton;
    private CircularProgressIndicator progressIndicator;

    private DatabaseReference feedbackDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_feedback);

        feedbackInputLayout = findViewById(R.id.feedbackInputLayout);
        feedbackEditText = findViewById(R.id.feedbackEditText);
        submitFeedbackButton = findViewById(R.id.submitFeedbackButton);
        progressIndicator = findViewById(R.id.progressIndicator);

        feedbackDatabase = FirebaseDatabase.getInstance().getReference("system_feedbacks");

        submitFeedbackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitFeedback();
            }
        });

        feedbackEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, android.view.KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    submitFeedback();
                    return true;
                }
                return false;
            }
        });
    }

    private void submitFeedback() {
        String feedback = feedbackEditText.getText() != null ? feedbackEditText.getText().toString().trim() : "";

        if (TextUtils.isEmpty(feedback)) {
            feedbackInputLayout.setError("Please enter your feedback");
            return;
        }

        feedbackInputLayout.setError(null);
        progressIndicator.setVisibility(View.VISIBLE);
        submitFeedbackButton.setEnabled(false);

        UserAuthContext authContext = UserAuthContext.getInstance(SystemFeedbackActivity.this);

        String userId = authContext.getLoggedInPhone();
        String userType = authContext.getLoggedInUserType();

        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("message", feedback);
        feedbackData.put("timestamp", System.currentTimeMillis());
        feedbackData.put("userId", userId);
        feedbackData.put("userType", userType);

        feedbackDatabase.push().setValue(feedbackData)
                .addOnSuccessListener(aVoid -> {
                    progressIndicator.setVisibility(View.GONE);
                    submitFeedbackButton.setEnabled(true);
                    feedbackEditText.setText("");
                    Snackbar.make(findViewById(R.id.feedbackContainer), "Feedback submitted. Thank you!", Snackbar.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    progressIndicator.setVisibility(View.GONE);
                    submitFeedbackButton.setEnabled(true);
                    Snackbar.make(submitFeedbackButton, "Failed to submit feedback: " + e.getMessage(), Snackbar.LENGTH_LONG).show();
                });
    }
}