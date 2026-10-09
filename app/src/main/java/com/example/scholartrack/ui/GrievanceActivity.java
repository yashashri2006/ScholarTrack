package com.example.scholartrack.ui;

// Unit 6: GrievanceActivity – email Intent with ACTION_SENDTO, To and CC,
//         generates formal grievance email body, SQLite draft storage.
// Unit 5: SQLite CRUD (GrievanceDao) with Cursor for history list.
// Unit 3: Spinner, ListView.

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.FirebaseRepository;
import com.example.scholartrack.data.sqlite.DbHelper;
import com.example.scholartrack.data.sqlite.GrievanceDao;
import com.example.scholartrack.ui.adapter.GrievanceAdapter;
import com.example.scholartrack.util.Constants;
import com.example.scholartrack.util.SnackbarUtil;
import com.example.scholartrack.util.ToastUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GrievanceActivity extends BaseActivity {

    private Spinner           spinnerIssueType;
    private TextInputEditText etDetails, etGrievAppId;
    private MaterialButton    btnGenerate, btnSendEmail, btnCopyText, btnSaveDraft;
    private TextView          tvPreview, tvNoGrievances;
    private ListView          listGrievances;

    private GrievanceDao      dao;
    private GrievanceAdapter  grievanceAdapter;
    private String            selectedIssue = "";
    private String            generatedBody = "";

    // Contacts from Firebase
    private String classCoordEmail = "", deptCoordEmail = "", scholSectionEmail = "";
    private String studentName = "", studentRollNo = "", studentClass = "",
                   studentBranch = "", studentSchemeName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grievance);

        dao = new GrievanceDao(this);

        bindViews();
        setupIssueSpinner();
        loadUserAndContacts();
        loadHistory();

        // Toolbar back
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnGenerate.setOnClickListener(v   -> generateEmail());
        btnSendEmail.setOnClickListener(v  -> sendEmail());
        btnCopyText.setOnClickListener(v   -> copyText());
        btnSaveDraft.setOnClickListener(v  -> saveDraft());
    }

    private void bindViews() {
        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        etDetails        = findViewById(R.id.etGrievDetails);
        etGrievAppId     = findViewById(R.id.etGrievAppId);
        btnGenerate      = findViewById(R.id.btnGenerate);
        btnSendEmail     = findViewById(R.id.btnSendEmail);
        btnCopyText      = findViewById(R.id.btnCopyText);
        btnSaveDraft     = findViewById(R.id.btnSaveDraft);
        tvPreview        = findViewById(R.id.tvEmailPreview);
        tvNoGrievances   = findViewById(R.id.tvNoGrievances);
        listGrievances   = findViewById(R.id.listGrievances);
    }

    private void setupIssueSpinner() {
        String[] issues = {
                getString(R.string.issue_select),
                getString(R.string.issue_portal_login),
                getString(R.string.issue_certificate_rejected),
                getString(R.string.issue_verification_pending),
                getString(R.string.issue_signature_pending),
                getString(R.string.issue_hardcopy_query),
                getString(R.string.issue_application_rejected),
                getString(R.string.issue_amount_not_credited)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, issues);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIssueType.setAdapter(adapter);
        spinnerIssueType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                selectedIssue = pos == 0 ? "" : issues[pos];
            }
            @Override
            public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void loadUserAndContacts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseRepository.getInstance().readUserOnce(user.getUid(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                studentName     = snapshot.child(Constants.FIELD_NAME).getValue(String.class);
                studentRollNo   = snapshot.child(Constants.FIELD_ROLL_NO).getValue(String.class);
                studentClass    = snapshot.child(Constants.FIELD_CLASS).getValue(String.class);
                studentBranch   = snapshot.child(Constants.FIELD_BRANCH).getValue(String.class);
                if (studentName    == null) studentName    = "";
                if (studentRollNo  == null) studentRollNo  = "";
                if (studentClass   == null) studentClass   = "";
                if (studentBranch  == null) studentBranch  = "";
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });

        FirebaseRepository.getInstance().readContactsOnce(user.getUid(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                classCoordEmail = getEmail(snapshot, Constants.FIELD_CLASS_COORD);
                deptCoordEmail  = getEmail(snapshot, Constants.FIELD_DEPT_COORD);
                scholSectionEmail = getEmail(snapshot, Constants.FIELD_SCHOL_SECTION);
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private String getEmail(DataSnapshot snap, String key) {
        String email = snap.child(key).child("email").getValue(String.class);
        return email != null ? email : "";
    }

    private void generateEmail() {
        if (TextUtils.isEmpty(selectedIssue)) {
            SnackbarUtil.showError(findViewById(android.R.id.content),
                    "Please select an issue type");
            return;
        }
        String details = etDetails.getText() != null ? etDetails.getText().toString().trim() : "";
        String appId   = etGrievAppId.getText() != null ? etGrievAppId.getText().toString().trim() : "";
        String date    = new SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(new Date());

        generatedBody = "Dear Scholarship Coordinator,\n\n"
                + "I am writing to bring the following matter to your attention.\n\n"
                + "Issue Type: " + selectedIssue + "\n"
                + "Student Name: " + studentName + "\n"
                + "Roll Number: " + studentRollNo + "\n"
                + "Class & Branch: " + studentClass + " – " + studentBranch + "\n";
        if (!appId.isEmpty()) generatedBody += "MahaDBT Application ID: " + appId + "\n";
        generatedBody += "Date: " + date + "\n\n"
                + "Details:\n" + details + "\n\n"
                + "I request you to please look into this matter and provide the necessary support.\n\n"
                + "Thank you for your time.\n\n"
                + "Sincerely,\n" + studentName;

        tvPreview.setText(generatedBody);
        tvPreview.setVisibility(View.VISIBLE);
        btnSendEmail.setVisibility(View.VISIBLE);
        btnCopyText.setVisibility(View.VISIBLE);
        btnSaveDraft.setVisibility(View.VISIBLE);
    }

    /** Unit 6: Email Intent with To, CC, Subject, Body */
    private void sendEmail() {
        if (TextUtils.isEmpty(generatedBody)) { generateEmail(); return; }

        String appId = etGrievAppId.getText() != null ? etGrievAppId.getText().toString().trim() : "";

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_EMAIL,
                new String[]{classCoordEmail, deptCoordEmail});
        if (!scholSectionEmail.isEmpty()) {
            emailIntent.putExtra(Intent.EXTRA_CC, new String[]{scholSectionEmail});
        }
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                "Scholarship Grievance – " + selectedIssue
                        + (!appId.isEmpty() ? " (App ID: " + appId + ")" : ""));
        emailIntent.putExtra(Intent.EXTRA_TEXT, generatedBody);

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email via…"));
        } catch (android.content.ActivityNotFoundException e) {
            ToastUtil.showLong(this, getString(R.string.no_email_app));
        }
    }

    private void copyText() {
        if (TextUtils.isEmpty(generatedBody)) return;
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("grievance", generatedBody);
        clipboard.setPrimaryClip(clip);
        ToastUtil.show(this, getString(R.string.text_copied));
    }

    private void saveDraft() {
        if (TextUtils.isEmpty(generatedBody)) { generateEmail(); return; }
        String appId = etGrievAppId.getText() != null ? etGrievAppId.getText().toString().trim() : "";
        String date  = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        dao.insert(date, selectedIssue, studentSchemeName, appId, generatedBody);
        SnackbarUtil.show(findViewById(android.R.id.content), getString(R.string.grievance_saved));
        loadHistory();
    }

    private void loadHistory() {
        Cursor c = dao.getAllCursor();
        List<String[]> rows = new ArrayList<>();
        while (c.moveToNext()) {
            rows.add(new String[]{
                    c.getString(c.getColumnIndexOrThrow(DbHelper.COL_ID)),
                    c.getString(c.getColumnIndexOrThrow(DbHelper.COL_CREATED_AT)),
                    c.getString(c.getColumnIndexOrThrow(DbHelper.COL_ISSUE_TYPE)),
                    c.getString(c.getColumnIndexOrThrow(DbHelper.COL_BODY)),
                    c.getString(c.getColumnIndexOrThrow(DbHelper.COL_GRIEV_STATUS))
            });
        }
        c.close();

        tvNoGrievances.setVisibility(rows.isEmpty() ? View.VISIBLE : View.GONE);
        listGrievances.setVisibility(rows.isEmpty() ? View.GONE : View.VISIBLE);

        grievanceAdapter = new GrievanceAdapter(this, rows,
                row -> reopenGrievance(row),
                row -> {
                    dao.markSent(Long.parseLong(row[0]));
                    loadHistory();
                });
        listGrievances.setAdapter(grievanceAdapter);
    }

    private void reopenGrievance(String[] row) {
        tvPreview.setText(row[3]);
        tvPreview.setVisibility(View.VISIBLE);
        generatedBody = row[3];
        selectedIssue = row[2];
        btnSendEmail.setVisibility(View.VISIBLE);
        btnCopyText.setVisibility(View.VISIBLE);
        btnSaveDraft.setVisibility(View.VISIBLE);
    }
}
