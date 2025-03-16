package com.csmsscollege.csmsspoly;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.csmsscollege.csmsspoly.Parent.ParentDashboard;
import com.csmsscollege.csmsspoly.Student.StudentDashboard;
import com.csmsscollege.csmsspoly.Teacher.TeacherDashboard;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class Login extends AppCompatActivity {
    private Spinner userTypeSpinner;
    private TextInputEditText emailInput, passwordInput;
    private CheckBox termsCheckbox;
    private MaterialButton loginButton;
    private String selectedUserType = "Student";
    private final HashMap<String, String> studentCredentials = new HashMap<>();
    private final HashMap<String, String> parentCredentials = new HashMap<>();
    private Dialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userTypeSpinner = findViewById(R.id.spinnerUserType);
        emailInput = findViewById(R.id.etEmail);
        passwordInput = findViewById(R.id.etPassword);
        termsCheckbox = findViewById(R.id.cbTerms);
        loginButton = findViewById(R.id.btnLogin);

        // Setup Progress Dialog
        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.dialog_progress);
        progressDialog.setCancelable(false);

        // Spinner setup
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.user_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        userTypeSpinner.setAdapter(adapter);

        userTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // Login button click
        loginButton.setOnClickListener(v -> loginUser());
    }

    private void fetchCSVDataAndLogin(String email, String password) {
        progressDialog.show();

        StorageReference storageRef = FirebaseStorage.getInstance().getReference("student_details.csv");
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(bytes)));
                String line;
                reader.readLine(); // Skip header

                boolean isValidUser = false;
                String userTypeDetected = "";
                String studentEmail = "", enrollmentNumber = "", studentYear = "", studentBranch = "";

                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    if (data.length >= 10) { // Ensure there are enough columns

                        // Extract student details from CSV
                        studentEmail = data[1];  // Student Email
                        String studentPassword = data[6]; // Student Password
                        enrollmentNumber = data[2]; // Enrollment Number
                        studentYear = data[3]; // Student Year (FY, SY, TY, etc.)
                        studentBranch = data[4]; // Student Branch (CSE, IT, etc.)

                        String parentEmail = data[8];  // Parent Email
                        String parentPassword = data[9]; // Parent Password

                        if (selectedUserType.equals("Student") && email.equals(studentEmail) && password.equals(studentPassword)) {
                            isValidUser = true;
                            userTypeDetected = "Student";
                            break;
                        } else if (selectedUserType.equals("Parent") && email.equals(parentEmail) && password.equals(parentPassword)) {
                            isValidUser = true;
                            userTypeDetected = "Parent";
                            break;
                        }
                    }
                }
                reader.close();
                progressDialog.dismiss();

                if (isValidUser) {
                    // Store student details in SharedPreferences
                    saveUserSession(email, userTypeDetected, enrollmentNumber, studentYear, studentBranch, studentEmail);

                    // Redirect to respective dashboards
                    Intent intent = userTypeDetected.equals("Student") ?
                            new Intent(Login.this, StudentDashboard.class) :
                            new Intent(Login.this, ParentDashboard.class);

                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(Login.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                progressDialog.dismiss();
                e.printStackTrace();
                Toast.makeText(Login.this, "Error reading CSV data", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            progressDialog.dismiss();
            Toast.makeText(Login.this, "Failed to load credentials", Toast.LENGTH_SHORT).show();
        });
    }



    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!termsCheckbox.isChecked()) {
            Toast.makeText(this, "Please accept Terms & Conditions", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedUserType.equals("Teacher")) {
            if (email.equals("teacher@csmss.edu") && password.equals("123456")) {
                saveUserSession(email, "Teacher","d","d","d","df");
                startActivity(new Intent(this, TeacherDashboard.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid Teacher Credentials", Toast.LENGTH_SHORT).show();
            }
        } else {
            fetchCSVDataAndLogin(email, password);
        }
    }

    private void saveUserSession(String email, String role, String enrollmentNumber, String studentYear, String studentBranch, String studentEmail) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("email", email);  // Logged-in User Email (Student or Parent)
        editor.putString("role", role);  // "Student" or "Parent"
        editor.putString("studentEnrollment", enrollmentNumber);
        editor.putString("studentYear", studentYear);
        editor.putString("studentBranch", studentBranch);
        editor.putString("studentEmail", studentEmail);

        editor.apply();
    }

}
