package com.example.scholartrack.ui.student;


import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

public class TrackerFragment extends Fragment {

    private StepperView   stepperView;
    private TextView      txtNoApplication;
    private MaterialButton btnMarkStage;
    private TextInputEditText etApplicationId;

    private int currentStage = 0;
    private String uid;
    private ValueEventListener appListener;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tracker, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        stepperView      = view.findViewById(R.id.stepperView);
        txtNoApplication = view.findViewById(R.id.txtNoApplication);
        btnMarkStage     = view.findViewById(R.id.btnMarkNextStage);
        etApplicationId  = view.findViewById(R.id.etApplicationId);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        uid = user.getUid();

        loadApplicationTracker();

        btnMarkStage.setOnClickListener(v -> markNextStage());
    }

    private void loadApplicationTracker() {
        appListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) return;
                Long stage = snapshot.child(Constants.FIELD_CURRENT_STAGE).getValue(Long.class);
                currentStage = stage != null ? stage.intValue() : 0;

                String appId = snapshot.child(Constants.FIELD_APPLICATION_ID).getValue(String.class);
                if (appId != null && !appId.isEmpty() && etApplicationId != null) {
                    etApplicationId.setText(appId);
                }

                // Build stage-done array
                boolean[] done = new boolean[8];
                DataSnapshot stagesSnap = snapshot.child(Constants.NODE_STAGES);
                for (int i = 1; i <= 8; i++) {
                    Boolean b = stagesSnap.child(String.valueOf(i))
                            .child("done").getValue(Boolean.class);
                    done[i - 1] = Boolean.TRUE.equals(b);
                }

                stepperView.setStages(STAGE_LABELS, done, currentStage);
                txtNoApplication.setVisibility(currentStage == 0 ? View.VISIBLE : View.GONE);
                btnMarkStage.setText(currentStage < 8
                        ? "Mark Stage " + (currentStage + 1) + " Complete"
                        : "All Stages Complete");
                btnMarkStage.setEnabled(currentStage < 8);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (getContext() != null) {
                    SnackbarUtil.showError(requireView(), getString(R.string.error_generic));
                }
            }
        };
        FirebaseRepository.getInstance().listenApplication(uid, appListener);
    }

    private void markNextStage() {
        int nextStage = currentStage + 1;
        if (nextStage > 8) return;

        // Stages 4 and 5 are coordinator-only
        if (nextStage == Constants.STAGE_COORD_VERIFIED || nextStage == Constants.STAGE_SIGNED) {
            Toast.makeText(getContext(), getString(R.string.coordinator_only),
                    Toast.LENGTH_LONG).show();
            return;
        }

        // If stage 3 ask for application ID
        if (nextStage == Constants.STAGE_FORM_SUBMITTED) {
            showAppIdDialog(nextStage);
        } else {
            saveStage(nextStage, null, null);
        }
    }

    private void showAppIdDialog(int stageNum) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_application_id, null);
        TextInputEditText etId = dialogView.findViewById(R.id.etAppId);
        TextInputEditText etNote = dialogView.findViewById(R.id.etStageNote);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.enter_application_id))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save), (d, w) -> {
                    String appId = etId.getText() != null ? etId.getText().toString().trim() : "";
                    String note  = etNote.getText() != null ? etNote.getText().toString().trim() : "";
                    if (!appId.isEmpty()) {
                        FirebaseRepository.getInstance().updateApplicationId(uid, appId);
                    }
                    saveStage(stageNum, note, null);
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    private void saveStage(int stageNum, String note, String appId) {
        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String updatedBy = user != null ? user.getEmail() : "student";

        StageInfo info = new StageInfo(true, date,
                note != null ? note : "", updatedBy);

        FirebaseRepository.getInstance().updateStage(uid, stageNum, info,
                new FirebaseRepository.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        if (getContext() != null) {
                            SnackbarUtil.show(requireView(), getString(R.string.stage_updated));
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        if (getContext() != null) {
                            SnackbarUtil.showError(requireView(),
                                    getString(R.string.error_generic));
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (uid != null && appListener != null) {
            FirebaseRepository.getInstance()
                    .applicationRef(uid)
                    .removeEventListener(appListener);
        }
    }
}
