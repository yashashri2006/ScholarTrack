package com.example.scholartrack.receiver;

// Unit 4: BroadcastReceiver for BOOT_COMPLETED – reschedules saved alarms after reboot.
//         Requires RECEIVE_BOOT_COMPLETED permission and an intent-filter in the manifest.

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.scholartrack.data.sqlite.DbHelper;
import com.example.scholartrack.util.AlarmHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "ScholarTrack-BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        Log.d(TAG, "Boot completed – rescheduling alarms");

        // Re-read saved alarms from SQLite and reschedule each one
        DbHelper helper = new DbHelper(context);
        SQLiteDatabase db = helper.getReadableDatabase();
        Cursor c = db.query(DbHelper.TABLE_ALARMS, null, null, null, null, null, null);
        while (c.moveToNext()) {
            int    alarmId  = c.getInt(c.getColumnIndexOrThrow(DbHelper.COL_ALARM_ID));
            long   millis   = c.getLong(c.getColumnIndexOrThrow(DbHelper.COL_TRIGGER_MILLIS));
            String docName  = c.getString(c.getColumnIndexOrThrow(DbHelper.COL_ALARM_DOC_NAME));
            AlarmHelper.scheduleDocFollowup(context, alarmId, millis, docName);
        }
        c.close();
        Log.d(TAG, "Alarms rescheduled");
    }
}
