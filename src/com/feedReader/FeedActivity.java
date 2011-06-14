package com.feedReader;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.feedReader.detail.FeedDetailActivity;
import com.feedReader.provider.FeedDB;
import com.feedReader.provider.FeedSourceProvider;
import com.feedReader.util.Constants;
/**
 * Feed Reader Main Activity will load all Feed Sources
 * @author Gopal Biyani
 */
public class FeedActivity extends Activity implements OnItemClickListener{
	private FeedAdapter mMainFeedAdapter;
	private FeedSourceProvider mFeedSourceProvider;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Constants.FEED_READER_LOG, "Feed Reader Launched");
		init();
	}

	private void init(){
		setContentView(R.layout.feed_main);
		new CreateFeedListDB().execute();
	}

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long arg3) {
		final FeedAdapter feedAdapter = (FeedAdapter) listView.getAdapter();
		final Cursor feedCursor = feedAdapter.getCursor();

		final int rowIndex = feedCursor.getColumnIndex(FeedSourceProvider.KEY_ROWID);
		final int feed_RowId = feedCursor.getInt(rowIndex);
		final Cursor feedDetailCursor = mFeedSourceProvider.fetchFeed(feed_RowId);

		final int nameIndex = feedDetailCursor.getColumnIndex(FeedSourceProvider.FEED_NAME);
		final int encodingColIndex = feedDetailCursor.getColumnIndex(FeedSourceProvider.XML_ENCODING);
		final int urlColIndex = feedDetailCursor.getColumnIndex(FeedSourceProvider.FEED_URL);

		final Intent intent = new Intent(this,FeedDetailActivity.class);
		final Bundle bundle = new Bundle();
		//URL
		bundle.putString(FeedDetailActivity.FEED_URL,feedDetailCursor.getString(urlColIndex));
		//title
		final String title = feedDetailCursor.getString(nameIndex);
		bundle.putString(FeedDetailActivity.TITLE, title);
		//encoding
		bundle.putString(FeedDetailActivity.ENCODING,feedDetailCursor.getString(encodingColIndex));
		bundle.putInt(FeedDetailActivity.FEED_ROW_ID, feed_RowId);
		intent.putExtras(bundle);
		
		//close cursor
		feedDetailCursor.close();
		//start feed detail activity
		Log.d(Constants.FEED_READER_LOG, "Feed Requested for " + title);
		startActivity(intent);
	}


	private class CreateFeedListDB extends AsyncTask<Void, Void, Cursor>{
		@Override
		protected Cursor doInBackground(Void... params) {
			return initializeFeedSource();
		}
		@Override
		protected void onPostExecute(Cursor result) {
			super.onPostExecute(result);
			startManagingCursor(result);
			//Set Adapter and OnItemClickListener
			mMainFeedAdapter = new FeedAdapter(FeedActivity.this,result);
			ListView mainFeedList = (ListView)findViewById(R.feed_main.FeedList);
			mainFeedList.setAdapter(mMainFeedAdapter);
			mainFeedList.setOnItemClickListener(FeedActivity.this);
		}
		/**
		 * Get instance of writable database and will populate the feedSource
		 * table if not populated. 
		 * @return Cursor over feedSource table
		 */
		private Cursor initializeFeedSource(){
			final SQLiteDatabase db = ((FeedDB)getApplicationContext()).getDatabseHelper().getWritableDatabase();
			mFeedSourceProvider = new FeedSourceProvider(db);
			Cursor cursor = mFeedSourceProvider.fetchFeedNameAndCount();
			//Starred
			if(cursor.getCount() <= 1){
				//Nothing in DB Read Feed Source from resource
				//and add into database
				Resources resource = getResources();
				final String[] feedNameArray = resource.getStringArray(R.array.feed_name);
				final String[] feedUrlArray = resource.getStringArray(R.array.feed_url);
				final String[] feedEncoding = resource.getStringArray(R.array.encoding);

				for(int i=0;i<feedNameArray.length;i++){
					mFeedSourceProvider.createFeed(feedNameArray[i],feedUrlArray[i],feedEncoding[i]);
				}
				cursor = mFeedSourceProvider.fetchFeedNameAndCount();
			}
			return cursor;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(isFinishing()){
			Log.d(Constants.FEED_READER_LOG, "Exiting from Feed Reader");
			((FeedDB)getApplicationContext()).closeDatabaseHelper();
		}
	}
}