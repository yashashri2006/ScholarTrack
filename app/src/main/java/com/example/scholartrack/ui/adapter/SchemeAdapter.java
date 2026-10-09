package com.example.scholartrack.ui.adapter;

// Unit 3: SchemeAdapter – custom ListView adapter for eligible schemes list.
//         Shows verify warning chip and document-add button.

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.scholartrack.R;
import com.example.scholartrack.data.models.Scheme;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;

public class SchemeAdapter extends BaseAdapter {

    public interface OnDetailClickListener   { void onClick(Scheme scheme); }
    public interface OnAddDocsClickListener  { void onClick(Scheme scheme); }

    private final Context               context;
    private final List<Scheme>          data;
    private final OnDetailClickListener detailListener;
    private final OnAddDocsClickListener addDocsListener;

    public SchemeAdapter(Context context, List<Scheme> data,
                         OnDetailClickListener detailListener,
                         OnAddDocsClickListener addDocsListener) {
        this.context         = context;
        this.data            = data;
        this.detailListener  = detailListener;
        this.addDocsListener = addDocsListener;
    }

    @Override public int getCount()          { return data.size(); }
    @Override public Object getItem(int pos) { return data.get(pos); }
    @Override public long getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_scheme, parent, false);
        }

        Scheme scheme = data.get(position);

        TextView tvName   = convertView.findViewById(R.id.tvSchemeName);
        TextView tvDept   = convertView.findViewById(R.id.tvSchemeDept);
        Chip     chipVerify = convertView.findViewById(R.id.chipVerify);
        MaterialButton btnDetail = convertView.findViewById(R.id.btnSchemeDetail);
        MaterialButton btnAddDocs = convertView.findViewById(R.id.btnAddDocs);

        tvName.setText(scheme.name);
        tvDept.setText(scheme.department);
        chipVerify.setVisibility(scheme.verify ? View.VISIBLE : View.GONE);

        btnDetail.setOnClickListener(v  -> detailListener.onClick(scheme));
        btnAddDocs.setOnClickListener(v -> addDocsListener.onClick(scheme));

        return convertView;
    }
}
