package com.example.scholartrack.ui;

// Unit 5: ContactsActivity – saves class coordinator, dept coordinator and scholarship section
//         to Firebase contacts/{uid}. Unit 6: Call and SMS Intents.

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.FirebaseRepository;
import com.example.scholartrack.util.Constants;
import com.example.scholartrack.util.SnackbarUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ContactsActivity extends BaseActivity {

    // Class coordinator fields
    private TextInputEditText etClassCoordName, etClassCoordEmail, etClassCoordPhone;
    // Dept coordinator fields
    private TextInputEditText etDeptCoordName, etDeptCoordEmail, etDeptCoordPhone;
    // Scholarship section fields
    private TextInputEditText etScholName, etScholEmail, etScholPhone;

    private MaterialButton btnSaveContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        bindViews();
        loadExistingContacts();

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        btnSaveContacts.setOnClickListener(v -> saveContacts());

        setupCallSmsButtons();
    }

    private void bindViews() {
        etClassCoordName  = findViewById(R.id.etClassCoordName);
        etClassCoordEmail = findViewById(R.id.etClassCoordEmail);
        etClassCoordPhone = findViewById(R.id.etClassCoordPhone);
        etDeptCoordName   = findViewById(R.id.etDeptCoordName);
        etDeptCoordEmail  = findViewById(R.id.etDeptCoordEmail);
        etDeptCoordPhone  = findViewById(R.id.etDeptCoordPhone);
        etScholName       = findViewById(R.id.etScholName);
        etScholEmail      = findViewById(R.id.etScholEmail);
        etScholPhone      = findViewById(R.id.etScholPhone);
        btnSaveContacts   = findViewById(R.id.btnSaveContacts);
    }

    private void loadExistingContacts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseRepository.getInstance().readContactsOnce(user.getUid(), new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                fillGroup(snapshot.child(Constants.FIELD_CLASS_COORD),
                        etClassCoordName, etClassCoordEmail, etClassCoordPhone);
                fillGroup(snapshot.child(Constants.FIELD_DEPT_COORD),
                        etDeptCoordName, etDeptCoordEmail, etDeptCoordPhone);
                fillGroup(snapshot.child(Constants.FIELD_SCHOL_SECTION),
                        etScholName, etScholEmail, etScholPhone);
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void fillGroup(DataSnapshot snap,
                           TextInputEditText etName,
                           TextInputEditText etEmail,
                           TextInputEditText etPhone) {
        String name  = snap.child("name").getValue(String.class);
        String email = snap.child("email").getValue(String.class);
        String phone = snap.child("phone").getValue(String.class);
        if (name  != null) etName.setText(name);
        if (email != null) etEmail.setText(email);
        if (phone != null) etPhone.setText(phone);
    }

    private void saveContacts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> contacts = new HashMap<>();
        contacts.put(Constants.FIELD_CLASS_COORD, buildGroup(
                etClassCoordName, etClassCoordEmail, etClassCoordPhone));
        contacts.put(Constants.FIELD_DEPT_COORD, buildGroup(
                etDeptCoordName, etDeptCoordEmail, etDeptCoordPhone));
        contacts.put(Constants.FIELD_SCHOL_SECTION, buildGroup(
                etScholName, etScholEmail, etScholPhone));

        FirebaseRepository.getInstance().saveContacts(user.getUid(), contacts,
                new FirebaseRepository.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        SnackbarUtil.show(findViewById(android.R.id.content),
                                getString(R.string.contacts_saved));
                    }
                    @Override
                    public void onFailure(String error) {
                        SnackbarUtil.showError(findViewById(android.R.id.content),
                                getString(R.string.error_generic));
                    }
                });
    }

    private Map<String, Object> buildGroup(TextInputEditText etName,
                                           TextInputEditText etEmail,
                                           TextInputEditText etPhone) {
        Map<String, Object> group = new HashMap<>();
        group.put("name",  text(etName));
        group.put("email", text(etEmail));
        group.put("phone", text(etPhone));
        return group;
    }

    private void setupCallSmsButtons() {
        // Unit 6: Call and SMS Intents
        MaterialButton btnCallClass = findViewById(R.id.btnCallClass);
        MaterialButton btnSmsClass  = findViewById(R.id.btnSmsClass);
        MaterialButton btnCallDept  = findViewById(R.id.btnCallDept);
        MaterialButton btnSmsDept   = findViewById(R.id.btnSmsDept);
        MaterialButton btnCallSchol = findViewById(R.id.btnCallSchol);
        MaterialButton btnSmsSchol  = findViewById(R.id.btnSmsSchol);

        btnCallClass.setOnClickListener(v -> call(text(etClassCoordPhone)));
        btnSmsClass.setOnClickListener(v  -> sms(text(etClassCoordPhone)));
        btnCallDept.setOnClickListener(v  -> call(text(etDeptCoordPhone)));
        btnSmsDept.setOnClickListener(v   -> sms(text(etDeptCoordPhone)));
        btnCallSchol.setOnClickListener(v -> call(text(etScholPhone)));
        btnSmsSchol.setOnClickListener(v  -> sms(text(etScholPhone)));
    }

    private void call(String phone) {
        if (!TextUtils.isEmpty(phone) && !phone.contains("[Phone Number]") && !phone.contains("[Helpline Number]")) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone)));
        }
    }

    private void sms(String phone) {
        if (!TextUtils.isEmpty(phone) && !phone.contains("[Phone Number]") && !phone.contains("[Helpline Number]")) {
            startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + phone)));
        }
    }

    private String text(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
