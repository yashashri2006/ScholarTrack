package com.example.scholartrack.ui.adapter;

// Unit 3: Custom ListView adapter for the Documents list.
//         Demonstrates getView with ViewHolder pattern, status chips (colored TextView).

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.scholartrack.R;
import com.example.scholartrack.data.sqlite.DbHelper;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class DocumentAdapter extends BaseAdapter {

    public interface OnStatusClickListener  { void onClick(String[] row); }
    public interface OnDeleteClickListener  { void onClick(String[] row, int pos); }

    private final Context              context;
    private       List<String[]>       data;
    private final OnStatusClickListener statusListener;
    private final OnDeleteClickListener deleteListener;

    public DocumentAdapter(Context context, List<String[]> data,
                           OnStatusClickListener statusListener,
                           OnDeleteClickListener deleteListener) {
        this.context        = context;
        this.data           = data;
        this.statusListener = statusListener;
        this.deleteListener = deleteListener;
    }

    public void updateData(List<String[]> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @Override public int getCount()             { return data.size(); }
    @Override public Object getItem(int pos)    { return data.get(pos); }
    @Override public long getItemId(int pos)    { return pos; }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_document, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String[] row = data.get(position);
        String name   = row[1];
        String status = row[2];
        String date   = row[3];
        String office = row[4];
        String note   = row[5];

        holder.tvIndex.setText(String.format("%02d", position + 1));
        holder.tvName.setText(name);

        if (note != null && !note.isEmpty()) {
            holder.tvCondition.setText(note);
            holder.tvCondition.setVisibility(View.VISIBLE);
        } else {
            holder.tvCondition.setVisibility(View.GONE);
        }
        holder.tvStatus.setText(statusLabel(context, status));
        holder.tvStatus.setBackgroundColor(statusBg(status));
        holder.tvStatus.setTextColor(statusFg(status));

        if (!date.isEmpty()) {
            holder.tvDetail.setText(office + " – " + date);
            holder.tvDetail.setVisibility(View.VISIBLE);
        } else {
            holder.tvDetail.setVisibility(View.GONE);
        }

        holder.btnChangeStatus.setOnClickListener(v -> statusListener.onClick(row));
        holder.btnDelete.setOnClickListener(v -> deleteListener.onClick(row, position));

        return convertView;
    }

    private static String statusLabel(Context ctx, String status) {
        switch (status) {
            case DbHelper.STATUS_READY:           return ctx.getString(R.string.status_ready);
            case DbHelper.STATUS_APPLIED_TO_MAKE: return ctx.getString(R.string.status_applied_to_make);
            case DbHelper.STATUS_RECEIVED:        return ctx.getString(R.string.status_received);
            default:                               return ctx.getString(R.string.status_not_ready);
        }
    }

    private static int statusBg(String status) {
        switch (status) {
            case DbHelper.STATUS_READY:           return Color.parseColor("#E8F5E9");
            case DbHelper.STATUS_APPLIED_TO_MAKE: return Color.parseColor("#FFF8E1");
            case DbHelper.STATUS_RECEIVED:        return Color.parseColor("#E3F2FD");
            default:                               return Color.parseColor("#F5F5F5");
        }
    }

    private static int statusFg(String status) {
        switch (status) {
            case DbHelper.STATUS_READY:           return Color.parseColor("#388E3C");
            case DbHelper.STATUS_APPLIED_TO_MAKE: return Color.parseColor("#F57F17");
            case DbHelper.STATUS_RECEIVED:        return Color.parseColor("#1565C0");
            default:                               return Color.parseColor("#616161");
        }
    }

    static class ViewHolder {
        final TextView       tvIndex, tvName, tvStatus, tvDetail, tvCondition;
        final MaterialButton btnChangeStatus, btnDelete;

        ViewHolder(View v) {
            tvIndex        = v.findViewById(R.id.tvDocIndex);
            tvName         = v.findViewById(R.id.tvDocName);
            tvCondition    = v.findViewById(R.id.tvDocCondition);
            tvStatus       = v.findViewById(R.id.tvDocStatus);
            tvDetail       = v.findViewById(R.id.tvDocDetail);
            btnChangeStatus = v.findViewById(R.id.btnChangeStatus);
            btnDelete      = v.findViewById(R.id.btnDeleteDoc);
        }
    }
}
