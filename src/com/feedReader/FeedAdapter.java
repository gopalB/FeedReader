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
	private final int  mNameColIndex;
	private final int  mCountColIndex;
	public FeedAdapter(Context context , Cursor cursor) {
		super(context, cursor);
		mLayoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mNameColIndex = cursor.getColumnIndex(FeedSourceProvider.FEED_NAME);
		mCountColIndex = cursor.getColumnIndex(FeedSourceProvider.FEED_COUNT);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final String name = cursor.getString(mNameColIndex);
		final TextView name_text = (TextView) view.findViewById(R.feed_list.co_feed_name);
		if (name_text != null) {
			name_text.setText(name);
		}
		
		final int feed_count = cursor.getInt(mCountColIndex);
		final TextView feedCountText = (TextView)view.findViewById(R.feed_list.feed_count);
		if(feedCountText != null && feed_count > 0){
			feedCountText.setText(String.valueOf(feed_count));
		}
	}
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		final View view = mLayoutInflater.inflate(R.layout.feed_list_item,parent,false);
//		System.out.println("count >> " + cursor.getInt(mCountColIndex));
		final String name = cursor.getString(mNameColIndex);
		final TextView feedNameText = (TextView) view.findViewById(R.feed_list.co_feed_name);
		if (feedNameText != null) {
			feedNameText.setText(name);
		}
		
		final int feed_count = cursor.getInt(mCountColIndex);
		final TextView feedCountText = (TextView)view.findViewById(R.feed_list.feed_count);
		if(feedCountText != null && feed_count > 0){
			feedCountText.setText(String.valueOf(feed_count));
		}
		
		return view;
	}


}
