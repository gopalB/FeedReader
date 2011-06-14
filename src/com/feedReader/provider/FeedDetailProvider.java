package com.feedReader.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.feedReader.detail.FeedBean;
import com.feedReader.util.Constants;
/**
 * Will handle all database related operations on 
 * FeedDetail table.
 * @author Gopal Biyani
 *
 */
public class FeedDetailProvider {
	public static final String KEY_ROWID = "_id";
	public static final String FEED_ID = "feed_id";//feed source id fk @Tables.FEED_SOURCE
	public static final String LINK = "link";
	public static final String TITLE = "title";
	public static final String PUB_DATE = "pub_date";
	public static final String GUID = "guid";
	public static final String READ = "isRead";//0 - Not Read, 1 - Read
	public static final String STARRED = "starred";//0 - Not Starred, 1 - Starred 
	public static final String DESCRIPTION = "description";

	private final SQLiteDatabase mDB;
    private final String[] mFeedDetailCols;
    private final String[] mOnlyKeyRowIdCol;
    //will query if last update was 30 minutes before
    private static final int LAST_UPDATE = 30;
    //pre compiled statement
    private final SQLiteStatement mCreateFeedDetailStatement;
    
    public FeedDetailProvider(SQLiteDatabase db){
    	mDB = db;
    	//insert statement to insert feed into feedDetail table
    	mCreateFeedDetailStatement = db.compileStatement(
    			"INSERT INTO " + Tables.FEED_DETAIL+ "("
                + FEED_ID + ","
                + TITLE + ","
                + LINK + ","
                + PUB_DATE + ","
                + READ + ","
                + GUID + ","
                + STARRED + ","
                + DESCRIPTION + ")"
                + " VALUES (?,?,?,?,0,?,0,?);");//Read and Starred - 0
    	//columns need to be fetched
    	mFeedDetailCols = new String[] {KEY_ROWID, TITLE,
                PUB_DATE,LINK,READ,STARRED};
    	mOnlyKeyRowIdCol = new String[]{KEY_ROWID};
    }
    
    /**
     * Create a new feed using the feed sourced Id and feed bean. If the feed is
     * successfully created return the new rowId for that feed, otherwise return
     * a -1 to indicate failure or 0 feed already exists
     * @param feed_id feed source id
     * @param feedBean
     * @return new row id else - to indicate failure
     */
    public long insertFeed(int feed_id,FeedBean feedBean) {
    	Cursor cursor = null;	
    	try{
    		//Check if feed already exists
    		cursor = mDB.query(Tables.FEED_DETAIL, mOnlyKeyRowIdCol, GUID + " like '" + feedBean.getGUID() +"'", null, null, null, null);
        	if (cursor != null && cursor.getCount() > 0){
        		return 0;//feed already exists
        	}
    	} finally {
    		if(cursor != null)
    			cursor.close();
    	}
    	//adding new feeds
        mCreateFeedDetailStatement.bindLong(1, feed_id);//feed source id
        mCreateFeedDetailStatement.bindString(2, feedBean.getTitle());//feed title
        mCreateFeedDetailStatement.bindString(3, feedBean.getLink());//feed url
        mCreateFeedDetailStatement.bindLong(4, feedBean.getPubDate());//feed pub date
        mCreateFeedDetailStatement.bindString(5, feedBean.getGUID());//guid
        mCreateFeedDetailStatement.bindString(6, feedBean.getDescription());//description
        return mCreateFeedDetailStatement.executeInsert();//insert into feedDetail table
    }
    /**
     * Retrieve feeds for corresponding feed source id
     * @param feedID feed source id
     * @param totalCount number of feeds need to be fetched
     * @return 
     */
    public Cursor fetchFeedDetails(int feedID,int totalCount) {
        return mDB.query(Tables.FEED_DETAIL, mFeedDetailCols , FEED_ID + "=" + feedID, null, null, null, FeedDetailProvider.PUB_DATE + " desc LIMIT " + totalCount);
    }
    
    /**
     * Retrieve starred feeds
     * @param totalCount number of feeds need to be fetched
     * @return Cursor 
     */
    public Cursor fetchStarredFeeds(int totalCount){
    	return mDB.query(Tables.FEED_DETAIL, mFeedDetailCols , STARRED + "=" + 1, null, null, null, FeedDetailProvider.PUB_DATE + " desc LIMIT " + totalCount);
    }
    
    /**
     * Retrieve total number of feeds for
     * a particular feedSource
     * @param feedID feed Source id
     * @return int feed count
     */
    public int getCount(int feedID) {
    	int count = 0;
        final Cursor cursor = mDB.query(Tables.FEED_DETAIL, mOnlyKeyRowIdCol , FEED_ID + "=" + feedID, null, null, null, null);
        count = cursor.getCount();
        cursor.close();
        return count;
        
        
    }
    
    /**
     * mark feed as starred
     * @param rowId id to make feed as starred/not starred
     * @param isStarred starred/not starred
     * @return boolean query executed successfully
     */
    public boolean markAsStarred(int rowId, boolean isStarred) {
        final ContentValues args = new ContentValues();
        args.put(STARRED, isStarred ? 1 : 0); // 0 - Not Starred 1 - Starred
        return mDB.update(Tables.FEED_DETAIL, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * mark feed as read
     * @param rowId id to make feed as read/unread
     * @param isRead read/unread
     * @return boolean query executed successfully
     */
    public boolean markAsRead(int rowId, boolean isRead) {
        final ContentValues args = new ContentValues();
        args.put(READ, isRead ? 1 : 0);//0 - Not Read 1 - Read
        return mDB.update(Tables.FEED_DETAIL, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    /**
     * Will return the url of feedDetail 
     * @param key_RowID
     * @return
     */
    public String getURL(int key_RowID){
    	final Cursor cursor = mDB.query(Tables.FEED_DETAIL,new String[]{KEY_ROWID,LINK}, KEY_ROWID + " = " + key_RowID ,null, null, null, null);
    	String url = "";
    	cursor.moveToFirst();
    	//check url is not null
    	if(cursor.getCount() > 0 && !cursor.isNull(1)){
    		url = cursor.getString(1);
    	}
    	cursor.close();
    	return url;
    }
    /**
     * Query for getting description from database
     * for corresponding feedId
     * @param key_RowId feed id for which description need to be retrieved
     * @return String description
     */
    public String getDescription(int key_RowId){
    	final Cursor cursor = mDB.query(Tables.FEED_DETAIL,new String[]{KEY_ROWID,DESCRIPTION}, KEY_ROWID + " = " + key_RowId ,null, null, null, null);
    	String description = "";
    	cursor.moveToFirst();
    	//check description is not null
    	if(cursor.getCount() > 0 && !cursor.isNull(1)){
    		description = cursor.getString(1);
    	}
    	cursor.close();
    	return description;
    }
    
    /**
     * Need to fetch feed or not, compute on the basis
     * of last update.
     * @param rowId
     * @return boolean need to fetch feeds
     */
    public boolean needToFetch(int rowId){
    	final ContentValues args = new ContentValues();
    	args.put(FeedSourceProvider.KEY_ROWID, rowId);
    	final Cursor cursor = mDB.query(Tables.FEED_SOURCE,new String[]{KEY_ROWID,FeedSourceProvider.LAST_UPDATED},FeedSourceProvider.KEY_ROWID + "=" + rowId,
    			null,null,null,null);
    	boolean value;
    	cursor.moveToFirst();
    	//check Last_Updated column value is not null
    	if(cursor.getCount() > 0 && !cursor.isNull(1)) {
    		//fetch last update time and compare it with LAST_UPDATE threshold
    		//value
    		final Long lastUpdateTime = cursor.getLong(1);
    		value = ((System.currentTimeMillis() - lastUpdateTime) / 60000) - LAST_UPDATE > 0 ? true : false;
    	}	else {
    		value =  true;
    	}
    	cursor.close();
    	return value;
    }
    /**
     * Update last feed fetched time
     * @param rowId
     * @return boolen updated successfully or not
     */
    public boolean updateFetchTime(int rowId){
    	final ContentValues args = new ContentValues();
    	args.put(FeedSourceProvider.LAST_UPDATED, System.currentTimeMillis());
    	Log.d(Constants.FEED_READER_LOG
    			,"Updating Fetch Time : " + System.currentTimeMillis() + " " + rowId + "");
    	return mDB.update(Tables.FEED_SOURCE, args,KEY_ROWID + "=" + rowId, null) > 0;
    }
}
