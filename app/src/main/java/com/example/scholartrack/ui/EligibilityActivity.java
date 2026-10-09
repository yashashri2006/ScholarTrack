package com.example.scholartrack.ui;

// Unit 6: EligibilityActivity – category Spinner, income EditText, gender RadioGroup,
//         CheckBoxes; sends values to SchemeResultsActivity via explicit Intent + extras.
// Unit 3: Spinner, RadioGroup, CheckBox, TextInputLayout.
// Unit 2: Explicit Intent with multiple extras.

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.FirebaseRepository;
import com.example.scholartrack.util.Constants;
import com.example.scholartrack.util.SnackbarUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EligibilityActivity extends BaseActivity {

    private Spinner        spinnerCategory;
    private TextInputLayout tilIncome;
    private TextInputEditText etIncome;
    private RadioGroup     rgGender;
    private RadioButton    rbMale, rbFemale, rbOther;
    private CheckBox       cbMinority, cbPwd, cbHostel;
    private MaterialButton btnCheckEligibility;

    private String selectedCategory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eligibility);

        bindViews();
        setupCategorySpinner();

        btnCheckEligibility.setOnClickListener(v -> checkEligibility());

        // Back button
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindViews() {
        spinnerCategory      = findViewById(R.id.spinnerCategory);
        tilIncome            = findViewById(R.id.tilIncome);
        etIncome             = findViewById(R.id.etIncome);
        rgGender             = findViewById(R.id.rgGender);
        rbMale               = findViewById(R.id.rbMale);
        rbFemale             = findViewById(R.id.rbFemale);
        rbOther              = findViewById(R.id.rbOther);
        cbMinority           = findViewById(R.id.cbMinority);
        cbPwd                = findViewById(R.id.cbPwd);
        cbHostel             = findViewById(R.id.cbHostel);
        btnCheckEligibility  = findViewById(R.id.btnCheckEligibility);
    }

    // Unit 3: Spinner with ArrayAdapter
    private void setupCategorySpinner() {
        String[] categories = {
                getString(R.string.category_select),
                getString(R.string.category_sc),
                getString(R.string.category_st),
                getString(R.string.category_obc),
                getString(R.string.category_vjnt),
                getString(R.string.category_sbc),
                getString(R.string.category_open_ebc)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedCategory = pos == 0 ? "" : categories[pos];
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void checkEligibility() {
        // Validate
        if (TextUtils.isEmpty(selectedCategory)) {
            SnackbarUtil.showError(findViewById(android.R.id.content),
                    getString(R.string.error_select_category));
            return;
        }
        if (rgGender.getCheckedRadioButtonId() == -1) {
            SnackbarUtil.showError(findViewById(android.R.id.content),
                    getString(R.string.error_select_gender));
            return;
        }

        long income = 0;
        String incomeStr = etIncome.getText() != null ? etIncome.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(incomeStr)) {
            try { income = Long.parseLong(incomeStr); }
            catch (NumberFormatException e) {
                tilIncome.setError(getString(R.string.error_invalid_income));
                return;
            }
        }
        tilIncome.setError(null);

        boolean isFemale  = rbFemale.isChecked();
        boolean isMinority = cbMinority.isChecked();
        boolean isPwd      = cbPwd.isChecked();
        boolean isHostel   = cbHostel.isChecked();

        // Save category to Firebase profile
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseRepository.getInstance().updateUserField(
                    user.getUid(), Constants.FIELD_CATEGORY, selectedCategory);
        }

        // Unit 2: Explicit Intent with extras to SchemeResultsActivity
        Intent intent = new Intent(this, SchemeResultsActivity.class);
        intent.putExtra(Constants.EXTRA_CATEGORY, selectedCategory);
        intent.putExtra(Constants.EXTRA_INCOME,   income);
        intent.putExtra(Constants.EXTRA_FEMALE,   isFemale);
        intent.putExtra(Constants.EXTRA_MINORITY, isMinority);
        intent.putExtra(Constants.EXTRA_PWD,      isPwd);
        intent.putExtra(Constants.EXTRA_HOSTEL,   isHostel);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
