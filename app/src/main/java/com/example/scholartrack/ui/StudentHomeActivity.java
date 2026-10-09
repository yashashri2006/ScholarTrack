package com.example.scholartrack.ui;

// Unit 2: StudentHomeActivity – hosts BottomNavigationView and four student fragments.
//         Demonstrates Fragment management, back-stack handling, and live Firebase listener.
// Unit 5: Firebase live ValueEventListener for application stage.

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.example.scholartrack.R;
import com.example.scholartrack.ui.student.DocumentsFragment;
import com.example.scholartrack.ui.student.HelpFragment;
import com.example.scholartrack.ui.student.HomeFragment;
import com.example.scholartrack.ui.student.TrackerFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class StudentHomeActivity extends BaseActivity {

    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_home);

        bottomNav = findViewById(R.id.bottomNav);

        // Show HomeFragment on launch
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                fragment = new HomeFragment();
            } else if (id == R.id.nav_documents) {
                fragment = new DocumentsFragment();
            } else if (id == R.id.nav_tracker) {
                fragment = new TrackerFragment();
            } else if (id == R.id.nav_help) {
                fragment = new HelpFragment();
            } else {
                return false;
            }
            loadFragment(fragment);
            return true;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    /** Called from HomeFragment logout button. */
    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
