package com.example.scholartrack.util;

// Unit 4: AlarmManager helper – schedules setAndAllowWhileIdle alarms (no exact-alarm permission).
//         Also saves/removes alarm rows in SQLite so BootReceiver can reschedule them.

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;

import com.example.scholartrack.data.sqlite.DbHelper;
import com.example.scholartrack.receiver.AlarmReceiver;

public final class AlarmHelper {

    private AlarmHelper() {}

    /**
     * Schedules a document follow-up notification and persists the alarm row in SQLite.
     * Called when a document status is set to APPLIED_TO_MAKE.
     * Unit 4: AlarmManager.setAndAllowWhileIdle
     */
    public static void scheduleDocFollowup(Context context, int alarmId,
                                           long triggerAtMillis, String docName) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_DOC_FOLLOWUP);
        intent.putExtra(AlarmReceiver.EXTRA_DOC_NAME, docName);
        intent.putExtra("notif_id", alarmId);

        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pi = PendingIntent.getBroadcast(context, alarmId, intent, flags);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }

        // Persist so BootReceiver can reschedule
        saveAlarm(context, alarmId, triggerAtMillis, docName);
    }

    /**
     * Schedules a deadline reminder alarm.
     * triggerAtMillis = deadline - 24 hours.
     */
    public static void scheduleDeadline(Context context, long deadlineMillis) {
        long triggerAt = deadlineMillis - AlarmManager.INTERVAL_DAY;
        if (triggerAt < System.currentTimeMillis()) return;

        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_DEADLINE);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                : PendingIntent.FLAG_UPDATE_CURRENT;

        PendingIntent pi = PendingIntent.getBroadcast(
                context, Constants.ALARM_DEADLINE, intent, flags);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
        }
        saveAlarm(context, Constants.ALARM_DEADLINE, triggerAt, "Deadline");
    }

    public static void cancelAlarm(Context context, int alarmId) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.ACTION_DOC_FOLLOWUP);
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                ? PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE
                : PendingIntent.FLAG_NO_CREATE;
        PendingIntent pi = PendingIntent.getBroadcast(context, alarmId, intent, flags);
        if (pi != null) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (am != null) am.cancel(pi);
        }
        deleteAlarm(context, alarmId);
    }

    private static void saveAlarm(Context context, int alarmId,
                                  long millis, String docName) {
        DbHelper helper = new DbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.COL_ALARM_ID,       alarmId);
        cv.put(DbHelper.COL_TRIGGER_MILLIS, millis);
        cv.put(DbHelper.COL_ALARM_DOC_NAME, docName);
        db.insertWithOnConflict(DbHelper.TABLE_ALARMS, null, cv,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    private static void deleteAlarm(Context context, int alarmId) {
        DbHelper helper = new DbHelper(context);
        SQLiteDatabase db = helper.getWritableDatabase();
        db.delete(DbHelper.TABLE_ALARMS,
                DbHelper.COL_ALARM_ID + "=?", new String[]{String.valueOf(alarmId)});
    }
}
