package com.csmsscollege.csmsspoly.Student;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.csmsscollege.csmsspoly.Login;
import com.csmsscollege.csmsspoly.R;
import com.csmsscollege.csmsspoly.Teacher.ReportActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;

public class StudentDashboard extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private SharedPreferences sharedPreferences;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_dashboard);

        // Initialize Views
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigationView);

        CardView cardMyProfile = findViewById(R.id.cardMarkAttendance);
        CardView cardSubmitAssignments = findViewById(R.id.cardUploadAssignments);
        CardView cardNoticeByFaculty = findViewById(R.id.cardSendNotice);
        CardView cardMyAttendance = findViewById(R.id.cardAttendanceReport);

        // Set Click Listeners
        cardMyProfile.setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, MyProfile.class)));
        cardSubmitAssignments.setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, SubmitAssignments.class)));
        cardNoticeByFaculty.setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, NoticeByFaculty.class)));
        cardMyAttendance.setOnClickListener(v -> startActivity(new Intent(StudentDashboard.this, MyAttendance.class)));

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE);

        // Set up Toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Set up Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set up Navigation View Listener
        navigationView.setNavigationItemSelectedListener(this);

        // Load user email into header
        TextView userEmailTextView = navigationView.getHeaderView(0).findViewById(R.id.userEmail);
        String userEmail = sharedPreferences.getString("email", "user@example.com");
        userEmailTextView.setText(userEmail);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Do nothing if already on home
        } else if (id == R.id.nav_share) {
            shareApp();
        } else if (id == R.id.nav_feedback) {
            sendFeedback();
        } else if (id == R.id.nav_logout) {
            logoutUser();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    // Method to share app
    private void shareApp() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this awesome app!");
        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }

    // Method to send feedback via email
    private void sendFeedback() {
        Intent feedbackIntent = new Intent(Intent.ACTION_SENDTO);
        feedbackIntent.setData(Uri.parse("mailto:feedback@csmss.edu.in"));
        feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, "App Feedback");
        startActivity(feedbackIntent);
    }


    private void logoutUser() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        startActivity(new Intent(this, Login.class));
        finish();
    }
}
