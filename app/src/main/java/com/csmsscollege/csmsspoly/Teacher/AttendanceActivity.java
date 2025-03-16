package com.csmsscollege.csmsspoly.Teacher;

import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Teacher.Adapter.StudentAdapter;
import com.csmsscollege.csmsspoly.Teacher.Model.Student;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;

public class AttendanceActivity extends AppCompatActivity {

    private Button btnMarkAttendance, btnSubmitAttendance;
    private Spinner yearSpinner, departmentSpinner;
    private RecyclerView recyclerViewStudents;
    private StudentAdapter studentAdapter;
    private List<Student> studentList = new ArrayList<>();
    private String selectedYear, selectedDepartment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);

        btnMarkAttendance = findViewById(R.id.btnMarkAttendance);
        yearSpinner = findViewById(R.id.yearSpinner);
        departmentSpinner = findViewById(R.id.departmentSpinner);
        recyclerViewStudents = findViewById(R.id.recyclerViewStudents);
        btnSubmitAttendance = findViewById(R.id.btnSubmitAttendance);

        recyclerViewStudents.setLayoutManager(new LinearLayoutManager(this));
        studentAdapter = new StudentAdapter(studentList);
        recyclerViewStudents.setAdapter(studentAdapter);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        setupSpinners();

        btnMarkAttendance.setOnClickListener(v -> {
            if (selectedYear != null && selectedDepartment != null) {
                fetchStudents();
            } else {
                Toast.makeText(this, "Please select year and department!", Toast.LENGTH_SHORT).show();
            }
        });

        btnSubmitAttendance.setOnClickListener(v -> submitAttendance());
    }

    private void setupSpinners() {
        String[] years = {"Select Year", "FY", "SY", "TY"};
        String[] departments = {"Select Department", "CSE", "IT", "ENTC", "Electrical", "Mechanical", "Civil"};

        yearSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, years));
        departmentSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, departments));

        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear = position > 0 ? years[position] : null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        departmentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDepartment = position > 0 ? departments[position] : null;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void fetchStudents() {
        studentList.clear();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference("student_details.csv");

        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.ByteArrayInputStream(bytes)));
                reader.readLine();
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] tokens = line.split(",");
                    if (tokens.length >= 5 && tokens[4].equals(selectedYear) && tokens[3].equals(selectedDepartment)) {
                        studentList.add(new Student(tokens[0], tokens[2], tokens[8], false));
                    }
                }
                studentAdapter.notifyDataSetChanged();
                recyclerViewStudents.setVisibility(View.VISIBLE);
                btnSubmitAttendance.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                Toast.makeText(this, "Error reading student data!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e ->
                Toast.makeText(this, "Failed to load student data!", Toast.LENGTH_SHORT).show()
        );
    }

    private void submitAttendance() {
        if (selectedYear == null || selectedDepartment == null) {
            Toast.makeText(this, "Please select year and department!", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference()
                .child("CSMSS Poly").child("Attendance")
                .child(selectedDepartment).child(selectedYear).child(getCurrentDate());

        for (Student student : studentList) {
            String status = student.isPresent() ? "Present" : "Absent";
            Map<String, String> attendanceData = new HashMap<>();
            attendanceData.put("name", student.getName());
            attendanceData.put("enrollmentNumber", student.getEnrollmentNumber());
            attendanceData.put("parentEmail", student.getParentEmail());
            attendanceData.put("status", status);
            databaseRef.child(student.getEnrollmentNumber()).setValue(attendanceData);
        }

        Toast.makeText(this, "Attendance submitted successfully!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
    }
}
