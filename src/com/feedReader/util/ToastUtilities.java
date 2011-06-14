package com.feedReader.util;

import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;
/**
 * 
 * @author Gopal Biyani
 *
 */
public class ToastUtilities {
    private ToastUtilities() {
    }
    /**
     * Method to show toast at center of the screen
     * @param context
     * @param id string resource id
     * @param longToast show toast for long time if true
     */
    public static void showToast(Context context, int id, boolean longToast) {
        final Toast toast = Toast.makeText(context, id, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
        toast.show();
    }
    /**
     * Method to show toast at center of the screen
     * @param context
     * @param messageString toast message
     * @param longToast show toast for long time if true
     */
    public static void showToast(Context context, String messageString, boolean longToast) {
        final Toast toast = Toast.makeText(context, messageString, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
        toast.show();
    }
}