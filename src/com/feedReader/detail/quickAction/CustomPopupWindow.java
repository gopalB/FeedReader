package com.feedReader.detail.quickAction;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.style.BackgroundColorSpan;
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
	protected final PopupWindow mPopWindow;
	private View mRootView;
	protected final WindowManager mWindowManager;
	
	/**
	 * Create a QuickAction
	 * 
	 * @param anchor
	 *            the view that the QuickAction will be displaying 'from'
	 */
	public CustomPopupWindow(Context context) {
		mContext = context;

		final PopupWindow popWindow = new PopupWindow(context);
		popWindow.setBackgroundDrawable(new BitmapDrawable());
		popWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		popWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		popWindow.setTouchable(true);
		popWindow.setFocusable(true);
		popWindow.setOutsideTouchable(true);
		// when a touch even happens outside of the mPopWindow
		// make the mPopWindow go away
		popWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					mPopWindow.dismiss();
					return true;
				}
				return false;
			}
		});

		mPopWindow = popWindow;
		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		onCreate();
	}

	/**
	 * Anything you want to have happen when created. Probably should create a view and setup the event listeners on
	 * child views.
	 */
	protected void onCreate() {};

	protected void preShow() {
		if (mRootView == null) {
			throw new IllegalStateException("setContentView was not called with a view to display.");
		}
	}


	/**
	 * Sets the content view. Probably should be called from {@link onCreate}
	 * 
	 * @param mRootView
	 *            the view the popup will display
	 */
	public void setContentView(View root) {
		this.mRootView = root;
		mPopWindow.setContentView(root);
	}
}