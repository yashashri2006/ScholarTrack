package com.example.scholartrack.receiver;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.scholartrack.R;
import com.example.scholartrack.service.TtsService;
import com.example.scholartrack.util.Constants;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_DOC_FOLLOWUP = "com.example.scholartrack.ACTION_DOC_FOLLOWUP";
    public static final String ACTION_DEADLINE     = "com.example.scholartrack.ACTION_DEADLINE";
    public static final String EXTRA_DOC_NAME      = "extra_doc_name";
    public static final String EXTRA_NEXT_STEP     = "extra_next_step";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        String title, text;
        int notifId;

        if (ACTION_DOC_FOLLOWUP.equals(action)) {
            String docName = intent.getStringExtra(EXTRA_DOC_NAME);
            title   = context.getString(R.string.notification_doc_followup_title);
            text    = context.getString(R.string.notification_doc_followup_text,
                    docName != null ? docName : "document");
            notifId = intent.getIntExtra("notif_id", 100);
        } else {
            title   = context.getString(R.string.notification_deadline_title);
            text    = context.getString(R.string.notification_deadline_text);
            notifId = Constants.ALARM_DEADLINE;
        }

        // ── "Read next step aloud" action – starts TtsService (Unit 4: Service) ──
        String nextStep = intent.getStringExtra(EXTRA_NEXT_STEP);
        Intent ttsIntent = new Intent(context, TtsService.class);
        ttsIntent.putExtra(TtsService.EXTRA_TEXT, nextStep != null ? nextStep : text);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                : PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent ttsAction = PendingIntent.getForegroundService(
                context, notifId + 10000, ttsIntent, flags);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_volume, // Unit 4: notification action
                        context.getString(R.string.notification_action_read),
                        ttsAction);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(notifId, builder.build());
    }
}
