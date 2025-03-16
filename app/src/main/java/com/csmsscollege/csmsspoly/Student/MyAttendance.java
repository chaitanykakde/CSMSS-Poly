package com.csmsscollege.csmsspoly.Student;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.csmsscollege.csmsspoly.R;
import com.google.firebase.database.*;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class MyAttendance extends AppCompatActivity {

    private TextView tvTotalLectures, tvCompletedLectures, tvAttendancePercentage;
    private DatabaseReference databaseReference;
    private String studentEmail;
    private AlertDialog progressDialog; // Progress dialog

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_attendance);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());



        // Initialize UI Components
        tvTotalLectures = findViewById(R.id.tvTotalLectures);
        tvCompletedLectures = findViewById(R.id.tvCompletedLectures);
        tvAttendancePercentage = findViewById(R.id.tvAttendancePercentage);

        // Initialize Progress Dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(R.layout.dialog_progress);
        builder.setCancelable(false);
        progressDialog = builder.create();

        // Get student email from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        studentEmail = sharedPreferences.getString("studentEmail", "");

        if (studentEmail != null && !studentEmail.isEmpty()) {
            fetchStudentDetails();
        } else {
            Toast.makeText(this, "Student email not found!", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchStudentDetails() {
        progressDialog.show(); // Show loading dialog
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("student_details.csv");

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            try {
                InputStream inputStream = new java.io.ByteArrayInputStream(bytes);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

                String studentYear = "";
                String studentDepartment = "";
                String studentEnrollmentNumber = "";

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length >= 4 && data[1].equals(studentEmail)) {
                        studentYear = data[4].trim();
                        studentDepartment = data[3].trim();
                        studentEnrollmentNumber = data[0].trim();
                        break;
                    }
                }

                if (!studentYear.isEmpty() && !studentDepartment.isEmpty()) {
                    fetchAttendanceData(studentYear, studentDepartment, studentEnrollmentNumber);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Student details not found!", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                progressDialog.dismiss();
                Toast.makeText(this, "Error reading student details", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(this, "Failed to load student details", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchAttendanceData(String year, String department, String studentEnrollmentNumber) {
        databaseReference = FirebaseDatabase.getInstance().getReference()
                .child("CSMSS Poly")
                .child("Attendance")
                .child(department)
                .child(year);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int totalLectures = 0;
                int presentCount = 0;

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    DataSnapshot studentSnapshot = dateSnapshot.child(studentEnrollmentNumber);
                    if (studentSnapshot.exists()) {
                        StudentAttendance studentData = studentSnapshot.getValue(StudentAttendance.class);
                        if (studentData != null) {
                            totalLectures++;
                            if ("Present".equals(studentData.status)) {
                                presentCount++;
                            }
                        }
                    }
                }

                progressDialog.dismiss(); // Dismiss loading dialog when data is loaded

                // Calculate attendance percentage
                double attendancePercentage = (totalLectures > 0) ? ((double) presentCount / totalLectures) * 100 : 0.0;

                // Update UI
                tvTotalLectures.setText("Total Lectures: " + totalLectures);
                tvCompletedLectures.setText("Completed Lectures: " + presentCount);
                tvAttendancePercentage.setText(String.format("Attendance Percentage: %.2f%%", attendancePercentage));
            }

            @Override
            public void onCancelled(DatabaseError error) {
                progressDialog.dismiss(); // Dismiss on error
                Toast.makeText(MyAttendance.this, "Failed to load attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class StudentAttendance {
        public String name;
        public String enrollmentNumber;
        public String parentEmail;
        public String status; // "Present" or "Absent"

        public StudentAttendance() {
            // Default constructor required for Firebase
        }
    }
}
