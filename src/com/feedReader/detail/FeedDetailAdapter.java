package com.feedReader.detail;

import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.feedReader.R;
import com.feedReader.provider.FeedDetailProvider;
import com.feedReader.util.Constants;
import com.feedReader.util.ToastUtilities;
/**
 * 
 * @author Gopal Biyani
 *
 */
public class FeedDetailAdapter extends CursorAdapter implements OnClickListener{

	private final LayoutInflater mLayoutInflater;

	public final int TITLE_INDEX;
	public final int READ_INDEX;
	public final int PUB_DATE_INDEX;
	public final int KEY_ROWID_INDEX;
	public final int STARRED_INDEX;

	private final Context mContext;
	private final FeedDetailProvider mFeedDetailDB;
	
	private final Date mDate;
	private final java.text.DateFormat mDateFormat;
	
	private long mPubDate;
	private boolean isRead;
	private boolean isStarred;
	private int mKeyRowId;

	public FeedDetailAdapter(Context context, Cursor cursor,FeedDetailProvider feedDetailDB) {
		super(context, cursor);
		mContext = context;
		mLayoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mFeedDetailDB = feedDetailDB;

		TITLE_INDEX = cursor.getColumnIndex(FeedDetailProvider.TITLE);
		READ_INDEX = cursor.getColumnIndex(FeedDetailProvider.READ);
		PUB_DATE_INDEX = cursor.getColumnIndex(FeedDetailProvider.PUB_DATE);
		KEY_ROWID_INDEX = cursor.getColumnIndex(FeedDetailProvider.KEY_ROWID);
		STARRED_INDEX = cursor.getColumnIndex(FeedDetailProvider.STARRED);
		
		mDate = new Date();
		mDateFormat = DateFormat.getDateFormat(context);
	}

	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final String title = cursor.getString(TITLE_INDEX);
		mPubDate = cursor.getLong(PUB_DATE_INDEX);
		isRead = cursor.getInt(READ_INDEX) == 0 ? false : true;
		mKeyRowId = cursor.getInt(KEY_ROWID_INDEX);
		//0 - Not Starred 1 - Starred
		final boolean isStarred = cursor.getInt(STARRED_INDEX) == 0 ? false : true;

		view.setTag(mKeyRowId);
		
		final TextView feedTitle = (TextView) view.findViewById(R.feed_detail_list_item.title);
		if (feedTitle != null) {
			feedTitle.setText(title);
		}

		final TextView feedLink = (TextView) view.findViewById(R.feed_detail_list_item.readMore);
		if (feedLink != null) {
			//set feed detail id to fetch details when user clicks
			//to read more
			feedLink.setTag(Integer.valueOf(mKeyRowId));
		}

		final TextView feedPubDate = (TextView) view.findViewById(R.feed_detail_list_item.pubDate);
		if (feedPubDate != null) {
			mDate.setTime(mPubDate);
			feedPubDate.setText(mDateFormat.format(mDate));
		}

		final ImageView isReadView = (ImageView) view.findViewById(R.feed_detail_list_item.is_read);
		if(isRead)
			isReadView.setVisibility(View.INVISIBLE);
		else
			isReadView.setVisibility(View.VISIBLE);

		final ImageView isStarredView = (ImageView) view.findViewById(R.feed_detail_list_item.starred);
		//set feed detail id to tag starred feed
		isStarredView.setTag(Integer.valueOf(mKeyRowId));
		//tagged by user
		isStarredView.setTag(R.feed_detail_list_item.starred, Boolean.valueOf(isStarred));//onClick
		if(isStarred)
			isStarredView.setImageResource(R.drawable.starred_on);
		else
			isStarredView.setImageResource(R.drawable.starred_off);
	}

	@Override
	public View newView(Context context, final Cursor cursor, ViewGroup parent) {
		final View view = mLayoutInflater.inflate(R.layout.feed_detail_list_item,parent,false);
		final TextView feedLink = (TextView) view.findViewById(R.feed_detail_list_item.readMore);
		feedLink.setOnClickListener(this);
		final ImageView isStarredView = (ImageView) view.findViewById(R.feed_detail_list_item.starred);
		isStarredView.setOnClickListener(this);
		return view;
	}

	@Override
	public void onClick(View v) {
		//get feed detail id
		final int key_RowId =(Integer) v.getTag();
		final FeedDetailProvider feedDetailDB = mFeedDetailDB;
		switch(v.getId()){
		case R.feed_detail_list_item.readMore:
			feedDetailDB.markAsRead(key_RowId, true);
			final String url = feedDetailDB.getURL(key_RowId);
			if(url.equals("")){
				ToastUtilities.showToast(mContext, "No Link Found", false);
				return;
			}	
			Log.d(Constants.FEED_READER_LOG, FeedDetailProvider.FEED_ID + " = " + key_RowId);
			Log.d(Constants.FEED_READER_LOG, FeedDetailProvider.LINK + " = " + url);
			//Open feed source url on browser
			final Intent browserIntent = new Intent(Intent.ACTION_VIEW);
			browserIntent.setData(Uri.parse(url));
			mContext.startActivity(browserIntent);
			break;
		case R.feed_detail_list_item.starred:
			boolean isStarred = (Boolean)v.getTag(R.feed_detail_list_item.starred);
			//mark feed as starred if it wasnt starred or view versa 
			feedDetailDB.markAsStarred(key_RowId, !isStarred);
			getCursor().requery();
			break;
		}
	}
}
