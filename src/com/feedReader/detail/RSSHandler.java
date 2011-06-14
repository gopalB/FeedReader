package com.feedReader.detail;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.feedReader.R;
import com.feedReader.provider.FeedDetailProvider;
import com.feedReader.util.Constants;
import com.feedReader.util.HttpService;
import com.feedReader.util.ToastUtilities;
/**
 * Will handle the querying of URL and parsing of the
 * RSS feed. Will update the database and cursor to 
 * reflect back to the user.
 * Params : String type args[0] = urlString args[1] = encodingType 
 * Result : 0 - if Success else Error
 * @author Gopal Biyani
 *
 */

public class RSSHandler extends AsyncTask<String, Void,Integer>{
	private final Cursor mCursor;
	private final ImageView mRefresh;
	private final FeedDetailProvider mFeedDetailDB;
	private final int mFeedRowID;
	private final HttpService mHttpService;
	private final Context mContext;
	
	private static final String TITLE = "title";
	private static final String LINK = "link";
	private static final String ITEM = "item";
	private static final String GUID = "guid";
	private static final String PUB_DATE = "pubDate";
	private static final String DESCRIPTION = "description";
	
	public RSSHandler(Cursor cursor,ImageView refresh,FeedDetailProvider feedDetailDB,int feedRowID){
		mContext = refresh.getContext();
		mCursor = cursor;
		mRefresh = refresh;
		mFeedRowID = feedRowID;
		mFeedDetailDB = feedDetailDB;
		mHttpService = HttpService.getInstance();
	}
	/**
	 * runs on ui thread
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		//animate to show work in progress
		mRefresh.setImageResource(R.drawable.refresh_spinner);
		mRefresh.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.refresh_rotate));
	}
	
	@Override
	protected Integer doInBackground(String... args) {
		try{
			final HttpClient httpClient = mHttpService.getHttpClient();
			final HttpGet request = new HttpGet(args[0]);
			final HttpResponse response = httpClient.execute(request);
			//parse
			parse(response.getEntity().getContent(),args[1]);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			return R.string.feed_internet_error;//exception
		} catch (IOException e) {
			e.printStackTrace();
			return R.string.feed_internet_error;//exception
		} catch (IllegalStateException e) {
			e.printStackTrace();
			return R.string.feed_xml_error;//exception
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			return R.string.feed_xml_error;//exception
		}
		return 0;//Successfully parsed
	}
	/**
	 * runs on ui thread
	 */
	@Override
	protected void onPostExecute(Integer result) {
		super.onPostExecute(result);
		mRefresh.clearAnimation();
		mRefresh.setImageResource(R.drawable.refresh);
		if(result != 0){ //Error
			Log.e(Constants.FEED_READER_LOG,"RSSHandler : " + mContext.getString(result));
			ToastUtilities.showToast(mContext, result, true);
		}
	}
	/**
	 * runs on ui thread
	 */
	@Override
	protected void onProgressUpdate(Void... values) {
		super.onProgressUpdate(values);
		mCursor.requery();//update back
	}
	
	/**
	 * Parse XML using Pull Parser
	 * @param inputStream stream need to be parsed
	 * @param encoding xml encoding
	 * @throws IOException 
	 * @throws XmlPullParserException 
	 */
	public void parse(InputStream inputStream,String encoding) throws XmlPullParserException, IOException{
		final FeedDetailProvider feedDetailDB = mFeedDetailDB;
		final int feedRowId = mFeedRowID;
		FeedBean feedBean = null;

		XmlPullParser parser = Xml.newPullParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(new InputStreamReader(inputStream, encoding));

		int eventType = parser.getEventType();
		boolean done = false;
		while (eventType != XmlPullParser.END_DOCUMENT && !done){
			String name = null;
			switch (eventType){
			case XmlPullParser.START_DOCUMENT:
				break;
			case XmlPullParser.START_TAG:
				name = parser.getName();
				//item tag encountered
				if (name.equalsIgnoreCase(ITEM)){
					//create new bean item tag encountered
					feedBean = new FeedBean();
				} else if (feedBean != null){
					//parse link tag
					if (name.equalsIgnoreCase(LINK)){
						feedBean.setLink(parser.nextText());
					} else if(name.equalsIgnoreCase(GUID)){//parse guid tag
						feedBean.setGUID(parser.nextText());
					} else if (name.equalsIgnoreCase(PUB_DATE)){//parse pubDate
						feedBean.setPubDate(parser.nextText());
					} else if (name.equalsIgnoreCase(TITLE)){//parse title tag
						feedBean.setTitle(parser.nextText());
					} else if(name.equalsIgnoreCase(DESCRIPTION)){//parse description tag
						feedBean.setDescription(parser.nextText());
					}
				}
				break;
			case XmlPullParser.END_TAG:
				name = parser.getName();
				//end of item tag
				if (name.equalsIgnoreCase(ITEM) && feedBean != null){
					feedDetailDB.createFeed(feedRowId, feedBean);//Insert feed into feedDetail table
					publishProgress();//publish new feed to ui
				}	
				break;
			}
			eventType = parser.next();
		}
		//update last update time
		feedDetailDB.updateFetchTime(feedRowId);
	}

}
