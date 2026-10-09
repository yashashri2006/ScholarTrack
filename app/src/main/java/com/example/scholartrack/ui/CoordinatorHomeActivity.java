package com.example.scholartrack.ui;

// Unit 2: CoordinatorHomeActivity – hosts BottomNavigationView for coordinator role.

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import com.example.scholartrack.R;
import com.example.scholartrack.ui.coordinator.CoordinatorHelpFragment;
import com.example.scholartrack.ui.coordinator.StudentsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class CoordinatorHomeActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinator_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavCoordinator);

        if (savedInstanceState == null) {
            loadFragment(new StudentsFragment());
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment fragment;
            int id = item.getItemId();
            if (id == R.id.nav_students) {
                fragment = new StudentsFragment();
            } else if (id == R.id.nav_help) {
                fragment = new CoordinatorHelpFragment();
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
                .replace(R.id.fragmentContainerCoordinator, fragment)
                .commit();
    }

    public void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
