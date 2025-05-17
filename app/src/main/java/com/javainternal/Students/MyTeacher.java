package com.javainternal.Students;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.javainternal.Model.SubscriptionModel;
import com.javainternal.R;
import com.javainternal.Students.Adapter.MyTeacherAdapter;

import java.util.ArrayList;
import java.util.List;

public class MyTeacher extends AppCompatActivity {
    private RecyclerView recyclerView;
    private MyTeacherAdapter adapter;
    private final List<SubscriptionModel> allSubscriptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_teacher);

        recyclerView = findViewById(R.id.teacherRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MyTeacherAdapter(this, allSubscriptions);
        recyclerView.setAdapter(adapter);

        String currentStudentUid = GlobalStudentUid.getInstance().getStudentUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("subscriptions");
        Query query = ref.orderByChild("studentUid").equalTo(currentStudentUid);

        query.get()
                .addOnSuccessListener(dataSnapshot -> {
                    allSubscriptions.clear();
                    DatabaseReference teachersRef = FirebaseDatabase.getInstance().getReference("teachers");

                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        SubscriptionModel model = snap.getValue(SubscriptionModel.class);
                        if (model != null) {
                            allSubscriptions.add(model);

                            String teacherUid = model.getTeacherUid();
                            teachersRef.child(teacherUid).get().addOnSuccessListener(snapshot -> {
                                if (snapshot.exists()) {
                                    String teacherName = snapshot.child("name").getValue(String.class);
                                    model.setTeacherName(teacherName);
                                    adapter.notifyDataSetChanged();
                                }
                            }).addOnFailureListener(e -> {
                                Log.e("MyTeacher", "Failed to load teacher details", e);
                            });
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("MyTeacher", "Failed to load subscriptions", e);
                });
    }
}