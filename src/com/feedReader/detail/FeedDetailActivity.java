package com.feedReader.detail;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.feedReader.R;
import com.feedReader.detail.quickAction.QuickAction;
import com.feedReader.provider.FeedDB;
import com.feedReader.provider.FeedDetailProvider;
import com.feedReader.provider.FeedSourceProvider;
import com.feedReader.social.facebook.FacebookProvider;
import com.feedReader.util.Constants;
/**
 * Activity class to show feeds of the source selected
 * by user.
 * @author Gopal Biyani
 *
 */
public class FeedDetailActivity extends Activity implements OnItemClickListener,OnClickListener{
	public static final String TITLE = "title";
	public static final String GUID = "guid";
	public static final String FEED_URL = "feed_url";
	public static final String ENCODING = "encoding";
	public static final String FEED_ROW_ID = "feed_RowId";
	private static final String POSITION = "position";

	private FeedDetailAdapter mFeedAdapter;
	private String mEncoding;//feed source encoding
	private String mURL;//feed source url
	private int mFeed_RowId;//feed source id
	private int FEED_COUNT = 20;//initial number of feed shown to user

	private QuickAction mQuickAction;
	private FeedDetailProvider mFeedDetailDB;
	private RSSHandler mRSSHandler;
	private ImageView mRefresh;
	private TextView mLoadMore;
	private Cursor mCursor;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		init();
	}

	private void init(){
//		Debug.startMethodTracing("Gopal");
		final Bundle bundle = this.getIntent().getExtras();
		setContentView(R.layout.feed_detail_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.feed_detail_custom_title);
		//set title
		final TextView title = (TextView)findViewById(R.feed_detail_custom_title.title);
		title.setText(bundle.getString(TITLE));
		//set refresh click listener
		mRefresh = (ImageView)findViewById(R.feed_detail_custom_title.refresh);
		mRefresh.setOnClickListener(this);
		
		mEncoding = bundle.getString(ENCODING);//get encoding
		mFeed_RowId = bundle.getInt(FEED_ROW_ID);//get feed source id
		mURL = bundle.getString(FEED_URL);//get feed source url

		mQuickAction = new QuickAction(this);

		final ListView feedListView = (ListView)findViewById(R.feed_detail_main.feedList);
		final LayoutInflater layoutInflater = (LayoutInflater) FeedDetailActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
		mLoadMore = (TextView)layoutInflater.inflate(R.layout.feed_detail_footer_more, null);
		feedListView.addFooterView(mLoadMore,null,true);

		new InitializeFeedDetailDB().execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//some issue with startManagedQuery
		//when user move from this activity to another
		//it resets cursor and list position to 0
		//so saving position in onPause and restoring here
		if(mCursor != null && mFeedAdapter != null){
			final SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
			final int savedPosition = sharedPreferences.getInt(POSITION, 0);
			mCursor.moveToPosition(savedPosition);
			final ListView listView = (ListView)findViewById(R.feed_detail_main.feedList);
			listView.setSelection(savedPosition);
		}	

	}

	@Override
	protected void onPause() {
		super.onPause();
		//some issue with startManagedQuery
		//when user move from this activity to another
		//it resets cursor and list position to 0
		//so saving position here and restoring in onPause
		if(mCursor != null){
			final SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
			final SharedPreferences.Editor editor = sharedPreferences.edit();
			editor.putInt(POSITION, mCursor.getPosition());
			editor.commit();
		}
		
//		if(isFinishing())
//			Debug.stopMethodTracing();
	}
	
	

	@Override
	public void onItemClick(AdapterView<?> listView, View view, int position, long arg3) {
		if(view.getId() != R.feed_detail_footer_more.load_more){
			if(mCursor != null){
				final Bundle params = new Bundle();
				params.putString(Constants.Facebook_Post_Message, mCursor.getString(mFeedAdapter.TITLE_INDEX));
				params.putString(Constants.Facebook_Post_Name, "Feed Reader Post");
				final int urlColIndex = mCursor.getColumnIndex(FeedDetailProvider.LINK);
				params.putString(Constants.Facebook_Post_Link, mCursor.getString(urlColIndex));
				params.putString(Constants.Facebook_Post_Description, mFeedDetailDB.getDescription(mCursor.getInt(mFeedAdapter.KEY_ROWID_INDEX)));
				//show quick action
				mQuickAction.show(view,params);
			}
		}	
		else{
			Log.d(Constants.FEED_READER_LOG,"In Load More");
			FEED_COUNT = FEED_COUNT + 20;//update load more count
			updateList();
		}	
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//update quick action
		mQuickAction.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		//user clicked to refresh feed
		case R.feed_detail_custom_title.refresh:
			//If it is Starred List Nothing to refresh
			if(mFeed_RowId != FeedSourceProvider.STARRED_ID){
				Log.d(Constants.FEED_READER_LOG, "User requested to refresh");
				delegateToRSSHandler();
			}
		}
	}
	/**
	 * Fetch feeds from feed source
	 */
	public void delegateToRSSHandler(){
		//first time
		if(mRSSHandler == null){
			mRSSHandler = new RSSHandler(mCursor, mRefresh, mFeedDetailDB, mFeed_RowId);
			mRSSHandler = (RSSHandler)mRSSHandler.execute(mURL,mEncoding);
		//check if previous task is finished	
		} else if(mRSSHandler.getStatus() == Status.FINISHED){
			Log.d(Constants.FEED_READER_LOG, "Old task is finished starting new one.");
			//previous task finished executing new one
			mRSSHandler = new RSSHandler(mCursor, mRefresh, mFeedDetailDB, mFeed_RowId);
			mRSSHandler = (RSSHandler)mRSSHandler.execute(mURL,mEncoding);
		}
	}
	/**
	 * Fetch data from FeedDetailProvider
	 * and update cursor
	 */
	public void updateList(){
		if(mCursor != null){
			//discard previous cursor
			stopManagingCursor(mCursor);
		}
		//starred feed source
		if(mFeed_RowId != FeedSourceProvider.STARRED_ID){
			mCursor =mFeedDetailDB.fetchFeedDetails(mFeed_RowId,FEED_COUNT);
		}	
		else{//feed source except starred
			mCursor = mFeedDetailDB.fetchStarredFeeds(FEED_COUNT);
		}
		//start managing new cursor
		startManagingCursor(mCursor);
		if(mFeedAdapter != null)
			mFeedAdapter.changeCursor(mCursor);//replace with new cursor
		
		Log.d(Constants.FEED_READER_LOG,"Update List");
		if(mCursor.getCount() == mFeedDetailDB.getCount(mFeed_RowId)){
			//No more feeds hide load more
			mLoadMore.setVisibility(View.GONE);
		} else {
			mLoadMore.setVisibility(View.VISIBLE);
		}
		
	}

	private class InitializeFeedDetailDB extends AsyncTask<Void, Void, Void>{
		@Override
		protected Void doInBackground(Void... params) {
			SQLiteDatabase db = ((FeedDB)getApplicationContext()).getDatabseHelper().getWritableDatabase();
			FeedDetailProvider feedDetailDB = new FeedDetailProvider(db);
			mFeedDetailDB = feedDetailDB;
			updateList();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			final ListView feedListView = (ListView)findViewById(R.feed_detail_main.feedList);
			mFeedAdapter = new FeedDetailAdapter(FeedDetailActivity.this,mCursor,mFeedDetailDB);
			feedListView.setAdapter(mFeedAdapter);

			feedListView.setOnItemClickListener(FeedDetailActivity.this);
			if(mFeed_RowId != FeedSourceProvider.STARRED_ID && mFeedDetailDB.needToFetch(mFeed_RowId)){
				delegateToRSSHandler();
			}	
		}
	}
}
