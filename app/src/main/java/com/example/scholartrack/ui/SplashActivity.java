package com.example.scholartrack.ui;

// Unit 2: SplashActivity – reads Firebase login state, routes to correct home screen.
//         Demonstrates explicit Intent, fade-in animation, and Firebase Auth check.

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.FirebaseRepository;
import com.example.scholartrack.util.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class SplashActivity extends BaseActivity {

    private static final int SPLASH_DISPLAY_MS = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imgLogo   = findViewById(R.id.imgLogo);
        TextView  txtName   = findViewById(R.id.txtAppName);
        TextView  txtTagline = findViewById(R.id.txtTagline);
        ProgressBar progress = findViewById(R.id.progressSplash);

        // ── Fade-in animation (Unit 2: anim resource) ─────────────────────
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        imgLogo.startAnimation(fadeIn);
        imgLogo.setAlpha(1f);

        txtName.animate().alpha(1f).setStartDelay(300).setDuration(700).start();
        txtTagline.animate().alpha(1f).setStartDelay(600).setDuration(700).start();
        progress.animate().alpha(1f).setStartDelay(900).setDuration(400)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        // After animations settle, wait a moment then check login
                        progress.postDelayed(SplashActivity.this::checkLoginState,
                                SPLASH_DISPLAY_MS - 1000);
                    }
                }).start();
    }

    /**
     * Checks Firebase Auth state, then reads the user's role from the database
     * to route to StudentHomeActivity or CoordinatorHomeActivity.
     * Unit 5: Firebase Auth + Realtime Database read.
     */
    private void checkLoginState() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            navigateTo(LoginActivity.class);
            return;
        }

        FirebaseRepository.getInstance().readUserOnce(user.getUid(),
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        String role = snapshot.child(Constants.FIELD_ROLE).getValue(String.class);
                        if (Constants.ROLE_COORDINATOR.equals(role)) {
                            navigateTo(CoordinatorHomeActivity.class);
                        } else {
                            navigateTo(StudentHomeActivity.class);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Fallback to student if DB read fails
                        navigateTo(StudentHomeActivity.class);
                    }
                });
    }

    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
