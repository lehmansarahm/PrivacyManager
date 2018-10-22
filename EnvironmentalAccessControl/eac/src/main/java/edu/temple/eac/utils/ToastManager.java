package edu.temple.eac.utils;

import android.app.Activity;
import android.widget.Toast;

public class ToastManager {
    /**
     * General purpose method to display a Toast message on top of the current activity for a short
     * period of time.  Used primarily for debugging.
     * @param message the message to display
     */
    public static void showShortToast(final Activity currentActivity, final String message) {
        if (currentActivity != null && message != null) {
            currentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * General purpose method to display a Toast message on top of the current activity for a longer
     * period of time.  Used primarily for debugging.
     * @param message the message to display
     */
    public static void showLongToast(final Activity currentActivity, final String message) {
        if (currentActivity != null && message != null) {
            currentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(currentActivity, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}