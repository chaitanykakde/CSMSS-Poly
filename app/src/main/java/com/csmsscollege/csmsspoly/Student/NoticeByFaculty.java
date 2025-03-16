package com.csmsscollege.csmsspoly.Student;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Teacher.Adapter.NoticeAdapter;
import com.csmsscollege.csmsspoly.Teacher.Model.Notice;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class NoticeByFaculty extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoticeAdapter noticeAdapter;
    private DatabaseReference databaseRef;
    private ProgressDialog progressDialog;
    private List<Notice> noticeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_by_faculty);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        recyclerView = findViewById(R.id.recyclerViewNotices);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading Notices...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        databaseRef = FirebaseDatabase.getInstance().getReference("CSMSS Poly").child("Notices");

        fetchNotices();
    }

    private void fetchNotices() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                noticeList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot noticeSnap : snapshot.getChildren()) {
                        Notice notice = noticeSnap.getValue(Notice.class);
                        if (notice != null) {
                            noticeList.add(notice);
                        }
                    }
                    noticeAdapter = new NoticeAdapter(noticeList);
                    recyclerView.setAdapter(noticeAdapter);
                } else {
                    Toast.makeText(NoticeByFaculty.this, "No notices available", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(NoticeByFaculty.this, "Failed to load notices: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
