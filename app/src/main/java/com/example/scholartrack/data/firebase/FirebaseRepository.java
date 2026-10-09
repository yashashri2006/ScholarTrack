package com.example.scholartrack.data.firebase;

// Unit 5: Firebase Realtime Database repository – wraps all DB reads and writes.
//         Uses ValueEventListener for live updates (reactive push model).

import com.example.scholartrack.data.models.StageInfo;
import com.example.scholartrack.util.Constants;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class FirebaseRepository {

    private final DatabaseReference dbRef;
    private static FirebaseRepository instance;

    private FirebaseRepository() {
        dbRef = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseRepository getInstance() {
        if (instance == null) instance = new FirebaseRepository();
        return instance;
    }

    // ── User profile ──────────────────────────────────────────────────────

    public DatabaseReference userRef(String uid) {
        return dbRef.child(Constants.NODE_USERS).child(uid);
    }

    public void saveUserProfile(String uid, Map<String, Object> profile,
                                OnCompleteListener listener) {
        userRef(uid).setValue(profile)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void listenUser(String uid, ValueEventListener listener) {
        userRef(uid).addValueEventListener(listener);
    }

    public void readUserOnce(String uid, ValueEventListener listener) {
        userRef(uid).addListenerForSingleValueEvent(listener);
    }

    public void updateUserField(String uid, String field, Object value) {
        userRef(uid).child(field).setValue(value);
    }

    // ── Application tracker ───────────────────────────────────────────────

    public DatabaseReference applicationRef(String uid) {
        return dbRef.child(Constants.NODE_APPLICATIONS).child(uid);
    }

    public void listenApplication(String uid, ValueEventListener listener) {
        applicationRef(uid).addValueEventListener(listener);
    }

    public void readApplicationOnce(String uid, ValueEventListener listener) {
        applicationRef(uid).addListenerForSingleValueEvent(listener);
    }

    public void initApplication(String uid, String schemeId) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.FIELD_SCHEME_ID,      schemeId);
        data.put(Constants.FIELD_APPLICATION_ID, "");
        data.put(Constants.FIELD_CURRENT_STAGE,  0);
        applicationRef(uid).setValue(data);
    }

    public void updateApplicationId(String uid, String applicationId) {
        applicationRef(uid).child(Constants.FIELD_APPLICATION_ID).setValue(applicationId);
    }

    /** Updates a single stage for a student's application. */
    public void updateStage(String uid, int stageNumber, StageInfo info,
                            OnCompleteListener listener) {
        applicationRef(uid)
                .child(Constants.NODE_STAGES)
                .child(String.valueOf(stageNumber))
                .setValue(info.toMap())
                .addOnSuccessListener(unused -> {
                    // Also update currentStage if this stage is now the furthest done
                    applicationRef(uid).child(Constants.FIELD_CURRENT_STAGE)
                            .setValue(stageNumber);
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    // ── Contacts ──────────────────────────────────────────────────────────

    public DatabaseReference contactsRef(String uid) {
        return dbRef.child(Constants.NODE_CONTACTS).child(uid);
    }

    public void saveContacts(String uid, Map<String, Object> contacts,
                             OnCompleteListener listener) {
        contactsRef(uid).setValue(contacts)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
    }

    public void listenContacts(String uid, ValueEventListener listener) {
        contactsRef(uid).addValueEventListener(listener);
    }

    public void readContactsOnce(String uid, ValueEventListener listener) {
        contactsRef(uid).addListenerForSingleValueEvent(listener);
    }

    // ── Coordinator: read all students ────────────────────────────────────

    public DatabaseReference allApplicationsRef() {
        return dbRef.child(Constants.NODE_APPLICATIONS);
    }

    public DatabaseReference allUsersRef() {
        return dbRef.child(Constants.NODE_USERS);
    }

    // ── Simple callback interface ──────────────────────────────────────────

    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }
}
