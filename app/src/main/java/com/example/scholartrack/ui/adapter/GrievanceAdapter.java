package com.example.scholartrack.ui.adapter;

// Unit 3: GrievanceAdapter – custom ListView adapter for saved grievance history.

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.scholartrack.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class GrievanceAdapter extends BaseAdapter {

    public interface OnAction { void onClick(String[] row); }

    private final Context      context;
    private       List<String[]> data;
    private final OnAction     reopenListener;
    private final OnAction     sentListener;

    public GrievanceAdapter(Context ctx, List<String[]> data,
                            OnAction reopenListener, OnAction sentListener) {
        this.context        = ctx;
        this.data           = data;
        this.reopenListener = reopenListener;
        this.sentListener   = sentListener;
    }

    @Override public int getCount()          { return data.size(); }
    @Override public Object getItem(int pos) { return data.get(pos); }
    @Override public long getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_grievance, parent, false);
        }
        String[] row = data.get(position);
        // [0]=id, [1]=createdAt, [2]=issueType, [3]=body, [4]=status

        ((TextView) convertView.findViewById(R.id.tvGrievDate)).setText(row[1]);
        ((TextView) convertView.findViewById(R.id.tvGrievIssue)).setText(row[2]);
        ((TextView) convertView.findViewById(R.id.tvGrievStatus)).setText(row[4]);

        MaterialButton btnReopen = convertView.findViewById(R.id.btnReopenGrievance);
        MaterialButton btnMarkSent = convertView.findViewById(R.id.btnMarkSent);

        btnReopen.setOnClickListener(v   -> reopenListener.onClick(row));
        btnMarkSent.setOnClickListener(v -> sentListener.onClick(row));
        btnMarkSent.setVisibility("DRAFT".equals(row[4]) ? View.VISIBLE : View.GONE);

        return convertView;
    }
}
