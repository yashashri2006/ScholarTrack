package com.example.scholartrack.ui;

// Unit 5: StudentDetailActivity – coordinator views student's stage stepper,
//         can mark stages 4 (verified) and 5 (signed), and add query notes.
//         Sends local notification to student when stage is updated.
// Unit 4: Notification to signal the student.

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.FirebaseRepository;
import com.example.scholartrack.data.models.StageInfo;
import com.example.scholartrack.ui.widget.StepperView;
import com.example.scholartrack.util.Constants;
import com.example.scholartrack.util.SnackbarUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StudentDetailActivity extends BaseActivity {

    private TextView      txtStudentName, txtStudentRollClass;
    private StepperView   stepperView;
    private MaterialButton btnMarkVerified, btnMarkSigned, btnAddQuery;

    private String studentUid;
    private int    currentStage = 0;

    private final String[] STAGE_LABELS = {
            "1. Eligibility Checked",
            "2. Documents Ready",
            "3. MahaDBT Form Submitted",
            "4. Coordinator Verified",
            "5. Hard Copy Signed",
            "6. Hard Copy Submitted",
            "7. Status: Approved / Rejected / Query",
            "8. Amount Credited"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        studentUid = getIntent().getStringExtra(Constants.EXTRA_STUDENT_UID);

        bindViews();
        loadStudentData();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnMarkVerified.setOnClickListener(v -> markStage(Constants.STAGE_COORD_VERIFIED));
        btnMarkSigned.setOnClickListener(v   -> markStage(Constants.STAGE_SIGNED));
        btnAddQuery.setOnClickListener(v     -> showQueryDialog());
    }

    private void bindViews() {
        txtStudentName      = findViewById(R.id.txtStudentDetailName);
        txtStudentRollClass = findViewById(R.id.txtStudentDetailRollClass);
        stepperView         = findViewById(R.id.stepperViewDetail);
        btnMarkVerified     = findViewById(R.id.btnMarkVerified);
        btnMarkSigned       = findViewById(R.id.btnMarkSigned);
        btnAddQuery         = findViewById(R.id.btnAddQuery);
    }

    private void loadStudentData() {
        if (studentUid == null) return;

        FirebaseRepository.getInstance().readUserOnce(studentUid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                String name   = snap.child(Constants.FIELD_NAME).getValue(String.class);
                String roll   = snap.child(Constants.FIELD_ROLL_NO).getValue(String.class);
                String cls    = snap.child(Constants.FIELD_CLASS).getValue(String.class);
                if (name != null) txtStudentName.setText(name);
                if (roll != null && cls != null)
                    txtStudentRollClass.setText(roll + " | " + cls);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });

        FirebaseRepository.getInstance().listenApplication(studentUid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                Long stageLong = snap.child(Constants.FIELD_CURRENT_STAGE).getValue(Long.class);
                currentStage = stageLong != null ? stageLong.intValue() : 0;

                boolean[] done = new boolean[8];
                DataSnapshot stagesSnap = snap.child(Constants.NODE_STAGES);
                for (int i = 1; i <= 8; i++) {
                    Boolean b = stagesSnap.child(String.valueOf(i))
                            .child("done").getValue(Boolean.class);
                    done[i - 1] = Boolean.TRUE.equals(b);
                }
                stepperView.setStages(STAGE_LABELS, done, currentStage);

                // Enable buttons based on current stage
                btnMarkVerified.setEnabled(currentStage == 3);
                btnMarkSigned.setEnabled(currentStage == 4);
            }
            @Override public void onCancelled(@NonNull DatabaseError e) {}
        });
    }

    private void markStage(int stageNum) {
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        FirebaseUser coord = FirebaseAuth.getInstance().getCurrentUser();
        String coordinatorId = coord != null ? coord.getEmail() : "coordinator";

        StageInfo info = new StageInfo(true, date, "", coordinatorId);

        FirebaseRepository.getInstance().updateStage(studentUid, stageNum, info,
                new FirebaseRepository.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        SnackbarUtil.show(findViewById(android.R.id.content),
                                getString(R.string.stage_updated));
                        // Unit 4: Send notification to the student device
                        postStudentNotification(stageNum, coordinatorId);
                    }
                    @Override
                    public void onFailure(String error) {
                        SnackbarUtil.showError(findViewById(android.R.id.content),
                                getString(R.string.error_generic));
                    }
                });
    }

    private void showQueryDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_application_id, null);
        TextInputEditText etNote = dialogView.findViewById(R.id.etStageNote);
        dialogView.findViewById(R.id.tilAppId).setVisibility(View.GONE);

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.add_query_note))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save), (d, w) -> {
                    String note = etNote.getText() != null
                            ? etNote.getText().toString().trim() : "";
                    if (!note.isEmpty()) {
                        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                                .format(new Date());
                        FirebaseUser coord = FirebaseAuth.getInstance().getCurrentUser();
                        String coordId = coord != null ? coord.getEmail() : "coordinator";

                        // Write query note to stage 7 without advancing stage
                        FirebaseRepository.getInstance()
                                .applicationRef(studentUid)
                                .child(Constants.NODE_STAGES)
                                .child("7")
                                .child("note")
                                .setValue(note);

                        SnackbarUtil.show(findViewById(android.R.id.content), "Query note added");
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    /** Posts a local notification to simulate notifying the student. */
    private void postStudentNotification(int stageNum, String coordinatorId) {
        String text = getString(R.string.notification_stage_update_text, coordinatorId);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getString(R.string.notification_stage_update_title))
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(stageNum + 200, builder.build());
    }
}
