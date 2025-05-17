package com.javainternal.Teachers;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.javainternal.R;
import com.javainternal.Teachers.Model.TeacherUserModel;

import java.util.HashMap;
import java.util.Map;

public class EditProfileTeacher extends AppCompatActivity {

    private EditText nameEditText, gmailEditText, editCategory;
    private Button saveButton;
    private DatabaseReference dbRef;
    private String teacherUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile_teacher);

        nameEditText = findViewById(R.id.editName);
        gmailEditText = findViewById(R.id.editGmail);
        editCategory = findViewById(R.id.editCategory);

        saveButton = findViewById(R.id.saveButton);

        teacherUid = GlobalTeacherUid.getInstance().getTeacherUid();
        dbRef = FirebaseDatabase.getInstance().getReference("teachers").child(teacherUid);

        loadTeacherProfile();

        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void loadTeacherProfile() {
        dbRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                TeacherUserModel teacher = snapshot.getValue(TeacherUserModel.class);
                if (teacher != null) {
                    nameEditText.setText(teacher.getName());
                    gmailEditText.setText(teacher.getGmail());
                    editCategory.setText(teacher.getCategory());
                }
            }
        }).addOnFailureListener(e ->
                Toast.makeText(EditProfileTeacher.this, "Failed to load profile", Toast.LENGTH_SHORT).show()
        );
    }

    private void saveProfile() {
        String name = nameEditText.getText().toString().trim();
        String gmail = gmailEditText.getText().toString().trim();
        String category = editCategory.getText().toString().trim();

        if (name.isEmpty() || gmail.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill out all the fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("gmail", gmail);
        updates.put("category", category);

        dbRef.updateChildren(updates)
                .addOnSuccessListener(unused -> Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        finish();
    }
}
