package com.csmsscollege.csmsspoly;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.csmsscollege.csmsspoly.Parent.ParentDashboard;
import com.csmsscollege.csmsspoly.Student.StudentDashboard;
import com.csmsscollege.csmsspoly.Teacher.TeacherDashboard;

public class SplashScreen extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Get stored user session
        SharedPreferences sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);
        String userEmail = sharedPreferences.getString("email", null);
        String userRole = sharedPreferences.getString("role", null);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (userEmail != null && !userEmail.isEmpty() && userRole != null && !userRole.isEmpty()) {
                // User is logged in, navigate to the respective dashboard
                Intent intent;
                switch (userRole) {
                    case "Teacher":
                        intent = new Intent(SplashScreen.this, TeacherDashboard.class);
                        break;
                    case "Student":
                        intent = new Intent(SplashScreen.this, StudentDashboard.class);
                        break;
                    case "Parent":
                        intent = new Intent(SplashScreen.this, ParentDashboard.class);
                        break;
                    default:
                        intent = new Intent(SplashScreen.this, Login.class);
                        break;
                }
                startActivity(intent);
            } else {
                // No user is logged in, go to LoginActivity
                startActivity(new Intent(SplashScreen.this, Login.class));
            }
            finish(); // Close SplashScreen activity
        }, 1000);
    }
}
