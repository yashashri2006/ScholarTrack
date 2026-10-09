package com.example.scholartrack.util;

// Unit 2: Application constants – roles, database node names, external URL

public final class Constants {

    private Constants() { /* no instances */ }

    // ── Roles ──────────────────────────────────────────────────────────────
    public static final String ROLE_STUDENT     = "student";
    public static final String ROLE_COORDINATOR = "coordinator";

    /**
     * Hard-coded access code used during coordinator registration.
     * NOTE: In a production system, coordinator accounts would require
     * admin approval rather than a shared secret. This is intentionally
     * simple for the college project demonstration.
     */
    public static final String COORDINATOR_CODE = "TRACK2026";

    // ── Firebase Realtime Database node names ──────────────────────────────
    public static final String NODE_USERS        = "users";
    public static final String NODE_APPLICATIONS = "applications";
    public static final String NODE_CONTACTS     = "contacts";
    public static final String NODE_STAGES       = "stages";

    // ── User profile fields ────────────────────────────────────────────────
    public static final String FIELD_NAME      = "name";
    public static final String FIELD_EMAIL     = "email";
    public static final String FIELD_ROLE      = "role";
    public static final String FIELD_ROLL_NO   = "rollNo";
    public static final String FIELD_CLASS     = "className";
    public static final String FIELD_BRANCH    = "branch";
    public static final String FIELD_CATEGORY  = "category";

    // ── Application fields ─────────────────────────────────────────────────
    public static final String FIELD_SCHEME_ID       = "schemeId";
    public static final String FIELD_APPLICATION_ID  = "applicationId";
    public static final String FIELD_CURRENT_STAGE   = "currentStage";
    public static final String FIELD_DONE            = "done";
    public static final String FIELD_DATE            = "date";
    public static final String FIELD_NOTE            = "note";
    public static final String FIELD_UPDATED_BY      = "updatedBy";

    // ── Contact fields ─────────────────────────────────────────────────────
    public static final String FIELD_CLASS_COORD  = "classCoordinator";
    public static final String FIELD_DEPT_COORD   = "deptCoordinator";
    public static final String FIELD_SCHOL_SECTION = "scholarshipSection";
    public static final String FIELD_PHONE        = "phone";

    // ── Total application stages ───────────────────────────────────────────
    public static final int TOTAL_STAGES = 8;

    // ── Stage numbers ──────────────────────────────────────────────────────
    public static final int STAGE_ELIGIBILITY    = 1;
    public static final int STAGE_DOCS_READY     = 2;
    public static final int STAGE_FORM_SUBMITTED = 3;
    public static final int STAGE_COORD_VERIFIED = 4;  // coordinator only
    public static final int STAGE_SIGNED         = 5;  // coordinator only
    public static final int STAGE_HARDCOPY_SUBMITTED = 6;
    public static final int STAGE_STATUS         = 7;
    public static final int STAGE_CREDITED       = 8;

    // ── MahaDBT portal URL (Unit 6: implicit Intent) ──────────────────────
    public static final String MAHADBT_URL = "https://mahadbt.maharashtra.gov.in";

    // ── Notification channel ───────────────────────────────────────────────
    public static final String NOTIFICATION_CHANNEL_ID = "scholartrack_reminders";

    // ── Intent extras ─────────────────────────────────────────────────────
    public static final String EXTRA_STUDENT_UID  = "extra_student_uid";
    public static final String EXTRA_SCHEME_ID    = "extra_scheme_id";
    public static final String EXTRA_CATEGORY     = "extra_category";
    public static final String EXTRA_INCOME       = "extra_income";
    public static final String EXTRA_FEMALE       = "extra_female";
    public static final String EXTRA_MINORITY     = "extra_minority";
    public static final String EXTRA_PWD          = "extra_pwd";
    public static final String EXTRA_HOSTEL       = "extra_hostel";
    public static final String EXTRA_GRIEVANCE_ID = "extra_grievance_id";
    public static final String EXTRA_DOC_NAME     = "extra_doc_name";
    public static final String EXTRA_DOC_ID       = "extra_doc_id";

    // ── AlarmManager request code bases ───────────────────────────────────
    public static final int ALARM_DOC_FOLLOWUP_BASE = 1000;
    public static final int ALARM_DEADLINE          = 9999;

    // ── SharedPreferences ─────────────────────────────────────────────────
    public static final String PREF_DEADLINE_MILLIS = "deadline_millis";
}
