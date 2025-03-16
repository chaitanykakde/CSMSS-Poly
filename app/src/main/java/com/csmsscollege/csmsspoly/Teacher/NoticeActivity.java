package com.csmsscollege.csmsspoly.Teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Teacher.Adapter.NoticeAdapter;
import com.csmsscollege.csmsspoly.Teacher.Model.Notice;
import com.csmsscollege.csmsspoly.databinding.ActivityNoticeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoticeActivity extends AppCompatActivity {

    private ActivityNoticeBinding binding;
    private DatabaseReference databaseRef;
    private List<Notice> noticeList;
    private NoticeAdapter noticeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNoticeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // Initialize Firebase Database Reference
        databaseRef = FirebaseDatabase.getInstance().getReference("CSMSS Poly").child("Notices");

        // Setup RecyclerView
        noticeList = new ArrayList<>();
        binding.rvNotices.setLayoutManager(new LinearLayoutManager(this));
        noticeAdapter = new NoticeAdapter(noticeList);
        binding.rvNotices.setAdapter(noticeAdapter);

        // Load existing notices
        loadNotices();

        // Send Notice Button Click
        binding.btnSendNotice.setOnClickListener(view -> sendNotice());
    }

    private void sendNotice() {
        String noticeText = binding.etNotice.getText().toString().trim();
        if (noticeText.isEmpty()) {
            Toast.makeText(this, "Please enter a notice!", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());

        Notice notice = new Notice(noticeText, currentDate, currentTime);
        String noticeKey = currentDate + " " + currentTime;

        // Store in Firebase
        databaseRef.child(noticeKey).setValue(notice).addOnSuccessListener(aVoid -> {
            Toast.makeText(NoticeActivity.this, "Notice sent successfully!", Toast.LENGTH_SHORT).show();
            binding.etNotice.setText(""); // Clear input field
        }).addOnFailureListener(e -> {
            Toast.makeText(NoticeActivity.this, "Failed to send notice!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadNotices() {
        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                noticeList.clear();
                for (DataSnapshot noticeSnapshot : snapshot.getChildren()) {
                    Notice notice = noticeSnapshot.getValue(Notice.class);
                    if (notice != null) {
                        noticeList.add(notice);
                    }
                }
                Collections.reverse(noticeList); // Show latest notice first
                noticeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NoticeActivity.this, "Failed to load notices", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
