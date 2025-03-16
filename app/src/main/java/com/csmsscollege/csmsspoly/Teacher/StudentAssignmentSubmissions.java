package com.csmsscollege.csmsspoly.Teacher;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Teacher.Adapter.SubmissionAdapter;
import com.csmsscollege.csmsspoly.Teacher.Model.Submission;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class StudentAssignmentSubmissions extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SubmissionAdapter submissionAdapter;
    private List<Submission> submissionList;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_assignment_submissions);

        // Get assignment metadata from Intent
        String assignmentDescription = getIntent().getStringExtra("description");

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewSubmissions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        submissionList = new ArrayList<>();
        submissionAdapter = new SubmissionAdapter(this, submissionList);
        recyclerView.setAdapter(submissionAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
        // Fetch submissions from Firebase
        fetchSubmissions(assignmentDescription);
    }

    private void fetchSubmissions(String assignmentDescription) {
        databaseReference = FirebaseDatabase.getInstance().getReference("CSMSS Poly").child("Answers");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                submissionList.clear();
                for (DataSnapshot submissionSnapshot : snapshot.getChildren()) {
                    Submission submission = submissionSnapshot.getValue(Submission.class);
                    if (submission != null && submission.getAssignmentDescription().equals(assignmentDescription)) {
                        submissionList.add(submission);
                    }
                }
                submissionAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentAssignmentSubmissions.this, "Failed to load submissions", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
