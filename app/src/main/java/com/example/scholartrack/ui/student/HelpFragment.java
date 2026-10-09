package com.example.scholartrack.ui.student;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.FirebaseRepository;
import com.example.scholartrack.util.Constants;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HelpFragment extends Fragment {

    private LinearLayout layoutSavedContacts;
    private LinearLayout layoutDefaultContacts;
    private MaterialButton btnContactsActivity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_help, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        layoutSavedContacts   = view.findViewById(R.id.layoutSavedContacts);
        layoutDefaultContacts = view.findViewById(R.id.layoutDefaultContacts);
        btnContactsActivity   = view.findViewById(R.id.btnContactsActivity);

        loadSavedContacts();
        parseDefaultContacts();

        btnContactsActivity.setOnClickListener(v ->
                startActivity(new Intent(getActivity(),
                        com.example.scholartrack.ui.ContactsActivity.class)));
    }

    // ── Firebase saved contacts ─────────────────────────────────────────

    private void loadSavedContacts() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseRepository.getInstance().readContactsOnce(user.getUid(),
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (getContext() == null) return;
                        addContactCard(layoutSavedContacts,
                                getString(R.string.class_coordinator),
                                snapshot.child(Constants.FIELD_CLASS_COORD));
                        addContactCard(layoutSavedContacts,
                                getString(R.string.dept_coordinator),
                                snapshot.child(Constants.FIELD_DEPT_COORD));
                        addContactCard(layoutSavedContacts,
                                getString(R.string.scholarship_section),
                                snapshot.child(Constants.FIELD_SCHOL_SECTION));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void addContactCard(LinearLayout parent, String label, DataSnapshot snap) {
        if (!snap.exists()) return;
        String name  = snap.child("name").getValue(String.class);
        String email = snap.child("email").getValue(String.class);
        String phone = snap.child("phone").getValue(String.class);
        if (name == null && phone == null) return;

        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_contact_card, parent, false);
        ((TextView) card.findViewById(R.id.tvContactLabel)).setText(label);
        ((TextView) card.findViewById(R.id.tvContactName)).setText(name != null ? name : "");
        ((TextView) card.findViewById(R.id.tvContactPhone)).setText(phone != null ? phone : "");
        ((TextView) card.findViewById(R.id.tvContactEmail)).setText(email != null ? email : "");

        // Unit 6: Call Intent
        String finalPhone = phone;
        card.findViewById(R.id.btnCallContact).setOnClickListener(v -> {
            if (finalPhone != null && !finalPhone.isEmpty()
                    && !finalPhone.contains("[Phone Number]") && !finalPhone.contains("[Helpline Number]")) {
                startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + finalPhone)));
            }
        });

        // Unit 6: SMS Intent
        card.findViewById(R.id.btnSmsContact).setOnClickListener(v -> {
            if (finalPhone != null && !finalPhone.isEmpty()
                    && !finalPhone.contains("[Phone Number]") && !finalPhone.contains("[Helpline Number]")) {
                startActivity(new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("smsto:" + finalPhone)));
            }
        });

        parent.addView(card);
    }

    // ── Unit 6: XML parsing with XmlPullParser ──────────────────────────

    private void parseDefaultContacts() {
        try {
            InputStream is = requireContext().getAssets().open("contacts.xml");
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(is, null);

            int eventType = parser.getEventType();
            String currentTag = null;
            String officeName = null;
            String person = null, phone = null, email = null,
                   timings = null, location = null, note = null;

            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        currentTag = parser.getName();
                        if ("office".equals(currentTag) || "helpline".equals(currentTag)) {
                            officeName = parser.getAttributeValue(null, "name");
                            person = phone = email = timings = location = note = null;
                        }
                        break;

                    case XmlPullParser.TEXT:
                        if (currentTag == null) break;
                        String text = parser.getText().trim();
                        switch (currentTag) {
                            case "person":   person   = text; break;
                            case "phone":    phone    = text; break;
                            case "email":    email    = text; break;
                            case "timings":  timings  = text; break;
                            case "location": location = text; break;
                            case "note":     note     = text; break;
                        }
                        break;

                    case XmlPullParser.END_TAG:
                        if ("office".equals(parser.getName())
                                || "helpline".equals(parser.getName())) {
                            // Add card to default contacts layout
                            addDefaultContactCard(officeName, person, phone, email,
                                    timings, location, note);
                        }
                        currentTag = null;
                        break;
                }
                eventType = parser.next();
            }
            is.close();
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
        }
    }

    private void addDefaultContactCard(String label, String person, String phone,
                                       String email, String timings,
                                       String location, String note) {
        if (getContext() == null) return;
        View card = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_default_contact_card, layoutDefaultContacts, false);
        ((TextView) card.findViewById(R.id.tvDefaultLabel)).setText(label != null ? label : "");
        ((TextView) card.findViewById(R.id.tvDefaultPerson)).setText(person != null ? person : "");
        ((TextView) card.findViewById(R.id.tvDefaultPhone)).setText(phone != null ? phone : "");
        ((TextView) card.findViewById(R.id.tvDefaultNote)).setText(note != null ? note : "");
        if (timings != null && !timings.isEmpty()) {
            TextView tvT = card.findViewById(R.id.tvDefaultTimings);
            tvT.setVisibility(View.VISIBLE);
            tvT.setText(timings);
        }

        // Unit 6: Maps geo: Intent
        String finalLocation = location;
        card.findViewById(R.id.btnMapsContact).setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + Uri.encode(
                                finalLocation != null ? finalLocation : label)));
                startActivity(intent);
            } catch (Exception e) {
                com.example.scholartrack.util.ToastUtil.show(
                        requireContext(), getString(R.string.no_maps_app));
            }
        });

        String finalPhone = phone;
        card.findViewById(R.id.btnCallDefault).setOnClickListener(v -> {
            if (finalPhone != null && !finalPhone.isEmpty()
                    && !finalPhone.contains("[Phone Number]") && !finalPhone.contains("[Helpline Number]")) {
                startActivity(new Intent(Intent.ACTION_DIAL,
                        Uri.parse("tel:" + finalPhone)));
            }
        });

        layoutDefaultContacts.addView(card);
    }
}
