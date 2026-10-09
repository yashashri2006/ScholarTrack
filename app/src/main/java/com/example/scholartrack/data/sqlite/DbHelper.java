package com.example.scholartrack.data.sqlite;

// Unit 5: SQLiteOpenHelper – creates and upgrades the local database with two tables:
//         'documents' and 'grievances'. Full CRUD demonstrated via Cursor.

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME    = "scholartrack.db";
    public static final int    DB_VERSION = 2;

    // ── Table: documents ──────────────────────────────────────────────────
    public static final String TABLE_DOCUMENTS  = "documents";
    public static final String COL_ID           = "id";
    public static final String COL_NAME         = "name";
    public static final String COL_SCHEME_ID    = "scheme_id";
    public static final String COL_STATUS       = "status";
    public static final String COL_APPLIED_DATE = "applied_date";
    public static final String COL_OFFICE       = "office";
    public static final String COL_NOTE         = "note";
    public static final String COL_IS_CUSTOM    = "is_custom";

    // Status constants
    public static final String STATUS_READY           = "READY";
    public static final String STATUS_NOT_READY       = "NOT_READY";
    public static final String STATUS_APPLIED_TO_MAKE = "APPLIED_TO_MAKE";
    public static final String STATUS_RECEIVED        = "RECEIVED";

    // ── Table: grievances ─────────────────────────────────────────────────
    public static final String TABLE_GRIEVANCES    = "grievances";
    public static final String COL_CREATED_AT      = "created_at";
    public static final String COL_ISSUE_TYPE      = "issue_type";
    public static final String COL_SCHEME_NAME     = "scheme_name";
    public static final String COL_APPLICATION_ID  = "application_id";
    public static final String COL_BODY            = "body";
    public static final String COL_GRIEV_STATUS    = "status";

    public static final String GRIEV_STATUS_DRAFT = "DRAFT";
    public static final String GRIEV_STATUS_SENT  = "SENT";

    // ── Table: alarms ─────────────────────────────────────────────────────
    public static final String TABLE_ALARMS        = "alarms";
    public static final String COL_ALARM_ID        = "alarm_id";
    public static final String COL_TRIGGER_MILLIS  = "trigger_millis";
    public static final String COL_ALARM_DOC_NAME  = "doc_name";

    private static final String CREATE_DOCUMENTS =
            "CREATE TABLE " + TABLE_DOCUMENTS + " ("
            + COL_ID           + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_NAME         + " TEXT NOT NULL, "
            + COL_SCHEME_ID    + " TEXT DEFAULT '', "
            + COL_STATUS       + " TEXT NOT NULL DEFAULT '" + STATUS_NOT_READY + "', "
            + COL_APPLIED_DATE + " TEXT DEFAULT '', "
            + COL_OFFICE       + " TEXT DEFAULT '', "
            + COL_NOTE         + " TEXT DEFAULT '', "
            + COL_IS_CUSTOM    + " INTEGER DEFAULT 0"
            + ")";

    private static final String CREATE_GRIEVANCES =
            "CREATE TABLE " + TABLE_GRIEVANCES + " ("
            + COL_ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COL_CREATED_AT    + " TEXT NOT NULL, "
            + COL_ISSUE_TYPE    + " TEXT NOT NULL, "
            + COL_SCHEME_NAME   + " TEXT DEFAULT '', "
            + COL_APPLICATION_ID + " TEXT DEFAULT '', "
            + COL_BODY          + " TEXT NOT NULL, "
            + COL_GRIEV_STATUS  + " TEXT NOT NULL DEFAULT '" + GRIEV_STATUS_DRAFT + "'"
            + ")";

    private static final String CREATE_ALARMS =
            "CREATE TABLE " + TABLE_ALARMS + " ("
            + COL_ALARM_ID       + " INTEGER PRIMARY KEY, "
            + COL_TRIGGER_MILLIS + " INTEGER NOT NULL, "
            + COL_ALARM_DOC_NAME + " TEXT DEFAULT ''"
            + ")";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DOCUMENTS);
        db.execSQL(CREATE_GRIEVANCES);
        db.execSQL(CREATE_ALARMS);
        insertDefaultDocuments(db);
    }

    private void insertDefaultDocuments(SQLiteDatabase db) {
        String[][] defaults = {
            {"Admission Confirmation Letter", "Xerox"},
            {"10th Marksheet", "Xerox"},
            {"12th Marksheet", "Xerox"},
            {"Previous Two Semesters' Marksheets", "Xerox"},
            {"T.C. (Transfer Certificate)", "Xerox"},
            {"Tahsildar Income Certificate (2025–2026)", "Required only for ST and EBC"},
            {"Admission Fee Receipt / Bonafide", "Fee Receipt for OBC, VJNT, SBC, EBC, SEBC. Bonafide for SC, ST"},
            {"Aadhaar Card", "Xerox — Mobile and Bank must be linked"},
            {"Bank Passbook", "Xerox — Only Nationalised Bank"},
            {"Caste Certificate", "ORIGINAL"},
            {"Non-Creamy Layer Certificate", "Xerox — Applicable to OBC, SBC, VJNT and SEBC"},
            {"Ration Card", "Xerox"},
            {"Gap Certificate", "ORIGINAL — Required only if there is a gap"}
        };
        for (String[] doc : defaults) {
            android.content.ContentValues cv = new android.content.ContentValues();
            cv.put(COL_NAME, doc[0]);
            cv.put(COL_NOTE, doc[1]);
            cv.put(COL_STATUS, STATUS_NOT_READY);
            cv.put(COL_IS_CUSTOM, 0);
            db.insert(TABLE_DOCUMENTS, null, cv);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Simple drop-and-recreate for the project; production would migrate rows.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GRIEVANCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARMS);
        onCreate(db);
    }
}
