package com.example.scholartrack.ui.adapter;

// Unit 3: StudentListAdapter – custom BaseAdapter for the coordinator's student list.

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.scholartrack.R;
import com.example.scholartrack.util.Constants;

import java.util.List;

public class StudentListAdapter extends BaseAdapter {

    private final Context      context;
    private       List<String[]> data;  // [uid, name, rollNo, stage]

    public StudentListAdapter(Context ctx, List<String[]> data) {
        this.context = ctx;
        this.data    = data;
    }

    public void updateData(List<String[]> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    public String[] getItemAt(int pos) {
        if (pos < 0 || pos >= data.size()) return null;
        return data.get(pos);
    }

    @Override public int getCount()          { return data.size(); }
    @Override public Object getItem(int pos) { return data.get(pos); }
    @Override public long getItemId(int pos) { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_student, parent, false);
        }

        String[] row  = data.get(position);
        int stage = Integer.parseInt(row[3]);

        ((TextView) convertView.findViewById(R.id.tvStudentName)).setText(row[1]);
        ((TextView) convertView.findViewById(R.id.tvStudentRoll)).setText(row[2]);

        TextView tvChip = convertView.findViewById(R.id.tvStageChip);
        tvChip.setText("Stage " + stage);

        ProgressBar pb = convertView.findViewById(R.id.pbStudentStage);
        pb.setMax(Constants.TOTAL_STAGES);
        pb.setProgress(stage);

        return convertView;
    }
}
