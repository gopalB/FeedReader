package com.feedReader.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
/**
 * Will handle all database related operations on 
 * FeedSouce table.
 * @author Gopal Biyani
 *
 */
public class FeedSourceProvider {
	public static final String KEY_ROWID = "_id";
	public static final String FEED_NAME = "feed_name";
	public static final String FEED_URL = "feed_url";
	public static final String XML_ENCODING = "xml_encoding";
	public static final String FEED_COUNT = "feed_count";
	public static final String LAST_UPDATED = "last_updated";

	private final SQLiteDatabase mDB;
	//query to retrieve all 
    private static final String FetchFeedListQuery = "select _id,feed_name,ifNull(feed_count,0) as feed_count from feedList " +
    		"LEFT JOIN " +
    		"(select feed_id,count(*) as feed_count from feedDetail where isRead = 0 group by feed_id) as feed_detail " +
    		"on feedList._id = feed_detail.feed_id  where feedList._id != 1 " +
    		"union " +
    		"select _id,feed_name,ifNull(feed_count,0) as feed_count from feedList " +
    		"LEFT JOIN " +
    		"(select 1 as feed_id,count(*) as feed_count from feedDetail where starred = 1) as starred_detail " +
    		"on  feedList._id = starred_detail.feed_id where feedList._id = 1;";
    
    public static final int STARRED_ID = 1;
    
    public FeedSourceProvider(SQLiteDatabase db){
    	mDB = db;
    }
    
    /**
     * Create a new feed using the feed name,url and encoding provided. If the feed is
     * successfully created return the new rowId for that feed, otherwise return
     * a -1 to indicate failure.
     * 
     * @param feedName name of the feed
     * @param feedURL feed url
     * @param encoding used to parse xml
     */
    public long createFeed(String feedName,String feedURL,String encoding) {
        final ContentValues initialValues = new ContentValues();
        initialValues.put(FEED_NAME, feedName);
        initialValues.put(FEED_URL, feedURL);
        initialValues.put(XML_ENCODING, encoding);
        return mDB.insert(Tables.FEED_SOURCE, null, initialValues);
    }

    /**
     * Return a Cursor over the list of all feeds 
     * and there count in the database
     * 
     * @return Cursor over all feeds
     */
    public Cursor fetchFeedNameAndCount() {
    	return mDB.rawQuery(FetchFeedListQuery, null);
    }
    /**
     * Return a Cursor positioned at the feed that matches the given rowId
     * 
     * @param rowId id of feed to retrieve
     * @return Cursor positioned to matching feed, if found
     * @throws SQLException if feed could not be found/retrieved
     */
    public Cursor fetchFeed(int rowId) throws SQLException {
        final Cursor mCursor = mDB.query(true, Tables.FEED_SOURCE, new String[] {KEY_ROWID,
                    FEED_NAME, FEED_URL,XML_ENCODING}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    
    
}
