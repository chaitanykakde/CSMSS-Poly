package com.csmsscollege.csmsspoly.Student;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Student.Adapter.AssignmentAdapter;
import com.csmsscollege.csmsspoly.Student.Model.Assignment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.*;
import com.google.firebase.storage.*;

import java.util.ArrayList;
import java.util.HashMap;

public class SubmitAssignments extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AssignmentAdapter adapter;
    private ArrayList<Assignment> assignmentList;
    private DatabaseReference databaseReference;
    private String studentYear, studentBranch, studentEmail;
    private static final int PICK_PDF_REQUEST = 1;
    private Uri pdfUri;
    private String selectedAssignmentId;

    private TextView tvFileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_submit_assignments);

        recyclerView = findViewById(R.id.recyclerViewAssignments);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        assignmentList = new ArrayList<>();

        // Get student details from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        studentYear = sharedPreferences.getString("year", "FY");
        studentBranch = sharedPreferences.getString("branch", "CSE");
        studentEmail = sharedPreferences.getString("email", "default@example.com");

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading assignments...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        adapter = new AssignmentAdapter(this, assignmentList, studentEmail);
        recyclerView.setAdapter(adapter);

        // Fetch assignments from Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("CSMSS Poly/Assignments/" + studentYear + "/" + studentBranch);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                assignmentList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Assignment assignment = ds.getValue(Assignment.class);
                    if (assignment != null) {
                        assignment.setId(ds.getKey());
                        assignmentList.add(assignment);
                    }
                }
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(SubmitAssignments.this, "Failed to load assignments.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showSubmitDialog(String assignmentId, String assignmentDescription) {
        selectedAssignmentId = assignmentId;
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_submit_assignment);

        // Make dialog non-cancelable
        dialog.setCancelable(false);

        Button selectFile = dialog.findViewById(R.id.btnSelectFile);
        Button submit = dialog.findViewById(R.id.btnSubmit);
        Button cancel = dialog.findViewById(R.id.btnCancel);
        TextView tvFileName = dialog.findViewById(R.id.tvFileName);

        // Select file and update file name
        selectFile.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            startActivityForResult(intent, PICK_PDF_REQUEST);
        });

        // Submit assignment only if a file is selected
        submit.setOnClickListener(v -> {
            if (pdfUri != null) {
                uploadPdfToFirebase(selectedAssignmentId, assignmentDescription);
                dialog.dismiss();

            } else {
                Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel button closes the dialog
        cancel.setOnClickListener(v -> dialog.dismiss());

        this.tvFileName = tvFileName; // Store reference to update file name when a file is selected
        dialog.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            pdfUri = data.getData();

            // Get the file name from the URI
            String fileName = getFileNameFromUri(pdfUri);
            if (tvFileName != null) {
                tvFileName.setText(fileName);
            }
        }
    }

    // Helper method to get file name from URI
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void uploadPdfToFirebase(String assignmentId,String assignmentDescription) {
        if (pdfUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String enrollmentNumber = sharedPreferences.getString("studentEnrollment", "UnknownEnrollment");
        String studentYear = sharedPreferences.getString("studentYear", "UnknownYear");
        String studentBranch = sharedPreferences.getString("studentBranch", "UnknownBranch");
        String studentEmail = sharedPreferences.getString("studentEmail", "UnknownEmail");

        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading assignment...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        // Generate unique ID using enrollment number + timestamp
        String uniqueId = enrollmentNumber + "_" + System.currentTimeMillis();

        // Firebase Storage Path
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(
                "CSMSS Poly/Answers/" + studentYear + "/" + studentBranch + "/" + uniqueId + "/" + studentEmail + ".pdf");

        storageReference.putFile(pdfUri).addOnSuccessListener(taskSnapshot ->
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {

                    // Firebase Database Path
                    DatabaseReference answerRef = FirebaseDatabase.getInstance().getReference("CSMSS Poly/Answers/" + uniqueId);

                    // Create HashMap with all required details
                    HashMap<String, Object> answerData = new HashMap<>();
                    answerData.put("answerUrl", uri.toString());
                    answerData.put("studentEmail", studentEmail);
                    answerData.put("studentEnrollment", enrollmentNumber);
                    answerData.put("year", studentYear);
                    answerData.put("branch", studentBranch);
                    answerData.put("assignmentDescription", assignmentDescription);
                    answerData.put("submissionTime", System.currentTimeMillis());

                    // Store in Firebase Database
                    answerRef.setValue(answerData).addOnSuccessListener(unused -> {
                        progressDialog.dismiss();
                        Toast.makeText(SubmitAssignments.this, "Assignment Submitted Successfully!", Toast.LENGTH_SHORT).show();
                        adapter.notifyDataSetChanged();
                    }).addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(SubmitAssignments.this, "Failed to submit: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

                }).addOnFailureListener(e ->
                        Toast.makeText(SubmitAssignments.this, "Failed to get file URL", Toast.LENGTH_SHORT).show()
                )
        ).addOnFailureListener(e ->

                Toast.makeText(SubmitAssignments.this, "File upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
    }


}
