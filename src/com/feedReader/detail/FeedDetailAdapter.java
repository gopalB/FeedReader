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
		mKeyRowId = cursor.getInt(KEY_ROWID_INDEX);
		//0 - Not Starred 1 - Starred
		isStarred = cursor.getInt(STARRED_INDEX) == 0 ? false : true;
		//0 - Not Read 1 - Read
		isRead = cursor.getInt(READ_INDEX) == 0 ? false : true;

		ViewHolder holder = (ViewHolder)view.getTag();
		
		final TextView feedTitle = holder.mFeedTitle;
		if (feedTitle != null) {
			feedTitle.setText(title);
		}

		final TextView feedLink = holder.mFeedLink;
		if (feedLink != null) {
			//set feed detail id to fetch details when user clicks
			//to read more
			feedLink.setTag(Integer.valueOf(mKeyRowId));
		}

		final TextView feedPubDate = holder.mFeedPubDate;
		if (feedPubDate != null) {
			mDate.setTime(mPubDate);
			feedPubDate.setText(mDateFormat.format(mDate));
		}

		final ImageView isReadView = holder.mReadView;
		if(isRead)
			isReadView.setVisibility(View.INVISIBLE);
		else
			isReadView.setVisibility(View.VISIBLE);

		final ImageView isStarredView = holder.mStarredView;
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
		
		final ViewHolder holder = new ViewHolder();
		//feedLink
		final TextView feedLink = (TextView) view.findViewById(R.feed_detail_list_item.readMore);
		feedLink.setOnClickListener(this);
		holder.mFeedLink = feedLink;
		//starred view
		final ImageView isStarredView = (ImageView) view.findViewById(R.feed_detail_list_item.starred);
		isStarredView.setOnClickListener(this);
		holder.mStarredView = isStarredView;
		//feed title
		final TextView feedTitle = (TextView) view.findViewById(R.feed_detail_list_item.title);
		holder.mFeedTitle = feedTitle;
		//read view
		final ImageView isReadView = (ImageView) view.findViewById(R.feed_detail_list_item.is_read);
		holder.mReadView = isReadView;
		//feed pub date
		final TextView feedPubDate = (TextView) view.findViewById(R.feed_detail_list_item.pubDate);
		holder.mFeedPubDate = feedPubDate;
		//settag as holder object
		view.setTag(holder);
		return view;
	}
	
	public static class ViewHolder{
		TextView mFeedLink;
		TextView mFeedTitle;
		TextView mFeedPubDate;
		ImageView mStarredView;
		ImageView mReadView;
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
			boolean isStarred = !((Boolean)v.getTag(R.feed_detail_list_item.starred));
			final ImageView isStarredView = (ImageView)v;
			
			if(isStarred)
				isStarredView.setImageResource(R.drawable.starred_on);
			else
				isStarredView.setImageResource(R.drawable.starred_off);
			isStarredView.invalidate();
			//mark feed as starred if it wasnt starred or view versa 
			feedDetailDB.markAsStarred(key_RowId, isStarred);
			getCursor().requery();
			break;
		}
	}
}
