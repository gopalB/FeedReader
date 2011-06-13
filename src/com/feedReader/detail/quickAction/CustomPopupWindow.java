package com.feedReader.detail.quickAction;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.feedReader.R;

public class CustomPopupWindow {
	protected final Context mContext;
	protected final PopupWindow window;
	private View root;
	private Drawable background = null;
	protected final WindowManager windowManager;
	
	/**
	 * Create a QuickAction
	 * 
	 * @param anchor
	 *            the view that the QuickAction will be displaying 'from'
	 */
	public CustomPopupWindow(Context context) {
		this.window = new PopupWindow(context);
		mContext = context;
		// when a touch even happens outside of the window
		// make the window go away
		window.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					CustomPopupWindow.this.window.dismiss();
					return true;
				}
				
				return false;
			}
		});

		windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		
		onCreate();
	}

	/**
	 * Anything you want to have happen when created. Probably should create a view and setup the event listeners on
	 * child views.
	 */
	protected void onCreate() {};

	protected void preShow() {
		if (root == null) {
			throw new IllegalStateException("setContentView was not called with a view to display.");
		}

		if (background == null) {
			window.setBackgroundDrawable(new BitmapDrawable());
		} else {
			window.setBackgroundDrawable(background);
		}

		// if using PopupWindow#setBackgroundDrawable this is the only values of the width and hight that make it work
		// otherwise you need to set the background of the root viewgroup
		// and set the popupwindow background to an empty BitmapDrawable
		window.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		window.setTouchable(true);
		window.setFocusable(true);
		window.setOutsideTouchable(true);

		window.setContentView(root);
	}


	/**
	 * Sets the content view. Probably should be called from {@link onCreate}
	 * 
	 * @param root
	 *            the view the popup will display
	 */
	public void setContentView(View root) {
		this.root = root;
		window.setContentView(root);
	}
}