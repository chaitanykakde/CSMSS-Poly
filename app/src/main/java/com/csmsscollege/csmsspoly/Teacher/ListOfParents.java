package com.csmsscollege.csmsspoly.Teacher;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Teacher.Adapter.ParentEmailAdapter;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ListOfParents extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ParentEmailAdapter adapter;
    private List<String> parentEmails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_parents);

        recyclerView = findViewById(R.id.recyclerViewParents);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        parentEmails = new ArrayList<>();
        adapter = new ParentEmailAdapter(this, parentEmails);
        recyclerView.setAdapter(adapter);

        fetchParentEmails();
    }

    private void fetchParentEmails() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Fetching parent emails...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("student_details.csv");

        storageRef.getBytes(1024 * 1024)
                .addOnSuccessListener(bytes -> {
                    progressDialog.dismiss();
                    String csvContent = new String(bytes);
                    parentEmails.addAll(extractParentEmailsFromCSV(csvContent));

                    adapter.notifyDataSetChanged(); // Refresh RecyclerView
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to fetch parent emails", Toast.LENGTH_SHORT).show();
                });
    }

    private List<String> extractParentEmailsFromCSV(String csvContent) {
        List<String> emails = new ArrayList<>();
        String[] lines = csvContent.split("\n");

        for (String line : lines) {
            String[] columns = line.split(",");
            if (columns.length > 2) { // Assuming parent email is in column 2
                emails.add(columns[8].trim());
            }
        }
        return emails;
    }
}
