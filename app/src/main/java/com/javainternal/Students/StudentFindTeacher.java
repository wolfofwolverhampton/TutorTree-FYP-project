package com.javainternal.Students;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.cooltechworks.views.shimmer.ShimmerRecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.javainternal.R;
import com.javainternal.Students.Adapter.StudentFindTeacherAdapter;
import com.javainternal.Teachers.Model.TeacherUserModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StudentFindTeacher extends AppCompatActivity {
    private ShimmerRecyclerView recyclerView;
    private DatabaseReference teachersRef;
    private ArrayList<TeacherUserModel> teachersList;
    private ArrayList<TeacherUserModel> filteredList;
    private StudentFindTeacherAdapter adapter;
    private EditText searchInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_find_teacher);

        teachersRef = FirebaseDatabase.getInstance().getReference("teachers");

        recyclerView = findViewById(R.id.recyclerView);
        searchInput = findViewById(R.id.searchInput);
        teachersList = new ArrayList<>();
        filteredList = new ArrayList<>();

        fetchTeachersFromFirebase();

        searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterTeachers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    private void fetchTeachersFromFirebase() {
        teachersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                teachersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    TeacherUserModel teacher = snapshot.getValue(TeacherUserModel.class);
                    if (teacher != null) {
                        teachersList.add(teacher);
                    }
                }

                filteredList.clear();
                filteredList.addAll(teachersList);

                adapter = new StudentFindTeacherAdapter(StudentFindTeacher.this, filteredList);
                recyclerView.setAdapter(adapter);
                recyclerView.showShimmerAdapter();
                recyclerView.hideShimmerAdapter();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                System.out.println("Failed to load teachers: " + error.getMessage());
            }
        });
    }

    private void filterTeachers(String query) {
        List<TeacherUserModel> filteredList = new ArrayList<>();
        for (TeacherUserModel teacher : teachersList) {
            if (teacher.getName().toLowerCase().contains(query.toLowerCase())
                    || teacher.getCategory().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(teacher);
            }
        }
        adapter.updateData(filteredList);
    }

//    private void filterTeachers(String query) {
//        String lowerQuery = query.toLowerCase(Locale.ROOT);
//        filteredList.clear();
//
//        for (TeacherUserModel teacher : teachersList) {
//            if (teacher.getName().toLowerCase().contains(lowerQuery) ||
//                    (teacher.getCategory() != null && teacher.getCategory().toLowerCase().contains(lowerQuery))) {
//                filteredList.add(teacher);
//            }
//        }
//
//        adapter.notifyDataSetChanged();
//    }
}
