package com.example.scholartrack.data.sqlite;

// Unit 5: Document DAO – full CRUD operations on the 'documents' SQLite table.
//         All reads use a Cursor to satisfy the syllabus requirement.

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DocumentDao {

    private final DbHelper dbHelper;

    public DocumentDao(Context context) {
        dbHelper = new DbHelper(context);
    }

    // ── Create ────────────────────────────────────────────────────────────

    /** Inserts a new document row. Returns the new row ID. */
    public long insert(String name, String schemeId, String status,
                       String appliedDate, String office, String note,
                       boolean isCustom) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.COL_NAME,         name);
        cv.put(DbHelper.COL_SCHEME_ID,    schemeId);
        cv.put(DbHelper.COL_STATUS,       status);
        cv.put(DbHelper.COL_APPLIED_DATE, appliedDate);
        cv.put(DbHelper.COL_OFFICE,       office);
        cv.put(DbHelper.COL_NOTE,         note);
        cv.put(DbHelper.COL_IS_CUSTOM,    isCustom ? 1 : 0);
        return db.insert(DbHelper.TABLE_DOCUMENTS, null, cv);
    }

    // ── Read ──────────────────────────────────────────────────────────────

    /** Returns all documents as a Cursor (caller must close). */
    public Cursor getAllCursor() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DbHelper.TABLE_DOCUMENTS, null, null, null, null, null,
                DbHelper.COL_NAME + " ASC");
    }

    /** Returns documents filtered by status. */
    public Cursor getByStatusCursor(String status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DbHelper.TABLE_DOCUMENTS, null,
                DbHelper.COL_STATUS + "=?", new String[]{status},
                null, null, DbHelper.COL_NAME + " ASC");
    }

    /** Returns count of documents with STATUS_READY. */
    public int countReady() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + DbHelper.TABLE_DOCUMENTS
                + " WHERE " + DbHelper.COL_STATUS + "=?",
                new String[]{DbHelper.STATUS_READY});
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    /** Returns total document count. */
    public int countAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + DbHelper.TABLE_DOCUMENTS, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();
        return count;
    }

    // ── Update ────────────────────────────────────────────────────────────

    public int updateStatus(long id, String status, String appliedDate, String office) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.COL_STATUS,       status);
        cv.put(DbHelper.COL_APPLIED_DATE, appliedDate);
        cv.put(DbHelper.COL_OFFICE,       office);
        return db.update(DbHelper.TABLE_DOCUMENTS, cv,
                DbHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int updateNote(long id, String note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.COL_NOTE, note);
        return db.update(DbHelper.TABLE_DOCUMENTS, cv,
                DbHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public int updateFull(long id, String name, String status,
                          String appliedDate, String office, String note) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(DbHelper.COL_NAME,         name);
        cv.put(DbHelper.COL_STATUS,       status);
        cv.put(DbHelper.COL_APPLIED_DATE, appliedDate);
        cv.put(DbHelper.COL_OFFICE,       office);
        cv.put(DbHelper.COL_NOTE,         note);
        return db.update(DbHelper.TABLE_DOCUMENTS, cv,
                DbHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ── Delete ────────────────────────────────────────────────────────────

    public int delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DbHelper.TABLE_DOCUMENTS,
                DbHelper.COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ── Bulk insert from scheme documents ─────────────────────────────────

    public void insertBulk(List<String> names, String schemeId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (String name : names) {
                // Avoid duplicate names
                Cursor dup = db.query(DbHelper.TABLE_DOCUMENTS,
                        new String[]{DbHelper.COL_ID},
                        DbHelper.COL_NAME + "=?", new String[]{name},
                        null, null, null);
                boolean exists = dup.getCount() > 0;
                dup.close();
                if (!exists) {
                    ContentValues cv = new ContentValues();
                    cv.put(DbHelper.COL_NAME,      name);
                    cv.put(DbHelper.COL_SCHEME_ID, schemeId);
                    cv.put(DbHelper.COL_STATUS,    DbHelper.STATUS_NOT_READY);
                    cv.put(DbHelper.COL_IS_CUSTOM, 0);
                    db.insert(DbHelper.TABLE_DOCUMENTS, null, cv);
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    // ── Helper: build list from Cursor ────────────────────────────────────

    public static List<String[]> cursorToList(Cursor cursor) {
        List<String[]> list = new ArrayList<>();
        if (cursor == null) return list;
        while (cursor.moveToNext()) {
            String[] row = new String[]{
                    cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_STATUS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_APPLIED_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_OFFICE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_NOTE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_SCHEME_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COL_IS_CUSTOM))
            };
            list.add(row);
        }
        return list;
    }
}
