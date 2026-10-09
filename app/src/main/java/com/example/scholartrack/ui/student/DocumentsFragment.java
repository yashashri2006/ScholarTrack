package com.example.scholartrack.ui.student;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.scholartrack.R;
import com.example.scholartrack.data.sqlite.DbHelper;
import com.example.scholartrack.data.sqlite.DocumentDao;
import com.example.scholartrack.ui.adapter.DocumentAdapter;
import com.example.scholartrack.util.AlarmHelper;
import com.example.scholartrack.util.Constants;
import com.example.scholartrack.util.SnackbarUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DocumentsFragment extends Fragment {

    private TextView         txtDocProgress;
    private ProgressBar      progressDocs;
    private ChipGroup        chipGroupFilter;
    private ListView         listView;
    private TextView         txtEmpty;
    private FloatingActionButton fabAdd;

    private DocumentDao      dao;
    private DocumentAdapter  adapter;
    private List<String[]>   allDocuments = new ArrayList<>();
    private String           currentFilter = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_documents, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        txtDocProgress  = view.findViewById(R.id.txtDocProgress);
        progressDocs    = view.findViewById(R.id.progressDocs);
        chipGroupFilter = view.findViewById(R.id.chipGroupFilter);
        listView        = view.findViewById(R.id.listDocuments);
        txtEmpty        = view.findViewById(R.id.txtEmptyDocuments);
        fabAdd          = view.findViewById(R.id.fabAddDocument);

        dao = new DocumentDao(requireContext());

        setupChips();
        loadDocuments();

        fabAdd.setOnClickListener(v -> showAddDialog());

        adapter = new DocumentAdapter(requireContext(), allDocuments,
                this::onStatusClick, this::onDeleteClick);
        listView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDocuments();
    }

    private void setupChips() {
        Chip chipAll     = chipGroupFilter.findViewById(R.id.chipAll);
        Chip chipReady   = chipGroupFilter.findViewById(R.id.chipReady);
        Chip chipPending = chipGroupFilter.findViewById(R.id.chipPending);

        chipAll.setOnClickListener(v    -> { currentFilter = "ALL";     loadDocuments(); });
        chipReady.setOnClickListener(v  -> { currentFilter = "READY";   loadDocuments(); });
        chipPending.setOnClickListener(v -> { currentFilter = "PENDING"; loadDocuments(); });
    }

    private void loadDocuments() {
        Cursor c;
        if ("READY".equals(currentFilter)) {
            c = dao.getByStatusCursor(DbHelper.STATUS_READY);
        } else if ("PENDING".equals(currentFilter)) {
            c = dao.getByStatusCursor(DbHelper.STATUS_NOT_READY);
        } else {
            c = dao.getAllCursor();
        }

        allDocuments = DocumentDao.cursorToList(c);
        c.close();

        if (adapter != null) {
            adapter.updateData(allDocuments);
        }

        // Update summary
        int ready = dao.countReady();
        int total = dao.countAll();
        progressDocs.setMax(Math.max(total, 1));
        progressDocs.setProgress(ready);
        txtDocProgress.setText(getString(R.string.doc_progress, ready, total));

        txtEmpty.setVisibility(allDocuments.isEmpty() ? View.VISIBLE : View.GONE);
        listView.setVisibility(allDocuments.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void onStatusClick(String[] row) {
        long docId = Long.parseLong(row[0]);
        String docName = row[1];
        String currentStatus = row[2];

        // Status options
        String[] statuses = {
                DbHelper.STATUS_NOT_READY,
                DbHelper.STATUS_APPLIED_TO_MAKE,
                DbHelper.STATUS_READY,
                DbHelper.STATUS_RECEIVED
        };
        String[] labels = {
                getString(R.string.status_not_ready),
                getString(R.string.status_applied_to_make),
                getString(R.string.status_ready),
                getString(R.string.status_received)
        };

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.document_status))
                .setItems(labels, (dialog, which) -> {
                    String newStatus = statuses[which];
                    if (DbHelper.STATUS_APPLIED_TO_MAKE.equals(newStatus)) {
                        showAppliedToMakeDialog(docId, docName);
                    } else {
                        dao.updateStatus(docId, newStatus, "", "");
                        loadDocuments();
                    }
                })
                .show();
    }

    /** Shows DatePickerDialog and office Spinner for APPLIED_TO_MAKE status. */
    private void showAppliedToMakeDialog(long docId, String docName) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
            String date = String.format(Locale.getDefault(), "%02d/%02d/%04d", day, month + 1, year);
            showOfficeDialog(docId, docName, date);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showOfficeDialog(long docId, String docName, String date) {
        String[] offices = {
                getString(R.string.office_tehsildar),
                getString(R.string.office_collector),
                getString(R.string.office_talathi),
                getString(R.string.office_school),
                getString(R.string.office_municipality),
                getString(R.string.office_other)
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.issuing_office))
                .setItems(offices, (dialog, which) -> {
                    dao.updateStatus(docId, DbHelper.STATUS_APPLIED_TO_MAKE,
                            date, offices[which]);
                    // Unit 4: Schedule follow-up alarm in 5 days
                    long triggerAt = System.currentTimeMillis() + (5L * 24 * 60 * 60 * 1000);
                    int alarmId = (int)(Constants.ALARM_DOC_FOLLOWUP_BASE + docId);
                    AlarmHelper.scheduleDocFollowup(requireContext(), alarmId, triggerAt, docName);
                    loadDocuments();
                })
                .show();
    }

    private void onDeleteClick(String[] row, int position) {
        long docId = Long.parseLong(row[0]);
        String docName = row[1];

        dao.delete(docId);
        AlarmHelper.cancelAlarm(requireContext(),
                (int)(Constants.ALARM_DOC_FOLLOWUP_BASE + docId));
        loadDocuments();

        // Unit 5: Snackbar undo
        SnackbarUtil.showWithUndo(requireView(),
                getString(R.string.document_deleted),
                getString(R.string.undo),
                v -> {
                    // Re-insert with same data
                    dao.insert(docName, row[6], row[2], row[3], row[4], row[5],
                            "1".equals(row[7]));
                    loadDocuments();
                });
    }

    private void showAddDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_document, null);
        TextInputEditText etDocName = dialogView.findViewById(R.id.etDialogDocName);

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.add_document))
                .setView(dialogView)
                .setPositiveButton(getString(R.string.save), (dialog, which) -> {
                    String name = etDocName.getText() != null
                            ? etDocName.getText().toString().trim() : "";
                    if (!name.isEmpty()) {
                        dao.insert(name, "", DbHelper.STATUS_NOT_READY,
                                "", "", "", true);
                        loadDocuments();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
}
