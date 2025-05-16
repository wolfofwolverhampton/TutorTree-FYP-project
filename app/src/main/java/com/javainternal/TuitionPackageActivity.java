package com.javainternal;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.javainternal.Adapter.TuitionPackageAdapter;
import com.javainternal.Model.TuitionPackageModel;

import java.util.ArrayList;
import java.util.List;

public class TuitionPackageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private TuitionPackageAdapter adapter;
    private List<TuitionPackageModel> packageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tution_package);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        packageList = new ArrayList<>();
        packageList.add(new TuitionPackageModel("1 Month Package", 1, 2000));
        packageList.add(new TuitionPackageModel("3 Month Package", 3, 5999));
        packageList.add(new TuitionPackageModel("6 Month Package", 6, 17999));

        String teacherUid = getIntent().getStringExtra("teacherUid");
        String studentUid = getIntent().getStringExtra("studentUid");

        adapter = new TuitionPackageAdapter(this, packageList, teacherUid, studentUid);
        recyclerView.setAdapter(adapter);
    }
}