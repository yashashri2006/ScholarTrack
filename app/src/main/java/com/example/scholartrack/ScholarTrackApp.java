package com.example.scholartrack;

// Unit 2: Application subclass – creates the notification channel on first launch
//         and holds global helpers (declared in AndroidManifest application tag).
// Unit 4: Notification channel creation (required for Android 8+)

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.example.scholartrack.util.Constants;

public class ScholarTrackApp extends Application {

    private static final String TAG = "ScholarTrack-App";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate");
        createNotificationChannel();
    }

    /**
     * Creates the notification channel that all reminders use.
     * Unit 4: NotificationChannel (required Android 8+, ignored on older versions).
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
                Log.d(TAG, "Notification channel created");
            }
        }
    }
}
