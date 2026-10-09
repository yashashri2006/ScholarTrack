package com.example.scholartrack.ui;

// Unit 2: Activity lifecycle logging – BaseActivity logs all 6 lifecycle callbacks
//         and applies edge-to-edge window insets so content never hides behind system bars.

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public abstract class BaseActivity extends AppCompatActivity {

    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = "ScholarTrack-" + getClass().getSimpleName();
        Log.d(TAG, "onCreate");
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        if (this instanceof SplashActivity) return;

        androidx.activity.EdgeToEdge.enable(this);
        android.view.View root = ((android.view.ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
        if (root != null) {
            androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                androidx.core.graphics.Insets bars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
                int extraTop = (int) (24 * getResources().getDisplayMetrics().density);
                v.setPadding(bars.left, bars.top + extraTop, bars.right, bars.bottom);
                return insets;
            });
        }
    }

    /**
     * Call this AFTER setContentView so the root view exists.
     * Applies window insets for edge-to-edge rendering.
     *
     * @param rootViewId The id of the root view (e.g. R.id.rootLayout).
     */
    protected void applyWindowInsets(int rootViewId) {
        // Obsolete: Now handled automatically in setContentView
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }
}
