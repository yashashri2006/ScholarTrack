package com.example.scholartrack.ui;

// Unit 6: SchemeResultsActivity – ListView with custom SchemeAdapter,
//         "Open MahaDBT" implicit Intent, empty state, warning chip, detail dialog.
// Unit 3: ListView with custom adapter (SchemeAdapter), empty state.
// Unit 2: Implicit Intent (ACTION_VIEW) to MahaDBT portal.

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.scholartrack.R;
import com.example.scholartrack.data.firebase.EligibilityEngine;
import com.example.scholartrack.data.firebase.SchemeRepository;
import com.example.scholartrack.data.models.Scheme;
import com.example.scholartrack.data.sqlite.DocumentDao;
import com.example.scholartrack.ui.adapter.SchemeAdapter;
import com.example.scholartrack.util.Constants;
import com.example.scholartrack.util.SnackbarUtil;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class SchemeResultsActivity extends BaseActivity {

    private ListView      listSchemes;
    private TextView      txtEmpty, txtCompiledOn;
    private MaterialButton btnOpenMahaDBT;

    private List<Scheme>  eligibleSchemes;
    private SchemeAdapter adapter;
    private DocumentDao   documentDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scheme_results);

        listSchemes    = findViewById(R.id.listSchemes);
        txtEmpty       = findViewById(R.id.txtEmptySchemes);
        txtCompiledOn  = findViewById(R.id.txtCompiledOn);
        btnOpenMahaDBT = findViewById(R.id.btnOpenMahaDBT);

        documentDao = new DocumentDao(this);

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Extract extras
        String  category  = getIntent().getStringExtra(Constants.EXTRA_CATEGORY);
        long    income    = getIntent().getLongExtra(Constants.EXTRA_INCOME, 0);
        boolean isFemale  = getIntent().getBooleanExtra(Constants.EXTRA_FEMALE,   false);
        boolean isMinority = getIntent().getBooleanExtra(Constants.EXTRA_MINORITY, false);
        boolean isPwd     = getIntent().getBooleanExtra(Constants.EXTRA_PWD,       false);
        boolean isHostel  = getIntent().getBooleanExtra(Constants.EXTRA_HOSTEL,    false);

        // Unit 6: JSON parsing happens inside SchemeRepository constructor
        SchemeRepository repo = new SchemeRepository(this);
        EligibilityEngine engine = new EligibilityEngine();
        eligibleSchemes = engine.filter(repo.getAll(), category, income,
                isFemale, isMinority, isPwd, isHostel);

        String compiledOn = repo.getCompiledOn();
        if (!compiledOn.isEmpty()) {
            txtCompiledOn.setText(getString(R.string.compiled_on, compiledOn));
        }

        setupList();

        // Unit 2: Implicit Intent to open MahaDBT URL
        btnOpenMahaDBT.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(Constants.MAHADBT_URL));
            startActivity(browserIntent);
        });
    }

    private void setupList() {
        if (eligibleSchemes.isEmpty()) {
            txtEmpty.setVisibility(View.VISIBLE);
            listSchemes.setVisibility(View.GONE);
        } else {
            txtEmpty.setVisibility(View.GONE);
            listSchemes.setVisibility(View.VISIBLE);

            adapter = new SchemeAdapter(this, eligibleSchemes,
                    scheme -> showSchemeDetail(scheme),
                    scheme -> addDocuments(scheme));
            listSchemes.setAdapter(adapter);
        }
    }

    private void showSchemeDetail(Scheme scheme) {
        StringBuilder sb = new StringBuilder();
        sb.append("Department: ").append(scheme.department).append("\n\n");
        sb.append("Benefit: ").append(scheme.benefit).append("\n\n");
        if (scheme.maxIncome > 0) {
            sb.append("Max family income: ₹").append(scheme.maxIncome).append("\n\n");
        }
        sb.append("Required Documents:\n");
        for (String doc : scheme.documents) {
            sb.append("  • ").append(doc).append("\n");
        }
        if (scheme.verify) {
            sb.append("\n⚠ ").append(getString(R.string.verify_warning));
        }

        new AlertDialog.Builder(this)
                .setTitle(scheme.name)
                .setMessage(sb.toString())
                .setPositiveButton(getString(R.string.open_mahadbt), (d, w) -> {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse(Constants.MAHADBT_URL)));
                })
                .setNegativeButton(getString(R.string.close), null)
                .show();
    }

    private void addDocuments(Scheme scheme) {
        documentDao.insertBulk(scheme.documents, scheme.id);
        SnackbarUtil.show(listSchemes, getString(R.string.documents_added));
    }
}
