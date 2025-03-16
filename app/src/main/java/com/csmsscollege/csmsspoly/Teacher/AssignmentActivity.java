package com.csmsscollege.csmsspoly.Teacher;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Teacher.Adapter.Assignment;
import com.csmsscollege.csmsspoly.Teacher.Adapter.AssignmentAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AssignmentActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton fabAdd;
    private DatabaseReference databaseRef;
    private FirebaseStorage storageRef;
    private List<Assignment> assignmentList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private Uri selectedPdfUri;

    private final ActivityResultLauncher<Intent> pdfPicker = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedPdfUri = result.getData().getData();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assignment);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        recyclerView = findViewById(R.id.recyclerViewAssignments);
        fabAdd = findViewById(R.id.fab_add_assignment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        storageRef = FirebaseStorage.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("CSMSS Poly/Assignments");

        fabAdd.setOnClickListener(v -> showUploadDialog());
        loadAssignments();
    }

    private void showUploadDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_upload_assignment);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        Spinner spinnerYear = dialog.findViewById(R.id.spinnerYear);
        Spinner spinnerDepartment = dialog.findViewById(R.id.spinnerDepartment);
        TextInputEditText inputDescription = dialog.findViewById(R.id.etDescription);
        Button btnSelectPdf = dialog.findViewById(R.id.btnSelectPDF);
        Button btnUpload = dialog.findViewById(R.id.btnUpload);

        String[] years = {"Select Year", "FY", "SY", "TY"};
        String[] departments = {"Select Department", "CSE", "IT", "Mechanical", "Civil", "Electrical", "ENTC"};

        spinnerYear.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
        spinnerDepartment.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, departments));

        btnSelectPdf.setOnClickListener(v -> pickPdfFile());

        btnUpload.setOnClickListener(v -> {
            String selectedYear = spinnerYear.getSelectedItem().toString();
            String selectedDepartment = spinnerDepartment.getSelectedItem().toString();
            String description = inputDescription.getText().toString();

            if ("Select Year".equals(selectedYear) || "Select Department".equals(selectedDepartment) || description.isEmpty() || selectedPdfUri == null) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            uploadPdf(selectedYear, selectedDepartment, description, dialog);
        });

        dialog.show();
    }

    private void pickPdfFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        pdfPicker.launch(intent);
    }

    private void uploadPdf(String year, String department, String description, Dialog dialog) {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Assignment_" + timestamp + ".pdf";

        StorageReference fileRef = storageRef.getReference().child("CSMSS Poly/Assignments/" + year + "/" + department + "/" + fileName);

        if (selectedPdfUri != null) {
            progressDialog.setMessage("Uploading assignment...");
            progressDialog.show();

            fileRef.putFile(selectedPdfUri).addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> {
                Assignment assignment = new Assignment(year, department, description, downloadUrl.toString());
                databaseRef.child(year).child(department).push().setValue(assignment).addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Assignment Uploaded", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    loadAssignments();
                }).addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Upload failed!", Toast.LENGTH_SHORT).show();
                });
            })).addOnFailureListener(e -> {
                progressDialog.dismiss();
                Toast.makeText(this, "Failed to upload PDF", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadAssignments() {
        progressDialog.setMessage("Loading assignments...");
        progressDialog.show();

        databaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assignmentList.clear();
                for (DataSnapshot yearSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot deptSnapshot : yearSnapshot.getChildren()) {
                        for (DataSnapshot assignmentSnapshot : deptSnapshot.getChildren()) {
                            Assignment assignment = assignmentSnapshot.getValue(Assignment.class);
                            if (assignment != null) {
                                assignmentList.add(assignment);
                            }
                        }
                    }
                }
                recyclerView.setAdapter(new AssignmentAdapter(AssignmentActivity.this, assignmentList, assignment -> {
                    // Handle button click
                    return null;
                }));
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(AssignmentActivity.this, "Failed to load assignments", Toast.LENGTH_SHORT).show();
            }
        });
    }
}