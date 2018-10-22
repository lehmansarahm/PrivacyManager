package edu.temple.eac.utils;

import android.app.Activity;
import android.app.ProgressDialog;

/**
 *
 */
public class DialogManager {

    private static ProgressDialog dialog;

    /**
     *
     * @param title
     * @param message
     */
    public static void show(final Activity currentActivity, final String title, final String message) {
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog = new ProgressDialog(currentActivity);
                dialog.setTitle(title);
                dialog.setMessage(message);
                dialog.setCancelable(false); // disable dismiss by tapping outside of the dialog
                dialog.show();
            }
        });
    }

    /**
     *
     * @param currentActivity
     */
    public static void hide(final Activity currentActivity) {
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dialog.hide();
            }
        });
    }

    /**
     *
     * @param currentActivity
     * @param message
     */
    public static void hide(final Activity currentActivity, final String message) {
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialog != null) dialog.hide();
                ToastManager.showShortToast(currentActivity, message);
            }
        });
    }

}