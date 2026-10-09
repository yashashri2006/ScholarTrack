package com.example.scholartrack.ui.student;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.FirebaseRepository;
import com.example.scholartrack.ui.EligibilityActivity;
import com.example.scholartrack.ui.GrievanceActivity;
import com.example.scholartrack.ui.StudentHomeActivity;
import com.example.scholartrack.util.AlarmHelper;
import com.example.scholartrack.util.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView      txtGreeting, txtRole, txtStageInfo, txtNextAction, txtDeadlineDate;
    private ProgressBar   progressStage;
    private MaterialCardView cardEligibility, cardDocuments, cardTracker, cardGrievance;
    private MaterialButton   btnSetDeadline, btnLogout;

    private final String[] STAGE_NAMES = {
            "", "Eligibility Checked", "Documents Ready", "MahaDBT Form Submitted",
            "Coordinator Verified", "Hard Copy Signed", "Hard Copy Submitted",
            "Status: Approved/Rejected/Query", "Amount Credited"
    };
    private final String[] NEXT_ACTIONS = {
            "Start by checking your eligibility",
            "Prepare your documents checklist",
            "Submit form on MahaDBT portal",
            "Wait for coordinator verification",
            "Get hard copy signed",
            "Submit hard copy to scholarship section",
            "Wait for approval decision",
            "Wait for amount to be credited",
            "Application complete!"
    };

    private ValueEventListener appListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        txtGreeting    = view.findViewById(R.id.txtGreeting);
        txtRole        = view.findViewById(R.id.txtRole);
        txtStageInfo   = view.findViewById(R.id.txtStageInfo);
        txtNextAction  = view.findViewById(R.id.txtNextAction);
        progressStage  = view.findViewById(R.id.progressStage);
        txtDeadlineDate = view.findViewById(R.id.txtDeadlineDate);
        cardEligibility = view.findViewById(R.id.cardEligibility);
        cardDocuments   = view.findViewById(R.id.cardDocuments);
        cardTracker     = view.findViewById(R.id.cardTracker);
        cardGrievance   = view.findViewById(R.id.cardGrievance);
        btnSetDeadline  = view.findViewById(R.id.btnSetDeadline);
        btnLogout       = view.findViewById(R.id.btnLogout);

        loadUserGreeting();
        loadApplicationStatus();
        setupQuickActions();

        // Unit 3: DatePickerDialog for deadline
        btnSetDeadline.setOnClickListener(v -> showDatePicker());

        btnLogout.setOnClickListener(v -> {
            if (getActivity() instanceof StudentHomeActivity) {
                ((StudentHomeActivity) getActivity()).logout();
            }
        });
    }

    private void loadUserGreeting() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseRepository.getInstance().readUserOnce(user.getUid(),
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (getContext() == null) return;
                        String name = snapshot.child(Constants.FIELD_NAME).getValue(String.class);
                        String displayName = (name != null && !name.isEmpty())
                                ? name.split(" ")[0] : "there";
                        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                        String greeting;
                        if (hour < 12)      greeting = getString(R.string.greeting_morning, displayName);
                        else if (hour < 17) greeting = getString(R.string.greeting_afternoon, displayName);
                        else                greeting = getString(R.string.greeting_evening, displayName);
                        txtGreeting.setText(greeting);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void loadApplicationStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        appListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (getContext() == null) return;
                Long stage = snapshot.child(Constants.FIELD_CURRENT_STAGE).getValue(Long.class);
                int currentStage = stage != null ? stage.intValue() : 0;
                updateStageUI(currentStage);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                txtStageInfo.setText(getString(R.string.no_application));
            }
        };
        FirebaseRepository.getInstance().listenApplication(user.getUid(), appListener);
    }

    private void updateStageUI(int stage) {
        progressStage.setMax(Constants.TOTAL_STAGES);
        progressStage.setProgress(stage);
        if (stage == 0) {
            txtStageInfo.setText(getString(R.string.no_application));
            txtNextAction.setText(NEXT_ACTIONS[0]);
        } else {
            String stageName = stage <= 8 ? STAGE_NAMES[stage] : STAGE_NAMES[8];
            txtStageInfo.setText(getString(R.string.progress_stage, stage, stageName));
            String next = stage < NEXT_ACTIONS.length - 1
                    ? NEXT_ACTIONS[stage] : NEXT_ACTIONS[NEXT_ACTIONS.length - 1];
            txtNextAction.setText(getString(R.string.next_action, next));
        }
    }

    private void setupQuickActions() {
        cardEligibility.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), EligibilityActivity.class)));

        cardDocuments.setOnClickListener(v -> {
            if (getActivity() instanceof StudentHomeActivity) {
                ((StudentHomeActivity) getActivity())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new DocumentsFragment())
                        .commit();
            }
        });

        cardTracker.setOnClickListener(v -> {
            if (getActivity() instanceof StudentHomeActivity) {
                ((StudentHomeActivity) getActivity())
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragmentContainer, new TrackerFragment())
                        .commit();
            }
        });

        // Unit 2: explicit Intent to GrievanceActivity
        cardGrievance.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), GrievanceActivity.class)));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            Calendar deadline = Calendar.getInstance();
            deadline.set(year, month, day, 23, 59, 0);
            long millis = deadline.getTimeInMillis();

            // Save and show
            requireContext().getSharedPreferences("st_prefs", 0)
                    .edit().putLong(Constants.PREF_DEADLINE_MILLIS, millis).apply();

            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            txtDeadlineDate.setText(sdf.format(deadline.getTime()));

            // Unit 4: schedule alarm 1 day before deadline
            AlarmHelper.scheduleDeadline(requireContext(), millis);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Remove Firebase listener to prevent memory leaks
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && appListener != null) {
            FirebaseRepository.getInstance()
                    .applicationRef(user.getUid())
                    .removeEventListener(appListener);
        }
    }
}
