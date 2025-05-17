package com.javainternal.Students;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.MainActivity;
import com.javainternal.R;
import com.javainternal.Services.UploadProfilePictureService;
import com.javainternal.Students.Model.StudentUserModel;
import com.javainternal.Teachers.TeacherSetting;
import com.javainternal.Utils.FirebaseUtils;
import com.javainternal.Utils.ProfilePictureUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public class StudentSetting extends AppCompatActivity {
    private TextView nameTextView, gmailTextView, guardianNameTextView, guardianGmailTextView, changePhotoText;
    private Button editButton, logoutButton, categoryButton;
    private DatabaseReference studentsRef;
    private String uid;
    private static final int PICK_IMAGE_REQUEST = 1;
    CircleImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_setting);

        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");
        studentsRef = FirebaseDatabase.getInstance().getReference("students").child(uid);

        nameTextView = findViewById(R.id.nameTextView2);
        gmailTextView = findViewById(R.id.gmailTextView2);
        guardianNameTextView = findViewById(R.id.guardianNameTextView2);
        guardianGmailTextView = findViewById(R.id.guardianGmailTextView2);

        editButton = findViewById(R.id.editButton2);
        logoutButton = findViewById(R.id.logoutButton);
        categoryButton = findViewById(R.id.categoryButton2);

        profileImageView = findViewById(R.id.profileImageView);
        changePhotoText = findViewById(R.id.changePhotoText);

        View.OnClickListener pickImageListener = v -> {
            Intent imageIntent = new Intent(Intent.ACTION_PICK);
            imageIntent.setType("image/*");
            startActivityForResult(imageIntent, PICK_IMAGE_REQUEST);
        };

        profileImageView.setOnClickListener(pickImageListener);
        changePhotoText.setOnClickListener(pickImageListener);


        fetchStudentInformation();

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentSetting.this, EditProfileStudent.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        categoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StudentSetting.this, CategoryStudent.class);
                intent.putExtra("uid", uid);
                startActivity(intent);
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserAuthContext.getInstance(StudentSetting.this).logoutAndRedirect(MainActivity.class);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStudentInformation();
    }

    private void fetchStudentInformation() {
        studentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    StudentUserModel student = dataSnapshot.getValue(StudentUserModel.class);
                    if (student.getProfilePicture() != null && !student.getProfilePicture().isEmpty()) {
                        Glide.with(StudentSetting.this)
                                .load(getString(R.string.backend_url) + student.getProfilePicture() + "?t=" + System.currentTimeMillis())
                                .into(profileImageView);
                    }

                    nameTextView.setText(student.getName());
                    gmailTextView.setText(student.getGmail());
                    guardianNameTextView.setText(student.getGuardianName());
                    guardianGmailTextView.setText(student.getGuardianGmail());
                } else {
                    Toast.makeText(StudentSetting.this, "Failed to load student information.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentSetting.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImageView = findViewById(R.id.profileImageView);
            profileImageView.setImageURI(imageUri);
            ProfilePictureUtils.uploadProfilePicture(getApplicationContext(), imageUri, uid, "students", new ProfilePictureUtils.OnProfileUploadListener() {
                @Override
                public void onUploadSuccess() {
                    fetchStudentInformation();
                }

                @Override
                public void onUploadFailure() {
                    Toast.makeText(getApplicationContext(), "Failed To Upload", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}