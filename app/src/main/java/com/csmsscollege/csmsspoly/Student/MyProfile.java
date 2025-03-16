package com.csmsscollege.csmsspoly.Student;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.csmsscollege.csmsspoly.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MyProfile extends AppCompatActivity {

    private TextView tvStudentName, tvEnrollmentNo, tvBranch, tvYear;
    private TextView tvStudentMobile, tvStudentEmail, tvParentMobile, tvParentEmail;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> onBackPressed());

        // Initialize TextViews
        tvStudentName = findViewById(R.id.tvStudentName);
        tvEnrollmentNo = findViewById(R.id.tvEnrollmentNo);
        tvBranch = findViewById(R.id.tvBranch);
        tvYear = findViewById(R.id.tvYear);
        tvStudentMobile = findViewById(R.id.tvStudentMobile);
        tvStudentEmail = findViewById(R.id.tvStudentEmail);
        tvParentMobile = findViewById(R.id.tvParentMobile);
        tvParentEmail = findViewById(R.id.tvParentEmail);

        // Initialize Progress Dialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        fetchStudentDetails();
    }

    private void fetchStudentDetails() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String loggedInEmail = sharedPreferences.getString("email", "");

        if (loggedInEmail == null || loggedInEmail.isEmpty()) {
            progressDialog.dismiss();
            Toast.makeText(this, "Student email not found!", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("student_details.csv");

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.ByteArrayInputStream(bytes)));
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length < 9) continue;

                    String email = parts[1]; // Student Email
                    if (email.equals(loggedInEmail)) {
                        tvEnrollmentNo.setText("Enrollment No: " + parts[0]);
                        tvStudentName.setText("Name: " + parts[2]);
                        tvBranch.setText("Branch: " + parts[3]);
                        tvYear.setText("Year: " + parts[4]);
                        tvStudentMobile.setText("Student Mobile: " + parts[5]);
                        tvStudentEmail.setText("Email: " + parts[1]);
                        tvParentMobile.setText("Parent Mobile: " + parts[7]);
                        tvParentEmail.setText("Parent Email: " + parts[8]);
                        break;
                    }
                }
                reader.close();
            } catch (Exception e) {
                Toast.makeText(this, "Error reading file", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to load data", Toast.LENGTH_SHORT).show();
        });
    }
}
