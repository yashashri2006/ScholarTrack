package com.example.scholartrack.util;

// Unit 3: Snackbar helper – standardised error and undo Snackbars

import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public final class SnackbarUtil {

    private SnackbarUtil() {}

    /** Short informational Snackbar. */
    public static void show(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_SHORT).show();
    }

    /** Long error Snackbar with red action. */
    public static void showError(View anchor, String message) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Snackbar with an Undo action – used in document deletion.
     * Unit 5: Demonstrates reversible SQLite delete.
     */
    public static void showWithUndo(View anchor, String message,
                                    String actionLabel,
                                    View.OnClickListener undoListener) {
        Snackbar.make(anchor, message, Snackbar.LENGTH_LONG)
                .setAction(actionLabel, undoListener)
                .show();
    }
}
