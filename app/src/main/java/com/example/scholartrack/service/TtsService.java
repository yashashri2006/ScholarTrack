package com.example.scholartrack.service;

// Unit 4: Service – Text-to-Speech service started from a notification action.
//         Reads the next step aloud using Android's built-in TTS engine.

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.Locale;

public class TtsService extends Service implements TextToSpeech.OnInitListener {

    private static final String TAG = "ScholarTrack-TtsService";
    public static final String EXTRA_TEXT = "tts_text";

    private TextToSpeech tts;
    private String pendingText;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            pendingText = intent.getStringExtra(EXTRA_TEXT);
        }
        if (tts == null) {
            tts = new TextToSpeech(this, this);
        } else {
            speak();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "TTS language not supported; trying default");
                tts.setLanguage(Locale.getDefault());
            }
            speak();
        } else {
            Log.e(TAG, "TTS initialization failed");
            stopSelf();
        }
    }

    private void speak() {
        if (pendingText != null && !pendingText.isEmpty() && tts != null) {
            tts.speak(pendingText, TextToSpeech.QUEUE_FLUSH, null, "scholartrack_tts");
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null; // Not a bound service
    }
}
