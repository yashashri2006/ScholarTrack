package com.example.scholartrack.ui;

// Unit 2: RegisterActivity – Firebase registration + role selection + coordinator access code.
//         Demonstrates Spinner, explicit Intent, extras, and Firebase Auth + Database write.
// Unit 3: LinearLayout, Spinner, TextInputLayout, ProgressBar.
// Unit 5: Firebase Auth createUserWithEmailAndPassword + Realtime Database write.

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.FirebaseRepository;
import com.example.scholartrack.data.models.UserProfile;
import com.example.scholartrack.util.Constants;
import com.example.scholartrack.util.SnackbarUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private TextInputLayout   tilName, tilRollNo, tilClassName, tilBranch;
    private TextInputLayout   tilEmail, tilPassword, tilConfirmPassword, tilAccessCode;
    private TextInputEditText etName, etRollNo, etClassName, etBranch;
    private TextInputEditText etEmail, etPassword, etConfirmPassword, etAccessCode;
    private Spinner           spinnerRole;
    private MaterialButton    btnRegister, btnGoToLogin;
    private ProgressBar       progressRegister;

    private FirebaseAuth auth;
    private String selectedRole = Constants.ROLE_STUDENT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        auth = FirebaseAuth.getInstance();
        bindViews();
        setupRoleSpinner();

        btnRegister.setOnClickListener(v -> attemptRegister());
        btnGoToLogin.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        });
    }

    private void bindViews() {
        tilName            = findViewById(R.id.tilName);
        tilRollNo          = findViewById(R.id.tilRollNo);
        tilClassName       = findViewById(R.id.tilClassName);
        tilBranch          = findViewById(R.id.tilBranch);
        tilEmail           = findViewById(R.id.tilEmail);
        tilPassword        = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        tilAccessCode      = findViewById(R.id.tilAccessCode);

        etName             = findViewById(R.id.etName);
        etRollNo           = findViewById(R.id.etRollNo);
        etClassName        = findViewById(R.id.etClassName);
        etBranch           = findViewById(R.id.etBranch);
        etEmail            = findViewById(R.id.etEmail);
        etPassword         = findViewById(R.id.etPassword);
        etConfirmPassword  = findViewById(R.id.etConfirmPassword);
        etAccessCode       = findViewById(R.id.etAccessCode);

        spinnerRole        = findViewById(R.id.spinnerRole);
        btnRegister        = findViewById(R.id.btnRegister);
        btnGoToLogin       = findViewById(R.id.btnGoToLogin);
        progressRegister   = findViewById(R.id.progressRegister);
    }

    // Unit 3: Spinner with ArrayAdapter
    private void setupRoleSpinner() {
        String[] roles = {
                getString(R.string.role_student),
                getString(R.string.role_coordinator)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        spinnerRole.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (pos == 1) {
                    selectedRole = Constants.ROLE_COORDINATOR;
                    tilAccessCode.setVisibility(View.VISIBLE);
                    // Update roll number hint
                    tilRollNo.setHint(getString(R.string.staff_id));
                } else {
                    selectedRole = Constants.ROLE_STUDENT;
                    tilAccessCode.setVisibility(View.GONE);
                    tilRollNo.setHint(getString(R.string.roll_number));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void attemptRegister() {
        clearErrors();
        String name    = text(etName);
        String rollNo  = text(etRollNo);
        String cls     = text(etClassName);
        String branch  = text(etBranch);
        String email   = text(etEmail);
        String pass    = text(etPassword);
        String confirm = text(etConfirmPassword);
        String code    = text(etAccessCode);

        boolean valid = true;

        if (TextUtils.isEmpty(name))   { tilName.setError(getString(R.string.error_field_required));      valid = false; }
        if (TextUtils.isEmpty(rollNo)) { tilRollNo.setError(getString(R.string.error_field_required));    valid = false; }
        if (TextUtils.isEmpty(cls))    { tilClassName.setError(getString(R.string.error_field_required)); valid = false; }
        if (TextUtils.isEmpty(branch)) { tilBranch.setError(getString(R.string.error_field_required));    valid = false; }

        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            valid = false;
        }

        if (TextUtils.isEmpty(pass)) {
            tilPassword.setError(getString(R.string.error_field_required));
            valid = false;
        } else if (pass.length() < 6) {
            tilPassword.setError(getString(R.string.error_password_short));
            valid = false;
        }

        if (!pass.equals(confirm)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            valid = false;
        }

        if (Constants.ROLE_COORDINATOR.equals(selectedRole)) {
            if (!Constants.COORDINATOR_CODE.equals(code)) {
                tilAccessCode.setError(getString(R.string.error_invalid_access_code));
                valid = false;
            }
        }

        if (!valid) return;

        setLoading(true);

        final String finalName   = name;
        final String finalRollNo = rollNo;
        final String finalCls    = cls;
        final String finalBranch = branch;

        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user == null) { setLoading(false); return; }

                        // Save profile to Firebase Realtime Database
                        Map<String, Object> profile = new HashMap<>();
                        profile.put(Constants.FIELD_NAME,      finalName);
                        profile.put(Constants.FIELD_EMAIL,     email);
                        profile.put(Constants.FIELD_ROLE,      selectedRole);
                        profile.put(Constants.FIELD_ROLL_NO,   finalRollNo);
                        profile.put(Constants.FIELD_CLASS,     finalCls);
                        profile.put(Constants.FIELD_BRANCH,    finalBranch);
                        profile.put(Constants.FIELD_CATEGORY,  "");

                        FirebaseRepository.getInstance().saveUserProfile(
                                user.getUid(), profile,
                                new FirebaseRepository.OnCompleteListener() {
                                    @Override
                                    public void onSuccess() {
                                        setLoading(false);
                                        navigateToHome();
                                    }

                                    @Override
                                    public void onFailure(String error) {
                                        setLoading(false);
                                        SnackbarUtil.showError(
                                                findViewById(android.R.id.content),
                                                getString(R.string.register_failed, error));
                                    }
                                });
                    } else {
                        setLoading(false);
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.error_generic);
                        
                        if (msg != null && msg.contains("CONFIGURATION_NOT_FOUND")) {
                            msg = "Firebase Auth is disabled. Please enable 'Email/Password' Sign-in provider in the Firebase Console under Authentication > Sign-in method.";
                        }
                        
                        SnackbarUtil.showError(
                                findViewById(android.R.id.content),
                                getString(R.string.register_failed, msg));
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        btnRegister.setEnabled(!loading);
        progressRegister.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void clearErrors() {
        tilName.setError(null); tilRollNo.setError(null);
        tilClassName.setError(null); tilBranch.setError(null);
        tilEmail.setError(null); tilPassword.setError(null);
        tilConfirmPassword.setError(null); tilAccessCode.setError(null);
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
