package com.feedReader.detail.quickAction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.feedReader.R;
import com.feedReader.social.facebook.FacebookProvider;
import com.feedReader.util.Constants;
/**
 * Popup mPopWindow, shows action list as icon and text like the one in Gallery3D app. 
 * 
 */
public class QuickAction extends CustomPopupWindow implements OnClickListener {
	private View mRoot;
	private ImageView mArrowUp;
	private ImageView mArrowDown;
	private final FacebookProvider mFacebookProvider;
	private Bundle mBundle;
	private TextView mFacebookView; 
	/**
	 * Constructor
	 * 
	 * @param anchor {@link View} on where the popup mPopWindow should be displayed
	 */
	public QuickAction(Activity feedDetailActivity) {
		super(feedDetailActivity);
		mFacebookProvider = new FacebookProvider(feedDetailActivity);
	}

	@Override
	protected void onCreate() {
		super.onCreate();

		final LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		final ViewGroup root = (ViewGroup) inflater.inflate(R.layout.feed_quick_action, null);
		
		mRoot = root;
		mArrowDown = (ImageView) root.findViewById(R.feed_qa.arrow_down);
		mArrowUp = (ImageView) root.findViewById(R.feed_qa.arrow_up);
		
		mFacebookView = (TextView)root.findViewById(R.feed_qa.facebook);
		mFacebookView.setOnClickListener(this);

		setContentView(root);

		final ViewGroup actionViewLayout = (ViewGroup)mRoot.findViewById(R.feed_qa.actionViewLayout);
		final int childCount = actionViewLayout.getChildCount();
		View view;
		for(int i=0;i<childCount;i++){
			view = actionViewLayout.getChildAt(i);
			if(view instanceof ImageView){
				view.setOnClickListener(this);
			}
		}
	}
	/**
	 * Show popup mPopWindow. Popup is automatically positioned, on top or bottom of anchor view.
	 * 
	 */
	public void show(View anchor,Bundle messageBundle) {
		preShow();
		
		if(!mFacebookProvider.isSessionValid()){
			mFacebookView.setText(R.string.facebook_login);
		} else {
			mFacebookView.setText(R.string.facebook_post);
		}
		
		mBundle = messageBundle;
		
		int xPos, yPos;
		final View root = mRoot;
		final int[] location = new int[2];

		anchor.getLocationOnScreen(location);
		final Rect anchorRect = new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight());
		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		final int rootHeight = root.getMeasuredHeight();
		final int rootWidth = root.getMeasuredWidth();
		final int screenWidth = mWindowManager.getDefaultDisplay().getWidth();
		final int screenHeight = mWindowManager.getDefaultDisplay().getHeight();

		//automatically get X coord of popup (top left)
		if ((anchorRect.left + rootWidth) > screenWidth) {
			xPos = anchorRect.left - (rootWidth-anchor.getWidth());
		} else {
			if (anchor.getWidth() > rootWidth) {
				xPos = anchorRect.centerX() - (rootWidth/2);
			} else {
				xPos = anchorRect.left;
			}
		}

		final int dyTop = anchorRect.top;
		final int dyBottom = screenHeight - anchorRect.bottom;
		final boolean onTop = (dyTop > dyBottom) ? true : false;

		final HorizontalScrollView scroller = (HorizontalScrollView) root.findViewById(R.feed_qa.scroller);
		if (onTop) {
			if (rootHeight > dyTop) {
				yPos = 15;
				LayoutParams l 	= scroller.getLayoutParams();
				l.height = dyTop - anchor.getHeight();
			} else {
				yPos = anchorRect.top - rootHeight;
			}
		} else {
			yPos = anchorRect.bottom;
			if (rootHeight > dyBottom) { 
				LayoutParams l 	= scroller.getLayoutParams();
				l.height		= dyBottom;
			}
		}

		showArrow(((onTop) ? R.feed_qa.arrow_down : R.feed_qa.arrow_up), anchorRect.centerX()-xPos);
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);
		mPopWindow.showAtLocation(anchor, Gravity.NO_GRAVITY, xPos, yPos);
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		mFacebookProvider.onActivityResult(requestCode, requestCode, data);
	}

	/**
	 * Set animation style
	 * 
	 * @param screenWidth screen width
	 * @param requestedX distance from left edge
	 * @param onTop flag to indicate where the popup should be displayed. Set TRUE if displayed on top of anchor view
	 * 		  and vice versa
	 */
	private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
		int arrowPos = requestedX - mArrowUp.getMeasuredWidth()/2;
		if (arrowPos <= screenWidth/4) {
			mPopWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left : R.style.Animations_PopDownMenu_Left);
		} else if (arrowPos > screenWidth/4 && arrowPos < 3 * (screenWidth/4)) {
			mPopWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center : R.style.Animations_PopDownMenu_Center);
		} else {
			mPopWindow.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right : R.style.Animations_PopDownMenu_Right);
		}
	}

	/**
	 * Show arrow
	 * 
	 * @param whichArrow arrow type resource id
	 * @param requestedX distance from left screen
	 */
	private void showArrow(int whichArrow, int requestedX) {
		final View showArrow = (whichArrow == R.feed_qa.arrow_up) ? mArrowUp : mArrowDown;
		final View hideArrow = (whichArrow == R.feed_qa.arrow_up) ? mArrowDown : mArrowUp;

		final int arrowWidth = mArrowUp.getMeasuredWidth();
		showArrow.setVisibility(View.VISIBLE);
		ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams)showArrow.getLayoutParams();
		param.leftMargin = requestedX - arrowWidth / 2;
		hideArrow.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.feed_qa.facebook:
			Log.d(Constants.FEED_READER_LOG, "User clicked on facebook - Quick Action");
			mFacebookProvider.post(mBundle);
			break;
		}
		
		mPopWindow.dismiss();
	}
}