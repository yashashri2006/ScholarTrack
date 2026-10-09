package com.example.scholartrack.util;

// Unit 3: Custom Toast layout – demonstrates inflating a custom view for Toast

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scholartrack.R;

public final class ToastUtil {

    private ToastUtil() {}

    /** Shows a branded custom Toast with the ScholarTrack design. */
    public static void show(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_custom_toast, null);

        TextView tvMessage = view.findViewById(R.id.tvToastMessage);
        tvMessage.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150);
        toast.show();
    }

    /** Shows a longer custom Toast (for errors). */
    public static void showLong(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.layout_custom_toast, null);

        TextView tvMessage = view.findViewById(R.id.tvToastMessage);
        tvMessage.setText(message);

        Toast toast = new Toast(context);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 150);
        toast.show();
    }
}
