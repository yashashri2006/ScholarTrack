package com.example.scholartrack.data.sqlite;

// Unit 5: Grievance DAO – CRUD on 'grievances' SQLite table using Cursor.

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class GrievanceDao {

    private final DbHelper dbHelper;

    public GrievanceDao(Context context) {
        dbHelper = new DbHelper(context);
    }

    public long insert(String createdAt, String issueType, String schemeName,
                       String applicationId, String body) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.COL_CREATED_AT,   createdAt);
        cv.put(DbHelper.COL_ISSUE_TYPE,   issueType);
        cv.put(DbHelper.COL_SCHEME_NAME,  schemeName);
        cv.put(DbHelper.COL_APPLICATION_ID, applicationId);
        cv.put(DbHelper.COL_BODY,         body);
        cv.put(DbHelper.COL_GRIEV_STATUS, DbHelper.GRIEV_STATUS_DRAFT);
        return db.insert(DbHelper.TABLE_GRIEVANCES, null, cv);
    }

    public Cursor getAllCursor() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DbHelper.TABLE_GRIEVANCES, null, null, null, null, null,
                DbHelper.COL_CREATED_AT + " DESC");
    }

    public Cursor getByIdCursor(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DbHelper.TABLE_GRIEVANCES, null,
                DbHelper.COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);
    }

    public int markSent(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.COL_GRIEV_STATUS, DbHelper.GRIEV_STATUS_SENT);
        return db.update(DbHelper.TABLE_GRIEVANCES, cv,
                DbHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DbHelper.TABLE_GRIEVANCES,
                DbHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
    }
}
