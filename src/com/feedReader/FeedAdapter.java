package com.feedReader;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.feedReader.provider.FeedSourceProvider;
/**
 * 
 * @author Gopal Biyani
 *
 */
public class FeedAdapter extends CursorAdapter{
	private LayoutInflater mLayoutInflater;
	private final int  NAME_COL_INDEX;
	private final int  COUNT_COL_INDEX;
	public FeedAdapter(Context context , Cursor cursor) {
		super(context, cursor);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		NAME_COL_INDEX = cursor.getColumnIndex(FeedSourceProvider.FEED_NAME);
		COUNT_COL_INDEX = cursor.getColumnIndex(FeedSourceProvider.FEED_COUNT);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final String name = cursor.getString(NAME_COL_INDEX);
		final int feed_count = cursor.getInt(COUNT_COL_INDEX);
		ViewHolder holder = (ViewHolder)view.getTag();
		
		final TextView name_text = holder.mFeedName;
		if (name_text != null) {
			name_text.setText(name);
		}
		
		final TextView feedCountText = holder.mFeedCount;
		if(feedCountText != null && feed_count > 0){
			feedCountText.setText(String.valueOf(feed_count));
		}
	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = mLayoutInflater.inflate(R.layout.feed_list_item,parent,false);
		ViewHolder holder = new ViewHolder();
		//feed name
		final TextView feedNameText = (TextView) view.findViewById(R.feed_list.co_feed_name);
		holder.mFeedName = feedNameText;
		//feed count
		final TextView feedCountText = (TextView)view.findViewById(R.feed_list.feed_count);
		holder.mFeedCount = feedCountText;
		//set tag
		view.setTag(holder);
		return view;
	}
	
	public static class ViewHolder{
		TextView mFeedName;
		TextView mFeedCount;
	}
}
