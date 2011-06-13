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

    public static void showToast(Context context, int id, boolean longToast) {
        Toast toast = Toast.makeText(context, id, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
        toast.show();
    }
    
    public static void showToast(Context context, String messageString, boolean longToast) {
        Toast toast = Toast.makeText(context, messageString, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, Gravity.CENTER_HORIZONTAL, Gravity.CENTER_VERTICAL);
        toast.show();
    }
}