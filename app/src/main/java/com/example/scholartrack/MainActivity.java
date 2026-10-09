package com.example.scholartrack;

// Unit 2: MainActivity acts as the entry point router.
//         It immediately delegates to SplashActivity so we never show a blank screen.
//         This is the LAUNCHER activity registered in the manifest.

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.example.scholartrack.ui.SplashActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Install Android 12+ Splash Screen
        SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        
        // Forward immediately to SplashActivity; never display the layout.
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }
}