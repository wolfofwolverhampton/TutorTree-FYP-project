package com.javainternal.Teachers;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.ApplicationContext.UserAuthContext;
import com.javainternal.MainActivity;
import com.javainternal.R;
import com.javainternal.Services.UploadProfilePictureService;
import com.javainternal.Students.CategoryStudent;
import com.javainternal.Students.EditProfileStudent;
import com.javainternal.Students.LoginForStudent;
import com.javainternal.Students.StudentSetting;
import com.javainternal.Teachers.Model.TeacherUserModel;
import com.javainternal.Utils.FirebaseUtils;
import com.javainternal.Utils.ProfilePictureUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

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

public class TeacherSetting extends AppCompatActivity {

    private CircleImageView profileImageView;
    private TextView nameTextView, gmailTextView, categoryTextView, averageRatingTextView, changeTeacherPhotoText;
    private RatingBar ratingBar;
    private DatabaseReference dbRef;
    private String teacherUid;
    private Button editProfileBtn, editCategoryBtn, logoutBtn;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher_setting);

        dbRef = FirebaseDatabase.getInstance().getReference();
        teacherUid = UserAuthContext.getInstance(this).getLoggedInPhone();

        profileImageView = findViewById(R.id.teacherProfileImageView);
        nameTextView = findViewById(R.id.teacherNameTextView);
        gmailTextView = findViewById(R.id.teacherGmailTextView);
        categoryTextView = findViewById(R.id.teacherCategoryTextView);
        ratingBar = findViewById(R.id.teacherRatingBar);
        averageRatingTextView = findViewById(R.id.teacherAverageRatingText);
        changeTeacherPhotoText = findViewById(R.id.changeTeacherPhotoText);

        editProfileBtn = findViewById(R.id.editTeacherProfileButton);
        logoutBtn = findViewById(R.id.teacherLogoutButton);

        ratingBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#FFA500")));

        loadTeacherProfile();

        loadTeacherRatings();

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TeacherSetting.this, EditProfileTeacher.class);
                intent.putExtra("uid", teacherUid);
                startActivity(intent);
            }
        });

        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserAuthContext.getInstance(TeacherSetting.this).logoutAndRedirect(MainActivity.class);
            }
        });

        View.OnClickListener pickImageListener = v -> {
            Intent imageIntent = new Intent(Intent.ACTION_PICK);
            imageIntent.setType("image/*");
            startActivityForResult(imageIntent, PICK_IMAGE_REQUEST);
        };

        profileImageView.setOnClickListener(pickImageListener);
        changeTeacherPhotoText.setOnClickListener(pickImageListener);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            profileImageView.setImageURI(imageUri);
            ProfilePictureUtils.uploadProfilePicture(getApplicationContext(), imageUri, teacherUid, "teachers", new ProfilePictureUtils.OnProfileUploadListener() {
                @Override
                public void onUploadSuccess() {
                    loadTeacherProfile();
                }

                @Override
                public void onUploadFailure() {
                    Toast.makeText(getApplicationContext(), "Failed To Upload", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTeacherProfile();
    }

    private void loadTeacherProfile() {
        dbRef.child("teachers").child(teacherUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String category = snapshot.child("category").getValue(String.class);
                String imageUrl = snapshot.child("profilePicture").getValue(String.class);

                nameTextView.setText(name != null ? name : "N/A");
                gmailTextView.setText(email != null ? email : "N/A");
                categoryTextView.setText(category != null ? category : "N/A");

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(TeacherSetting.this)
                            .load(getString(R.string.backend_url) + imageUrl + "?t=" + System.currentTimeMillis())
                            .placeholder(R.drawable.profile)
                            .into(profileImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TeacherSetting.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTeacherRatings() {
        dbRef.child("teachers").child(teacherUid).child("ratings")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        float total = 0;
                        int count = 0;

                        for (DataSnapshot ratingSnap : snapshot.getChildren()) {
                            Object starsObj = ratingSnap.child("stars").getValue();
                            if (starsObj instanceof Number) {
                                float stars = ((Number) starsObj).floatValue();
                                total += stars;
                                count++;
                            }
                        }

                        if (count > 0) {
                            float average = total / count;
                            ratingBar.setRating(average);
                            averageRatingTextView.setText(String.format("%.1f / 5", average));
                        } else {
                            ratingBar.setRating(0);
                            averageRatingTextView.setText("No ratings yet");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TeacherSetting.this, "Failed to load ratings", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
