package com.example.scholartrack.ui.coordinator;

// Unit 5: StudentsFragment – reads all student applications from Firebase,
//         shows search and filter. Coordinator only.
// Unit 3: ListView with custom adapter, search EditText.

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.FirebaseRepository;
import com.example.scholartrack.ui.StudentDetailActivity;
import com.example.scholartrack.ui.adapter.StudentListAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class StudentsFragment extends Fragment {

    private TextInputEditText etSearch;
    private Spinner           spinnerFilter;
    private ListView          listStudents;
    private TextView          txtNoStudents;
    private StudentListAdapter adapter;
    private List<String[]>    allStudents = new ArrayList<>();
    private String            filterMode  = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_students, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        etSearch     = view.findViewById(R.id.etSearchStudents);
        spinnerFilter = view.findViewById(R.id.spinnerStudentFilter);
        listStudents  = view.findViewById(R.id.listStudents);
        txtNoStudents = view.findViewById(R.id.txtNoStudents);

        setupFilter();
        loadStudents();

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                filterAndDisplay(s.toString());
            }
        });

        listStudents.setOnItemClickListener((parent, v, pos, id) -> {
            String[] row = adapter.getItemAt(pos);
            if (row == null) return;
            Intent intent = new Intent(getActivity(), StudentDetailActivity.class);
            intent.putExtra(com.example.scholartrack.util.Constants.EXTRA_STUDENT_UID, row[0]);
            startActivity(intent);
        });
    }

    private void setupFilter() {
        String[] filters = {
                getString(R.string.filter_all_stages),
                getString(R.string.filter_pending_hardcopy),
                getString(R.string.filter_query_raised)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                filterMode = pos == 0 ? "ALL" : pos == 1 ? "PENDING_HARDCOPY" : "QUERY";
                filterAndDisplay(etSearch.getText() != null
                        ? etSearch.getText().toString() : "");
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });
    }

    private void loadStudents() {
        // Unit 5: Firebase live listener across all students
        FirebaseRepository.getInstance().allApplicationsRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot appsSnap) {
                        allStudents.clear();
                        for (DataSnapshot app : appsSnap.getChildren()) {
                            String uid     = app.getKey();
                            Long stageLong = app.child("currentStage").getValue(Long.class);
                            int  stage     = stageLong != null ? stageLong.intValue() : 0;
                            // Read name & rollNo from users node
                            readUserForStudent(uid, stage);
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });
    }

    private void readUserForStudent(String uid, int stage) {
        FirebaseRepository.getInstance().readUserOnce(uid, new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (getContext() == null) return;
                String name   = snap.child("name").getValue(String.class);
                String rollNo = snap.child("rollNo").getValue(String.class);
                if (name == null) name = "Unknown";
                if (rollNo == null) rollNo = "";
                allStudents.add(new String[]{uid, name, rollNo, String.valueOf(stage)});
                filterAndDisplay(etSearch.getText() != null
                        ? etSearch.getText().toString() : "");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void filterAndDisplay(String query) {
        if (getContext() == null) return;
        List<String[]> filtered = new ArrayList<>();
        for (String[] s : allStudents) {
            String name   = s[1].toLowerCase();
            String roll   = s[2].toLowerCase();
            String qLower = query.toLowerCase().trim();
            boolean matchesQuery = qLower.isEmpty() || name.contains(qLower) || roll.contains(qLower);

            int stage = Integer.parseInt(s[3]);
            boolean matchesFilter = "ALL".equals(filterMode)
                    || ("PENDING_HARDCOPY".equals(filterMode) && stage == 3)
                    || ("QUERY".equals(filterMode) && stage == 7);

            if (matchesQuery && matchesFilter) filtered.add(s);
        }

        txtNoStudents.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
        listStudents.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);

        if (adapter == null) {
            adapter = new StudentListAdapter(requireContext(), filtered);
            listStudents.setAdapter(adapter);
        } else {
            adapter.updateData(filtered);
        }
    }
}
